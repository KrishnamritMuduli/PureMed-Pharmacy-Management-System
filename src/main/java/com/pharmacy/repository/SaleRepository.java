package com.pharmacy.repository;

import com.pharmacy.model.Sale;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class SaleRepository {
    private final Map<String, Sale> storage = new ConcurrentHashMap<>();
    private final List<Sale> salesLog = new CopyOnWriteArrayList<>();

    public Sale save(Sale sale) {
        if (sale.getId() == null || sale.getId().trim().isEmpty()) {
            sale.setId("SALE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (sale.getInvoiceNumber() == null || sale.getInvoiceNumber().trim().isEmpty()) {
            sale.setInvoiceNumber("INV-" + System.currentTimeMillis() % 1000000);
        }
        storage.put(sale.getId(), sale);
        salesLog.add(sale);
        return sale;
    }

    public Optional<Sale> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Sale> findAll() {
        return new ArrayList<>(salesLog);
    }

    public List<Sale> findRecent(int limit) {
        int size = salesLog.size();
        if (size <= limit) {
            List<Sale> reversed = new ArrayList<>(salesLog);
            Collections.reverse(reversed);
            return reversed;
        }
        List<Sale> sub = salesLog.subList(size - limit, size);
        List<Sale> reversed = new ArrayList<>(sub);
        Collections.reverse(reversed);
        return reversed;
    }

    public List<Sale> findByMedicineId(String medicineId) {
        return salesLog.stream()
                .filter(s -> s.getMedicineId() != null && s.getMedicineId().equals(medicineId))
                .collect(Collectors.toList());
    }

    public List<Sale> findSalesBetween(LocalDateTime start, LocalDateTime end) {
        return salesLog.stream()
                .filter(s -> (s.getSaleDate().isEqual(start) || s.getSaleDate().isAfter(start))
                        && (s.getSaleDate().isEqual(end) || s.getSaleDate().isBefore(end)))
                .collect(Collectors.toList());
    }

    public List<Sale> findSalesSince(LocalDateTime since) {
        return salesLog.stream()
                .filter(s -> s.getSaleDate().isEqual(since) || s.getSaleDate().isAfter(since))
                .collect(Collectors.toList());
    }

    public long count() {
        return storage.size();
    }
}
