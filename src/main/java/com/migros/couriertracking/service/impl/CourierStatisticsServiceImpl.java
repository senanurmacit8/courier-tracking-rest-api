package com.migros.couriertracking.service.impl;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.repository.CourierEventRepository;
import com.migros.couriertracking.service.CourierStatisticsService;
import com.migros.couriertracking.strategy.DistanceStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CourierStatisticsServiceImpl implements CourierStatisticsService {

    private final CourierEventRepository courierEventRepository;
    private final DistanceStrategy distanceStrategy;
    private final Map<String, Double> totalDistanceByCourier = new ConcurrentHashMap<>();

    public CourierStatisticsServiceImpl(CourierEventRepository courierEventRepository, DistanceStrategy distanceStrategy) {
        this.courierEventRepository = courierEventRepository;
        this.distanceStrategy = distanceStrategy;
    }

    @Override
    public void recalculate(String courierId) {
        List<CourierLocationEvent> events = courierEventRepository.getEvents(courierId);
        double totalDistance = 0.0;

        for (int index = 1; index < events.size(); index++) {
            CourierLocationEvent previous = events.get(index - 1);
            CourierLocationEvent current = events.get(index);
            totalDistance += distanceStrategy.distanceMeters(
                    previous.lat(),
                    previous.lng(),
                    current.lat(),
                    current.lng()
            );
        }

        totalDistanceByCourier.put(courierId, totalDistance);
    }

    @Override
    public double getTotalTravelDistance(String courierId) {
        return totalDistanceByCourier.getOrDefault(courierId, 0.0);
    }
}
