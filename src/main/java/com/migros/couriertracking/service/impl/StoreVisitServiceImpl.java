package com.migros.couriertracking.service.impl;

import com.migros.couriertracking.catalog.StoreCatalog;
import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.domain.Store;
import com.migros.couriertracking.domain.StoreVisitLog;
import com.migros.couriertracking.entity.StoreVisitLogEntity;
import com.migros.couriertracking.repository.CourierEventRepository;
import com.migros.couriertracking.repository.jpa.StoreVisitLogJpaRepository;
import com.migros.couriertracking.service.StoreVisitService;
import com.migros.couriertracking.strategy.DistanceStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StoreVisitServiceImpl implements StoreVisitService {

    private static final double ENTRY_RADIUS_METERS = 100.0;
    private static final Duration REENTRY_COOLDOWN = Duration.ofMinutes(1);

    private final CourierEventRepository courierEventRepository;
    private final StoreCatalog storeCatalog;
    private final DistanceStrategy distanceStrategy;
    private final StoreVisitLogJpaRepository storeVisitLogJpaRepository;

    public StoreVisitServiceImpl(CourierEventRepository courierEventRepository,
                                StoreCatalog storeCatalog,
                                DistanceStrategy distanceStrategy,
                                StoreVisitLogJpaRepository storeVisitLogJpaRepository) {
        this.courierEventRepository = courierEventRepository;
        this.storeCatalog = storeCatalog;
        this.distanceStrategy = distanceStrategy;
        this.storeVisitLogJpaRepository = storeVisitLogJpaRepository;
    }

    @Override
    @Transactional
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

        // calculatedVisits is a full recompute, so just wipe the old rows and re-save.
        storeVisitLogJpaRepository.deleteByCourierId(courierId);
        storeVisitLogJpaRepository.saveAll(calculatedVisits.stream()
                .map(visit -> new StoreVisitLogEntity(
                        visit.time(), visit.courierId(), visit.storeId(), visit.storeName(), visit.lat(), visit.lng()
                ))
                .toList());
    }

    @Override
    public List<StoreVisitLog> getAllVisits() {
        return storeVisitLogJpaRepository.findAllByOrderByTimeAscCourierIdAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<StoreVisitLog> getVisitsForCourier(String courierId) {
        return storeVisitLogJpaRepository.findByCourierIdOrderByTimeAscStoreIdAsc(courierId).stream()
                .map(this::toDomain)
                .toList();
    }

    private StoreVisitLog toDomain(StoreVisitLogEntity entity) {
        return new StoreVisitLog(entity.getTime(), entity.getCourierId(), entity.getStoreId(), entity.getStoreName(),
                entity.getLat(), entity.getLng());
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

