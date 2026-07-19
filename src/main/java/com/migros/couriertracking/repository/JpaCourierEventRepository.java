package com.migros.couriertracking.repository;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.entity.CourierLocationEventEntity;
import com.migros.couriertracking.repository.jpa.CourierLocationEventJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-backed implementation of {@link CourierEventRepository}. Converts
 * between the persistence entity ({@link CourierLocationEventEntity}) and
 * the plain domain record ({@link CourierLocationEvent}) so the rest of the
 * application never depends on JPA directly.
 */
@Repository
public class JpaCourierEventRepository implements CourierEventRepository {

    private final CourierLocationEventJpaRepository jpaRepository;

    public JpaCourierEventRepository(CourierLocationEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void addEvent(CourierLocationEvent event) {
        jpaRepository.save(new CourierLocationEventEntity(
                event.time(),
                event.courierId(),
                event.lat(),
                event.lng()
        ));
    }

    @Override
    public List<CourierLocationEvent> getEvents(String courierId) {
        return jpaRepository.findByCourierIdOrderByTimeAsc(courierId).stream()
                .map(entity -> new CourierLocationEvent(entity.getTime(), entity.getCourierId(), entity.getLat(), entity.getLng()))
                .toList();
    }
}
