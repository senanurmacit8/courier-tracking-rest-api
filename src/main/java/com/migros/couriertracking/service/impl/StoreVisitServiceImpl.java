package com.migros.couriertracking.service.impl;

import com.migros.couriertracking.catalog.StoreCatalog;
import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.domain.Store;
import com.migros.couriertracking.domain.StoreVisitLog;
import com.migros.couriertracking.repository.CourierEventRepository;
import com.migros.couriertracking.service.StoreVisitService;
import com.migros.couriertracking.strategy.DistanceStrategy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StoreVisitServiceImpl implements StoreVisitService {

    private static final double ENTRY_RADIUS_METERS = 100.0;
    private static final Duration REENTRY_COOLDOWN = Duration.ofMinutes(1);

    private final CourierEventRepository courierEventRepository;
    private final StoreCatalog storeCatalog;
    private final DistanceStrategy distanceStrategy;
    private final Map<String, List<StoreVisitLog>> visitsByCourier = new ConcurrentHashMap<>();

    public StoreVisitServiceImpl(CourierEventRepository courierEventRepository,
                                StoreCatalog storeCatalog,
                                DistanceStrategy distanceStrategy) {
        this.courierEventRepository = courierEventRepository;
        this.storeCatalog = storeCatalog;
        this.distanceStrategy = distanceStrategy;
    }

    @Override
    public void recalculate(String courierId) {
        List<CourierLocationEvent> events = courierEventRepository.getEvents(courierId);
        List<StoreVisitLog> calculatedVisits = new ArrayList<>();

        for (Store store : storeCatalog.getStores()) {
            boolean previousInside = false;
            CourierLocationEvent lastCountedEntrance = null;

            for (CourierLocationEvent event : events) {
                boolean inside = isInsideStore(event, store);
                boolean entered = inside && !previousInside;

                if (entered && isPastCooldown(lastCountedEntrance, event)) {
                    calculatedVisits.add(new StoreVisitLog(
                            event.time(),
                            event.courierId(),
                            store.id(),
                            store.name(),
                            event.lat(),
                            event.lng()
                    ));
                    lastCountedEntrance = event;
                }

                previousInside = inside;
            }
        }

        calculatedVisits.sort(Comparator.comparing(StoreVisitLog::time).thenComparing(StoreVisitLog::storeId));
        visitsByCourier.put(courierId, List.copyOf(calculatedVisits));
    }

    @Override
    public List<StoreVisitLog> getAllVisits() {
        return visitsByCourier.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(StoreVisitLog::time).thenComparing(StoreVisitLog::courierId))
                .toList();
    }

    @Override
    public List<StoreVisitLog> getVisitsForCourier(String courierId) {
        return visitsByCourier.getOrDefault(courierId, List.of());
    }

    private boolean isInsideStore(CourierLocationEvent event, Store store) {
        return store.distanceTo(event.lat(), event.lng(), distanceStrategy) <= ENTRY_RADIUS_METERS;
    }

    private boolean isPastCooldown(CourierLocationEvent lastCountedEntrance, CourierLocationEvent currentEvent) {
        if (lastCountedEntrance == null) {
            return true;
        }

        return !currentEvent.time().isBefore(lastCountedEntrance.time().plus(REENTRY_COOLDOWN));
    }
}
