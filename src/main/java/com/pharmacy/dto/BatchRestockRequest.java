package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BatchRestockRequest {
    private int quantityToAdd;
    private String newBatchNumber;
    private BigDecimal newPurchasePrice;
    private BigDecimal newSellingPrice;
    private LocalDate newExpiryDate;

    public BatchRestockRequest() {}

    public BatchRestockRequest(int quantityToAdd, String newBatchNumber, BigDecimal newPurchasePrice, BigDecimal newSellingPrice, LocalDate newExpiryDate) {
        this.quantityToAdd = quantityToAdd;
        this.newBatchNumber = newBatchNumber;
        this.newPurchasePrice = newPurchasePrice;
        this.newSellingPrice = newSellingPrice;
        this.newExpiryDate = newExpiryDate;
    }

    public int getQuantityToAdd() {
        return quantityToAdd;
    }

    public void setQuantityToAdd(int quantityToAdd) {
        this.quantityToAdd = quantityToAdd;
    }

    public String getNewBatchNumber() {
        return newBatchNumber;
    }

    public void setNewBatchNumber(String newBatchNumber) {
        this.newBatchNumber = newBatchNumber;
    }

    public BigDecimal getNewPurchasePrice() {
        return newPurchasePrice;
    }

    public void setNewPurchasePrice(BigDecimal newPurchasePrice) {
        this.newPurchasePrice = newPurchasePrice;
    }

    public BigDecimal getNewSellingPrice() {
        return newSellingPrice;
    }

    public void setNewSellingPrice(BigDecimal newSellingPrice) {
        this.newSellingPrice = newSellingPrice;
    }

    public LocalDate getNewExpiryDate() {
        return newExpiryDate;
    }

    public void setNewExpiryDate(LocalDate newExpiryDate) {
        this.newExpiryDate = newExpiryDate;
    }
}
