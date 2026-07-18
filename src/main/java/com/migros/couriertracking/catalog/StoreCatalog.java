package com.migros.couriertracking.catalog;

import com.migros.couriertracking.domain.Store;

import java.util.List;
import java.util.Optional;

public interface StoreCatalog {
    List<Store> getStores();

    Optional<Store> findById(String id);
}