package com.migros.couriertracking.service.impl;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.entity.CourierDistanceEntity;
import com.migros.couriertracking.repository.CourierEventRepository;
import com.migros.couriertracking.repository.jpa.CourierDistanceJpaRepository;
import com.migros.couriertracking.service.CourierStatisticsService;
import com.migros.couriertracking.strategy.DistanceStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourierStatisticsServiceImpl implements CourierStatisticsService {

    private final CourierEventRepository courierEventRepository;
    private final CourierDistanceJpaRepository courierDistanceJpaRepository;
    private final DistanceStrategy distanceStrategy;

    public CourierStatisticsServiceImpl(CourierEventRepository courierEventRepository,
                                         CourierDistanceJpaRepository courierDistanceJpaRepository,
                                         DistanceStrategy distanceStrategy) {
        this.courierEventRepository = courierEventRepository;
        this.courierDistanceJpaRepository = courierDistanceJpaRepository;
        this.distanceStrategy = distanceStrategy;
    }

    @Override
    @Transactional
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

        courierDistanceJpaRepository.save(new CourierDistanceEntity(courierId, totalDistance));
    }

    @Override
    public double getTotalTravelDistance(String courierId) {
        return courierDistanceJpaRepository.findById(courierId)
                .map(CourierDistanceEntity::getTotalDistanceMeters)
                .orElse(0.0);
    }
}

