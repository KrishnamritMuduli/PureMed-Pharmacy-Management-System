package com.pharmacy.repository;

import com.pharmacy.model.Borrower;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class BorrowerRepository {
    private final Map<String, Borrower> storage = new ConcurrentHashMap<>();

    public Borrower save(Borrower borrower) {
        if (borrower.getId() == null || borrower.getId().trim().isEmpty()) {
            borrower.setId("BOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        storage.put(borrower.getId(), borrower);
        return borrower;
    }

    public Optional<Borrower> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Borrower> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }

    public List<Borrower> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        String q = query.trim().toLowerCase();
        return storage.values().stream()
                .filter(b -> (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(q))
                        || (b.getPhoneNumber() != null && b.getPhoneNumber().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public BigDecimal calculateTotalOutstandingDebt() {
        return storage.values().stream()
                .map(Borrower::getOutstandingBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long count() {
        return storage.size();
    }
}
