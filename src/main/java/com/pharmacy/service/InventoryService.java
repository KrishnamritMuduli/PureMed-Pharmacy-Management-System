package com.pharmacy.service;

import com.pharmacy.dto.BatchRestockRequest;
import com.pharmacy.model.Medicine;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    private final MedicineRepository medicineRepository;

    public InventoryService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public Medicine addMedicine(Medicine medicine) {
        if (medicine.getName() == null || medicine.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be empty");
        }
        if (medicine.getPurchasePrice() == null || medicine.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purchase price must be non-negative");
        }
        if (medicine.getSellingPrice() == null || medicine.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Selling price must be non-negative");
        }
        if (medicine.getQuantityInStock() < 0) {
            medicine.setQuantityInStock(0);
        }
        return medicineRepository.save(medicine);
    }

    public Medicine restockBatch(String medicineId, BatchRestockRequest request) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with ID: " + medicineId));

        if (request.getQuantityToAdd() <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }

        synchronized (medicine) {
            medicine.setQuantityInStock(medicine.getQuantityInStock() + request.getQuantityToAdd());

            if (request.getNewBatchNumber() != null && !request.getNewBatchNumber().trim().isEmpty()) {
                medicine.setBatchNumber(request.getNewBatchNumber().trim());
            }
            if (request.getNewPurchasePrice() != null && request.getNewPurchasePrice().compareTo(BigDecimal.ZERO) > 0) {
                medicine.setPurchasePrice(request.getNewPurchasePrice());
            }
            if (request.getNewSellingPrice() != null && request.getNewSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
                medicine.setSellingPrice(request.getNewSellingPrice());
            }
            if (request.getNewExpiryDate() != null) {
                medicine.setExpiryDate(request.getNewExpiryDate());
            }

            return medicineRepository.save(medicine);
        }
    }

    public Optional<Medicine> getMedicine(String id) {
        return medicineRepository.findById(id);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public List<Medicine> searchMedicines(String query) {
        return medicineRepository.search(query);
    }

    public List<Medicine> getLowStockAlerts(int threshold) {
        return medicineRepository.findLowStock(threshold);
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findExpired();
    }

    public List<Medicine> getNearExpiryMedicines(int daysThreshold) {
        return medicineRepository.findNearExpiry(daysThreshold);
    }

    public boolean deleteMedicine(String id) {
        return medicineRepository.deleteById(id);
    }

    public BigDecimal calculateTotalInventoryCost() {
        return medicineRepository.findAll().stream()
                .map(m -> m.getPurchasePrice().multiply(BigDecimal.valueOf(m.getQuantityInStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalInventoryRetail() {
        return medicineRepository.findAll().stream()
                .map(m -> m.getSellingPrice().multiply(BigDecimal.valueOf(m.getQuantityInStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
