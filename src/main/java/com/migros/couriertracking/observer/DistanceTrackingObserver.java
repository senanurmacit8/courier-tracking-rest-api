package com.migros.couriertracking.observer;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.service.CourierStatisticsService;
import org.springframework.stereotype.Component;

@Component
public class DistanceTrackingObserver implements LocationEventObserver {

    private final CourierStatisticsService courierStatisticsService;

    public DistanceTrackingObserver(CourierStatisticsService courierStatisticsService) {
        this.courierStatisticsService = courierStatisticsService;
    }

    @Override
    public void onLocationEvent(CourierLocationEvent event) {
        courierStatisticsService.recalculate(event.courierId());
    }
}