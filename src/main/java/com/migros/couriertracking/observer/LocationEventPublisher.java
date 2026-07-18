package com.migros.couriertracking.observer;

import com.migros.couriertracking.domain.CourierLocationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationEventPublisher {

    private final List<LocationEventObserver> observers;

    public LocationEventPublisher(List<LocationEventObserver> observers) {
        this.observers = observers;
    }

    public void publish(CourierLocationEvent event) {
        observers.forEach(observer -> observer.onLocationEvent(event));
    }
}