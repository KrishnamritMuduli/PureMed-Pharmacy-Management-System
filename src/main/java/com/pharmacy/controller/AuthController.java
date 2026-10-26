package com.pharmacy.controller;

import com.pharmacy.dto.LoginRequest;
import com.pharmacy.dto.RegisterSellerRequest;
import com.pharmacy.model.Seller;
import com.pharmacy.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Seller> seller = authService.login(request);
        if (seller.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Login successful",
                    "seller", seller.get()
            ));
        }
        return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Invalid username or password"
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterSellerRequest request) {
        try {
            Seller seller = authService.register(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Seller registered successfully",
                    "seller", seller
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Optional<Seller> current = authService.getCurrentSeller();
        if (current.isPresent()) {
            return ResponseEntity.ok(current.get());
        }
        return ResponseEntity.status(404).body(Map.of("message", "No active seller profile"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
