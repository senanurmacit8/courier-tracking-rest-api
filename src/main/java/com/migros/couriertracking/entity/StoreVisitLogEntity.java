package com.migros.couriertracking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "store_visit_log")
public class StoreVisitLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant time;

    @Column(nullable = false)
    private String courierId;

    @Column(nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    protected StoreVisitLogEntity() {
        // required by JPA
    }

    public StoreVisitLogEntity(Instant time, String courierId, String storeId, String storeName, double lat, double lng) {
        this.time = time;
        this.courierId = courierId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.lat = lat;
        this.lng = lng;
    }

    public Long getId() {
        return id;
    }

    public Instant getTime() {
        return time;
    }

    public String getCourierId() {
        return courierId;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
