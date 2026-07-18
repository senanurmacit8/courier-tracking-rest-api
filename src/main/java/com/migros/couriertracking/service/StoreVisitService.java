package com.migros.couriertracking.service;

import com.migros.couriertracking.domain.StoreVisitLog;

import java.util.List;

public interface StoreVisitService {
    void recalculate(String courierId);

    List<StoreVisitLog> getAllVisits();

    List<StoreVisitLog> getVisitsForCourier(String courierId);
}