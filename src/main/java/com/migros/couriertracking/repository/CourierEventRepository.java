package com.migros.couriertracking.repository;

import com.migros.couriertracking.domain.CourierLocationEvent;

import java.util.List;

public interface CourierEventRepository {
    void addEvent(CourierLocationEvent event);

    List<CourierLocationEvent> getEvents(String courierId);
}