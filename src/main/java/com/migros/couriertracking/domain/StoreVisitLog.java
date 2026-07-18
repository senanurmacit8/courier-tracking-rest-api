package com.migros.couriertracking.domain;

import java.time.Instant;

public record StoreVisitLog(
        Instant time,
        String courierId,
        String storeId,
        String storeName,
        double lat,
        double lng
) {
}