package com.migros.couriertracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CourierLocationRequest(
        @NotNull Instant time,
        @NotBlank String courierId,
        double lat,
        double lng
) {
}