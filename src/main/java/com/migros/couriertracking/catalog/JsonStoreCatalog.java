package com.migros.couriertracking.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.couriertracking.domain.Store;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class JsonStoreCatalog implements StoreCatalog {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private List<Store> stores = List.of();

    public JsonStoreCatalog(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void load() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:stores.json");
        try (InputStream inputStream = resource.getInputStream()) {
            List<Store> loadedStores = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            stores = List.copyOf(loadedStores);
        }
    }

    @Override
    public List<Store> getStores() {
        return Collections.unmodifiableList(stores);
    }

    @Override
    public Optional<Store> findById(String id) {
        return stores.stream().filter(store -> store.id().equals(id)).findFirst();
    }
}