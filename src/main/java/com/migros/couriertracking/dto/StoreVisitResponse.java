package com.migros.couriertracking.dto;

import java.time.Instant;

public record StoreVisitResponse(
        Instant time,
        String courierId,
        String storeId,
        String storeName,
        double lat,
        double lng
) {
}