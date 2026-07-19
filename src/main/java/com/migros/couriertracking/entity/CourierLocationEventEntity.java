package com.migros.couriertracking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;


@Entity
@Table(name = "courier_location_event")
public class CourierLocationEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant time;

    @Column(nullable = false)
    private String courierId;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    protected CourierLocationEventEntity() {
        // required by JPA
    }

    public CourierLocationEventEntity(Instant time, String courierId, double lat, double lng) {
        this.time = time;
        this.courierId = courierId;
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

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
