package com.migros.couriertracking.strategy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineDistanceStrategyTest {

    private final HaversineDistanceStrategy strategy = new HaversineDistanceStrategy();

    @Test
    void calculatesDistanceBetweenTwoNearbyPoints() {
        double distance = strategy.distanceMeters(41.0, 29.0, 41.001, 29.0);

        assertThat(distance).isBetween(110.0, 113.0);
    }
}