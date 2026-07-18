package com.migros.couriertracking.domain;

import java.time.Instant;

public record CourierLocationEvent(
        Instant time,
        String courierId,
        double lat,
        double lng
) {
}