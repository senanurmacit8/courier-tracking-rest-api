package com.migros.couriertracking.service.impl;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.dto.CourierLocationRequest;
import com.migros.couriertracking.observer.LocationEventPublisher;
import com.migros.couriertracking.repository.CourierEventRepository;
import com.migros.couriertracking.service.CourierLocationIngestionService;
import org.springframework.stereotype.Service;

@Service
public class CourierLocationIngestionServiceImpl implements CourierLocationIngestionService {

    private final CourierEventRepository courierEventRepository;
    private final LocationEventPublisher locationEventPublisher;

    public CourierLocationIngestionServiceImpl(CourierEventRepository courierEventRepository,
                                              LocationEventPublisher locationEventPublisher) {
        this.courierEventRepository = courierEventRepository;
        this.locationEventPublisher = locationEventPublisher;
    }

    @Override
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
