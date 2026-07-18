package com.migros.couriertracking.strategy;

public interface DistanceStrategy {
    double distanceMeters(double lat1, double lng1, double lat2, double lng2);
}