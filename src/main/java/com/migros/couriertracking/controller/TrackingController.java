package com.migros.couriertracking.controller;

import com.migros.couriertracking.dto.CourierDistanceResponse;
import com.migros.couriertracking.dto.CourierLocationRequest;
import com.migros.couriertracking.dto.LocationIngestResponse;
import com.migros.couriertracking.dto.StoreVisitResponse;
import com.migros.couriertracking.service.CourierLocationIngestionService;
import com.migros.couriertracking.service.CourierStatisticsService;
import com.migros.couriertracking.service.StoreVisitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TrackingController {

    private final CourierLocationIngestionService courierLocationIngestionService;
    private final CourierStatisticsService courierStatisticsService;
    private final StoreVisitService storeVisitService;

    public TrackingController(CourierLocationIngestionService courierLocationIngestionService,
                               CourierStatisticsService courierStatisticsService,
                               StoreVisitService storeVisitService) {
        this.courierLocationIngestionService = courierLocationIngestionService;
        this.courierStatisticsService = courierStatisticsService;
        this.storeVisitService = storeVisitService;
    }

    @PostMapping("/locations")
    public LocationIngestResponse ingestLocation(@Valid @RequestBody CourierLocationRequest request) {
        courierLocationIngestionService.ingest(request);
        return new LocationIngestResponse(
                request.courierId(),
                courierStatisticsService.getTotalTravelDistance(request.courierId()),
                storeVisitService.getVisitsForCourier(request.courierId()).size()
        );
    }

    @GetMapping("/couriers/{courierId}/distance")
    public CourierDistanceResponse getCourierDistance(@PathVariable String courierId) {
        return new CourierDistanceResponse(courierId, courierStatisticsService.getTotalTravelDistance(courierId));
    }

    @GetMapping("/store-visits")
    public List<StoreVisitResponse> getStoreVisits() {
        return storeVisitService.getAllVisits().stream()
                .map(log -> new StoreVisitResponse(
                        log.time(),
                        log.courierId(),
                        log.storeId(),
                        log.storeName(),
                        log.lat(),
                        log.lng()
                ))
                .toList();
    }
}