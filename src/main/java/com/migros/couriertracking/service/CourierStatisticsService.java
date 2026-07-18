package com.migros.couriertracking.service;

public interface CourierStatisticsService {
    void recalculate(String courierId);

    double getTotalTravelDistance(String courierId);
}