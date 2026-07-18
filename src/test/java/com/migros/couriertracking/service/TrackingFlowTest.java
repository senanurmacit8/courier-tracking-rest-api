package com.migros.couriertracking.service;

import com.migros.couriertracking.catalog.StoreCatalog;
import com.migros.couriertracking.domain.Store;
import com.migros.couriertracking.observer.LocationEventPublisher;
import com.migros.couriertracking.repository.CourierEventRepository;
import com.migros.couriertracking.repository.InMemoryCourierEventRepository;
import com.migros.couriertracking.service.impl.CourierLocationIngestionServiceImpl;
import com.migros.couriertracking.service.impl.CourierStatisticsServiceImpl;
import com.migros.couriertracking.service.impl.StoreVisitServiceImpl;
import com.migros.couriertracking.strategy.DistanceStrategy;
import com.migros.couriertracking.strategy.HaversineDistanceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TrackingFlowTest {

    private CourierEventRepository courierEventRepository;
    private StoreCatalog storeCatalog;
    private DistanceStrategy distanceStrategy;
    private CourierStatisticsService courierStatisticsService;
    private StoreVisitService storeVisitService;
    private CourierLocationIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        courierEventRepository = new InMemoryCourierEventRepository();
        storeCatalog = new StoreCatalog() {
            private final List<Store> stores = List.of(new Store("store-1", "Migros Test Store", 0.0, 0.0));

            @Override
            public List<Store> getStores() {
                return stores;
            }

            @Override
            public Optional<Store> findById(String id) {
                return stores.stream().filter(store -> store.id().equals(id)).findFirst();
            }
        };
        distanceStrategy = new HaversineDistanceStrategy();
        courierStatisticsService = new CourierStatisticsServiceImpl(courierEventRepository, distanceStrategy);
        storeVisitService = new StoreVisitServiceImpl(courierEventRepository, storeCatalog, distanceStrategy);
        LocationEventPublisher publisher = new LocationEventPublisher(List.of(
                event -> courierStatisticsService.recalculate(event.courierId()),
                event -> storeVisitService.recalculate(event.courierId())
        ));
        ingestionService = new CourierLocationIngestionServiceImpl(
                courierEventRepository,
            publisher
        );
    }

    @Test
    void calculatesTotalDistanceAndAppliesStoreCooldown() {
        String courierId = "courier-1";
        Instant start = Instant.parse("2026-07-18T10:00:00Z");

        ingestionService.ingest(new com.migros.couriertracking.dto.CourierLocationRequest(start, courierId, 0.0020, 0.0));
        ingestionService.ingest(new com.migros.couriertracking.dto.CourierLocationRequest(start.plusSeconds(10), courierId, 0.0005, 0.0));
        ingestionService.ingest(new com.migros.couriertracking.dto.CourierLocationRequest(start.plusSeconds(20), courierId, 0.0020, 0.0));
        ingestionService.ingest(new com.migros.couriertracking.dto.CourierLocationRequest(start.plusSeconds(50), courierId, 0.0005, 0.0));
        ingestionService.ingest(new com.migros.couriertracking.dto.CourierLocationRequest(start.plusSeconds(70), courierId, 0.0020, 0.0));
        ingestionService.ingest(new com.migros.couriertracking.dto.CourierLocationRequest(start.plusSeconds(90), courierId, 0.0005, 0.0));

        assertThat(courierStatisticsService.getTotalTravelDistance(courierId)).isGreaterThan(0.0);
        assertThat(storeVisitService.getVisitsForCourier(courierId)).hasSize(2);
    }
}