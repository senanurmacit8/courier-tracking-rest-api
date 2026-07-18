package com.migros.couriertracking.service;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.dto.CourierLocationRequest;
import com.migros.couriertracking.observer.LocationEventPublisher;
import com.migros.couriertracking.repository.CourierEventRepository;
import org.springframework.stereotype.Service;

@Service
public class CourierLocationIngestionService {

    private final CourierEventRepository courierEventRepository;
    private final LocationEventPublisher locationEventPublisher;

    public CourierLocationIngestionService(CourierEventRepository courierEventRepository,
                                           LocationEventPublisher locationEventPublisher) {
        this.courierEventRepository = courierEventRepository;
        this.locationEventPublisher = locationEventPublisher;
    }

    public void ingest(CourierLocationRequest request) {
        CourierLocationEvent event = new CourierLocationEvent(
                request.time(),
                request.courierId(),
                request.lat(),
                request.lng()
        );

        courierEventRepository.addEvent(event);
        locationEventPublisher.publish(event);
    }
}