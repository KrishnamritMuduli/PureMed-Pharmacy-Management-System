package com.pharmacy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Borrower implements Serializable {
    private String id;
    private String customerName;
    private String phoneNumber;
    private String email;
    private String address;
    private BigDecimal outstandingBalance;
    private LocalDateTime createdAt;

    public Borrower() {
        this.outstandingBalance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public Borrower(String id, String customerName, String phoneNumber, String email, String address, BigDecimal initialBalance) {
        this.id = id;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.outstandingBalance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public synchronized void addDebt(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.outstandingBalance = this.outstandingBalance.add(amount);
        }
    }

    public synchronized void reduceDebt(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.outstandingBalance = this.outstandingBalance.subtract(amount);
            if (this.outstandingBalance.compareTo(BigDecimal.ZERO) < 0) {
                // Keep zero or slight credit if overpaid, but typically zero
                this.outstandingBalance = BigDecimal.ZERO;
            }
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
