package com.migros.couriertracking.service;

import com.migros.couriertracking.dto.CourierLocationRequest;

public interface CourierLocationIngestionService {
    void ingest(CourierLocationRequest request);
}