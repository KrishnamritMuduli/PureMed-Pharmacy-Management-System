package com.pharmacy.repository;

import com.pharmacy.model.CreditTransaction;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class CreditTransactionRepository {
    private final Map<String, CreditTransaction> storage = new ConcurrentHashMap<>();
    private final List<CreditTransaction> transactionHistory = new CopyOnWriteArrayList<>();

    public CreditTransaction save(CreditTransaction transaction) {
        if (transaction.getTransactionId() == null || transaction.getTransactionId().trim().isEmpty()) {
            transaction.setTransactionId("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        storage.put(transaction.getTransactionId(), transaction);
        transactionHistory.add(transaction);
        return transaction;
    }

    public Optional<CreditTransaction> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<CreditTransaction> findAll() {
        return new ArrayList<>(transactionHistory);
    }

    public List<CreditTransaction> findByBorrowerId(String borrowerId) {
        return transactionHistory.stream()
                .filter(tx -> tx.getBorrowerId() != null && tx.getBorrowerId().equals(borrowerId))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // latest first
                .collect(Collectors.toList());
    }

    public long count() {
        return storage.size();
    }
}
