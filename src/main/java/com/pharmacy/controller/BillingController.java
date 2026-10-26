package com.pharmacy.controller;

import com.pharmacy.dto.CheckoutRequest;
import com.pharmacy.dto.InvoiceReceipt;
import com.pharmacy.model.Sale;
import com.pharmacy.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {
    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            InvoiceReceipt receipt = billingService.checkout(request);
            return ResponseEntity.ok(receipt);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/sales")
    public List<Sale> getAllSales() {
        return billingService.getAllSales();
    }

    @GetMapping("/recent")
    public List<Sale> getRecentSales(@RequestParam(defaultValue = "15") int limit) {
        return billingService.getRecentSales(limit);
    }
}
