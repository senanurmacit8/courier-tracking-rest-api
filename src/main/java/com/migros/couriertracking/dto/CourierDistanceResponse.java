package com.migros.couriertracking.dto;

public record CourierDistanceResponse(
        String courierId,
        double totalDistanceMeters
) {
}