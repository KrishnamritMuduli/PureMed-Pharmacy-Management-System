package com.pharmacy.repository;

import com.pharmacy.model.Seller;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SellerRepository {
    private final Map<String, Seller> storage = new ConcurrentHashMap<>();

    public Seller save(Seller seller) {
        if (seller.getId() == null || seller.getId().trim().isEmpty()) {
            seller.setId("SEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        storage.put(seller.getId(), seller);
        return seller;
    }

    public Optional<Seller> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Seller> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return storage.values().stream()
                .filter(s -> s.getUsername() != null && s.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst();
    }

    public List<Seller> findAll() {
        return new ArrayList<>(storage.values());
    }

    public long count() {
        return storage.size();
    }
}
