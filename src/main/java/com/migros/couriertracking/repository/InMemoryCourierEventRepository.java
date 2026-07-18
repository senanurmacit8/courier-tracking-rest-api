package com.migros.couriertracking.repository;

import com.migros.couriertracking.domain.CourierLocationEvent;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCourierEventRepository implements CourierEventRepository {

    private final Map<String, List<CourierLocationEvent>> eventsByCourier = new ConcurrentHashMap<>();

    @Override
    public void addEvent(CourierLocationEvent event) {
        eventsByCourier.compute(event.courierId(), (courierId, events) -> {
            List<CourierLocationEvent> list = events == null ? new ArrayList<>() : new ArrayList<>(events);
            list.add(event);
            list.sort(Comparator.comparing(CourierLocationEvent::time));
            return list;
        });
    }

    @Override
    public List<CourierLocationEvent> getEvents(String courierId) {
        List<CourierLocationEvent> events = eventsByCourier.get(courierId);
        if (events == null) {
            return Collections.emptyList();
        }
        return List.copyOf(events);
    }
}