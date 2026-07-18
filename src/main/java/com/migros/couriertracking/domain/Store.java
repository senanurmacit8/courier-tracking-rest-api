package com.migros.couriertracking.domain;

import com.migros.couriertracking.strategy.DistanceStrategy;

public record Store(
        String id,
        String name,
        double lat,
        double lng
) {
    public double distanceTo(double otherLat, double otherLng, DistanceStrategy distanceStrategy) {
        return distanceStrategy.distanceMeters(lat, lng, otherLat, otherLng);
    }
}