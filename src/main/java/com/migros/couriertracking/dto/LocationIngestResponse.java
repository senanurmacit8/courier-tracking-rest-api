package com.migros.couriertracking.dto;

public record LocationIngestResponse(
        String courierId,
        double totalDistanceMeters,
        int totalStoreVisitsForCourier
) {
}