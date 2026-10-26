package com.pharmacy.service;

import com.pharmacy.dto.LoginRequest;
import com.pharmacy.dto.RegisterSellerRequest;
import com.pharmacy.model.Seller;
import com.pharmacy.repository.SellerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AuthService {
    private final SellerRepository sellerRepository;
    private final AtomicReference<Seller> currentSessionSeller = new AtomicReference<>();

    public AuthService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public Seller register(RegisterSellerRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (sellerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        Seller seller = new Seller(
                null,
                request.getPharmacyName() != null ? request.getPharmacyName() : "Arogya Medico & Pharmacy",
                request.getUsername().trim(),
                request.getPassword().trim(),
                request.getContactPhone(),
                request.getEmail(),
                request.getAddress()
        );
        Seller saved = sellerRepository.save(seller);
        currentSessionSeller.set(saved);
        return saved;
    }

    public Optional<Seller> login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Optional.empty();
        }
        Optional<Seller> sellerOpt = sellerRepository.findByUsername(request.getUsername().trim());
        if (sellerOpt.isPresent() && sellerOpt.get().getPassword().equals(request.getPassword().trim())) {
            currentSessionSeller.set(sellerOpt.get());
            return sellerOpt;
        }
        return Optional.empty();
    }

    public Optional<Seller> getCurrentSeller() {
        Seller current = currentSessionSeller.get();
        if (current == null) {
            // default to first registered seller if any
            Optional<Seller> first = sellerRepository.findAll().stream().findFirst();
            first.ifPresent(currentSessionSeller::set);
            return first;
        }
        return Optional.of(current);
    }

    public void logout() {
        currentSessionSeller.set(null);
    }
}
