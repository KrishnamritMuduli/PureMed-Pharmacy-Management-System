package com.pharmacy.controller;

import com.pharmacy.dto.BatchRestockRequest;
import com.pharmacy.model.Medicine;
import com.pharmacy.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "*")
public class MedicineController {
    private final InventoryService inventoryService;

    public MedicineController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Medicine> getMedicines(@RequestParam(required = false) String query,
                                       @RequestParam(required = false) String filter) {
        if ("low-stock".equalsIgnoreCase(filter)) {
            return inventoryService.getLowStockAlerts(10);
        }
        if ("expired".equalsIgnoreCase(filter)) {
            return inventoryService.getExpiredMedicines();
        }
        if ("near-expiry".equalsIgnoreCase(filter)) {
            return inventoryService.getNearExpiryMedicines(60);
        }
        if (query != null && !query.trim().isEmpty()) {
            return inventoryService.searchMedicines(query);
        }
        return inventoryService.getAllMedicines();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable String id) {
        return inventoryService.getMedicine(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> addMedicine(@RequestBody Medicine medicine) {
        try {
            Medicine created = inventoryService.addMedicine(medicine);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<?> restockBatch(@PathVariable String id, @RequestBody BatchRestockRequest request) {
        try {
            Medicine updated = inventoryService.restockBatch(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedicine(@PathVariable String id) {
        boolean deleted = inventoryService.deleteMedicine(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Medicine deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
