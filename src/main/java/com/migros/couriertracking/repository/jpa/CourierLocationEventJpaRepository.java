package com.migros.couriertracking.repository.jpa;

import com.migros.couriertracking.entity.CourierLocationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierLocationEventJpaRepository extends JpaRepository<CourierLocationEventEntity, Long> {
    List<CourierLocationEventEntity> findByCourierIdOrderByTimeAsc(String courierId);
}
