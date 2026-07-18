package com.migros.couriertracking.observer;

import com.migros.couriertracking.domain.CourierLocationEvent;

public interface LocationEventObserver {
    void onLocationEvent(CourierLocationEvent event);
}