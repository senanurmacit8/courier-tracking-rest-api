package com.migros.couriertracking.repository.jpa;

import com.migros.couriertracking.entity.StoreVisitLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreVisitLogJpaRepository extends JpaRepository<StoreVisitLogEntity, Long> {
    List<StoreVisitLogEntity> findByCourierIdOrderByTimeAscStoreIdAsc(String courierId);

    List<StoreVisitLogEntity> findAllByOrderByTimeAscCourierIdAsc();

    void deleteByCourierId(String courierId);
}
