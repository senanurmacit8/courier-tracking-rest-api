package com.migros.couriertracking.repository.jpa;

import com.migros.couriertracking.entity.CourierDistanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierDistanceJpaRepository extends JpaRepository<CourierDistanceEntity, String> {
}
