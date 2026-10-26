package com.pharmacy.controller;

import com.pharmacy.dto.CreditActionRequest;
import com.pharmacy.model.Borrower;
import com.pharmacy.model.CreditTransaction;
import com.pharmacy.service.BorrowerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrowers")
@CrossOrigin(origins = "*")
public class BorrowerController {
    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @GetMapping
    public List<Borrower> getBorrowers(@RequestParam(required = false) String query) {
        if (query != null && !query.trim().isEmpty()) {
            return borrowerService.searchBorrowers(query);
        }
        return borrowerService.getAllBorrowers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Borrower> getBorrower(@PathVariable String id) {
        return borrowerService.getBorrower(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBorrower(@RequestBody Borrower borrower) {
        try {
            Borrower created = borrowerService.createBorrower(borrower);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<?> issueCredit(@PathVariable String id, @RequestBody CreditActionRequest request) {
        try {
            CreditTransaction tx = borrowerService.issueCredit(id, request);
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<?> recordPayment(@PathVariable String id, @RequestBody CreditActionRequest request) {
        try {
            CreditTransaction tx = borrowerService.recordPayment(id, request);
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}/ledger")
    public List<CreditTransaction> getCustomerLedger(@PathVariable String id) {
        return borrowerService.getLedger(id);
    }

    @GetMapping("/total-debt")
    public Map<String, BigDecimal> getTotalStoreDebt() {
        return Map.of("totalOutstandingDebt", borrowerService.getTotalOutstandingDebt());
    }
}
