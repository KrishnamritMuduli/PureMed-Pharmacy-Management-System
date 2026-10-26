package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MedicineRepository {
    private final Map<String, Medicine> storage = new ConcurrentHashMap<>();

    public Medicine save(Medicine medicine) {
        if (medicine.getId() == null || medicine.getId().trim().isEmpty()) {
            medicine.setId("MED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        storage.put(medicine.getId(), medicine);
        return medicine;
    }

    public Optional<Medicine> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Medicine> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }

    public List<Medicine> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        String q = query.trim().toLowerCase();
        return storage.values().stream()
                .filter(m -> (m.getName() != null && m.getName().toLowerCase().contains(q))
                        || (m.getGenericName() != null && m.getGenericName().toLowerCase().contains(q))
                        || (m.getBatchNumber() != null && m.getBatchNumber().toLowerCase().contains(q))
                        || (m.getCategory() != null && m.getCategory().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public List<Medicine> findLowStock(int threshold) {
        return storage.values().stream()
                .filter(m -> m.getQuantityInStock() < threshold)
                .collect(Collectors.toList());
    }

    public List<Medicine> findExpired() {
        return storage.values().stream()
                .filter(Medicine::isExpired)
                .collect(Collectors.toList());
    }

    public List<Medicine> findNearExpiry(int daysThreshold) {
        return storage.values().stream()
                .filter(m -> m.isNearExpiry(daysThreshold))
                .collect(Collectors.toList());
    }

    public long count() {
        return storage.size();
    }
}
