package com.migros.couriertracking.observer;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.service.StoreVisitService;
import org.springframework.stereotype.Component;

@Component
public class StoreEntranceObserver implements LocationEventObserver {

    private final StoreVisitService storeVisitService;

    public StoreEntranceObserver(StoreVisitService storeVisitService) {
        this.storeVisitService = storeVisitService;
    }

    @Override
    public void onLocationEvent(CourierLocationEvent event) {
        storeVisitService.recalculate(event.courierId());
    }
}