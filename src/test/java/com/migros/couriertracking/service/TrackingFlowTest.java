package com.migros.couriertracking.service;

import com.migros.couriertracking.dto.CourierLocationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

// End-to-end test with the real Spring context, backed by the in-memory H2
// profile in src/test/resources/application.properties.
@SpringBootTest
class TrackingFlowTest {

    // Coordinates of "migros-001" (Ataşehir MMM Migros) from stores.json.
    private static final double STORE_LAT = 40.9923307;
    private static final double STORE_LNG = 29.1244229;

    @Autowired
    private CourierLocationIngestionService ingestionService;

    @Autowired
    private CourierStatisticsService courierStatisticsService;

    @Autowired
    private StoreVisitService storeVisitService;

    @Test
    void calculatesTotalDistanceAndAppliesStoreCooldown() {
        String courierId = "courier-flow-test";
        Instant start = Instant.parse("2026-07-18T10:00:00Z");

        ingestionService.ingest(new CourierLocationRequest(start, courierId, STORE_LAT + 0.0020, STORE_LNG));
        ingestionService.ingest(new CourierLocationRequest(start.plusSeconds(10), courierId, STORE_LAT + 0.0005, STORE_LNG));
        ingestionService.ingest(new CourierLocationRequest(start.plusSeconds(20), courierId, STORE_LAT + 0.0020, STORE_LNG));
        ingestionService.ingest(new CourierLocationRequest(start.plusSeconds(50), courierId, STORE_LAT + 0.0005, STORE_LNG));
        ingestionService.ingest(new CourierLocationRequest(start.plusSeconds(70), courierId, STORE_LAT + 0.0020, STORE_LNG));
        ingestionService.ingest(new CourierLocationRequest(start.plusSeconds(90), courierId, STORE_LAT + 0.0005, STORE_LNG));

        assertThat(courierStatisticsService.getTotalTravelDistance(courierId)).isGreaterThan(0.0);
        assertThat(storeVisitService.getVisitsForCourier(courierId)).hasSize(2);
    }
}
