package com.pharmacy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Sale implements Serializable {
    private String id;
    private String invoiceNumber;
    private String medicineId;
    private String medicineName;
    private int quantitySold;
    private BigDecimal purchasePricePerUnit;
    private BigDecimal sellingPricePerUnit;
    private BigDecimal totalRevenue;
    private BigDecimal cogs;
    private BigDecimal grossProfit;
    private LocalDateTime saleDate;
    private String paymentMode; // CASH, UPI, CARD, CREDIT
    private String borrowerId; // if credit sale
    private String customerName;

    public Sale() {
        this.saleDate = LocalDateTime.now();
    }

    public Sale(String id, String invoiceNumber, String medicineId, String medicineName,
                int quantitySold, BigDecimal purchasePricePerUnit, BigDecimal sellingPricePerUnit,
                LocalDateTime saleDate, String paymentMode, String borrowerId, String customerName) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantitySold = quantitySold;
        this.purchasePricePerUnit = purchasePricePerUnit;
        this.sellingPricePerUnit = sellingPricePerUnit;
        this.saleDate = saleDate != null ? saleDate : LocalDateTime.now();
        this.paymentMode = paymentMode;
        this.borrowerId = borrowerId;
        this.customerName = customerName;

        calculateFinancials();
    }

    public void calculateFinancials() {
        BigDecimal qty = BigDecimal.valueOf(this.quantitySold);
        if (this.sellingPricePerUnit != null) {
            this.totalRevenue = this.sellingPricePerUnit.multiply(qty);
        } else {
            this.totalRevenue = BigDecimal.ZERO;
        }

        if (this.purchasePricePerUnit != null) {
            this.cogs = this.purchasePricePerUnit.multiply(qty);
        } else {
            this.cogs = BigDecimal.ZERO;
        }

        this.grossProfit = this.totalRevenue.subtract(this.cogs);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
        calculateFinancials();
    }

    public BigDecimal getPurchasePricePerUnit() {
        return purchasePricePerUnit;
    }

    public void setPurchasePricePerUnit(BigDecimal purchasePricePerUnit) {
        this.purchasePricePerUnit = purchasePricePerUnit;
        calculateFinancials();
    }

    public BigDecimal getSellingPricePerUnit() {
        return sellingPricePerUnit;
    }

    public void setSellingPricePerUnit(BigDecimal sellingPricePerUnit) {
        this.sellingPricePerUnit = sellingPricePerUnit;
        calculateFinancials();
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getCogs() {
        return cogs;
    }

    public void setCogs(BigDecimal cogs) {
        this.cogs = cogs;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
