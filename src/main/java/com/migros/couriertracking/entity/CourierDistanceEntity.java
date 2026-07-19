package com.migros.couriertracking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// One row per courier with the last computed total distance. Each recalculation
// just overwrites the previous value.
@Entity
@Table(name = "courier_distance")
public class CourierDistanceEntity {

    @Id
    private String courierId;

    @Column(nullable = false)
    private double totalDistanceMeters;

    protected CourierDistanceEntity() {
        // required by JPA
    }

    public CourierDistanceEntity(String courierId, double totalDistanceMeters) {
        this.courierId = courierId;
        this.totalDistanceMeters = totalDistanceMeters;
    }

    public String getCourierId() {
        return courierId;
    }

    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }
}
