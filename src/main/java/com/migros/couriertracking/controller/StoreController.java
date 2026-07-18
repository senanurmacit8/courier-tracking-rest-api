package com.migros.couriertracking.controller;

import com.migros.couriertracking.catalog.StoreCatalog;
import com.migros.couriertracking.domain.Store;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreCatalog storeCatalog;

    public StoreController(StoreCatalog storeCatalog) {
        this.storeCatalog = storeCatalog;
    }

    @GetMapping
    public List<Store> getStores() {
        return storeCatalog.getStores();
    }
}