package com.migros.couriertracking.repository;

import com.migros.couriertracking.domain.CourierLocationEvent;
import com.migros.couriertracking.entity.CourierLocationEventEntity;
import com.migros.couriertracking.repository.jpa.CourierLocationEventJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Converts between the JPA entity and the domain record, so the rest of the
// app doesn't need to know JPA exists.
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
