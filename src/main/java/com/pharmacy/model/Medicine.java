package com.pharmacy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Medicine implements Serializable {
    private String id;
    private String name;
    private String genericName;
    private String batchNumber;
    private int quantityInStock;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private LocalDate expiryDate;
    private String category;
    private String manufacturer;

    public Medicine() {}

    public Medicine(String id, String name, String genericName, String batchNumber,
                    int quantityInStock, BigDecimal purchasePrice, BigDecimal sellingPrice,
                    LocalDate expiryDate, String category, String manufacturer) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.batchNumber = batchNumber;
        this.quantityInStock = quantityInStock;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.expiryDate = expiryDate;
        this.category = category;
        this.manufacturer = manufacturer;
    }

    public boolean isLowStock() {
        return this.quantityInStock < 10;
    }

    public boolean isExpired() {
        if (this.expiryDate == null) return false;
        return LocalDate.now().isAfter(this.expiryDate);
    }

    public boolean isNearExpiry(int daysThreshold) {
        if (this.expiryDate == null || isExpired()) return false;
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), this.expiryDate);
        return daysUntil >= 0 && daysUntil <= daysThreshold;
    }

    public long getDaysUntilExpiry() {
        if (this.expiryDate == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), this.expiryDate);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
