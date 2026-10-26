package com.pharmacy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProfitLossSummary implements Serializable {
    private BigDecimal totalRevenue;
    private BigDecimal totalCogs;
    private BigDecimal grossProfit;
    private double grossMarginPercentage;
    private BigDecimal pendingReceivables;
    private int totalTransactions;
    private int totalUnitsSold;
    private BigDecimal inventoryValueAtCost;
    private BigDecimal inventoryValueAtRetail;
    private int totalMedicinesCount;
    private int lowStockCount;
    private int nearExpiryCount;
    private int expiredCount;

    public ProfitLossSummary() {
        this.totalRevenue = BigDecimal.ZERO;
        this.totalCogs = BigDecimal.ZERO;
        this.grossProfit = BigDecimal.ZERO;
        this.pendingReceivables = BigDecimal.ZERO;
        this.inventoryValueAtCost = BigDecimal.ZERO;
        this.inventoryValueAtRetail = BigDecimal.ZERO;
    }

    public void calculateMargin() {
        if (totalRevenue != null && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margin = grossProfit.multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP);
            this.grossMarginPercentage = margin.doubleValue();
        } else {
            this.grossMarginPercentage = 0.0;
        }
    }

    // Getters and Setters
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalCogs() {
        return totalCogs;
    }

    public void setTotalCogs(BigDecimal totalCogs) {
        this.totalCogs = totalCogs;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public double getGrossMarginPercentage() {
        return grossMarginPercentage;
    }

    public void setGrossMarginPercentage(double grossMarginPercentage) {
        this.grossMarginPercentage = grossMarginPercentage;
    }

    public BigDecimal getPendingReceivables() {
        return pendingReceivables;
    }

    public void setPendingReceivables(BigDecimal pendingReceivables) {
        this.pendingReceivables = pendingReceivables;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getTotalUnitsSold() {
        return totalUnitsSold;
    }

    public void setTotalUnitsSold(int totalUnitsSold) {
        this.totalUnitsSold = totalUnitsSold;
    }

    public BigDecimal getInventoryValueAtCost() {
        return inventoryValueAtCost;
    }

    public void setInventoryValueAtCost(BigDecimal inventoryValueAtCost) {
        this.inventoryValueAtCost = inventoryValueAtCost;
    }

    public BigDecimal getInventoryValueAtRetail() {
        return inventoryValueAtRetail;
    }

    public void setInventoryValueAtRetail(BigDecimal inventoryValueAtRetail) {
        this.inventoryValueAtRetail = inventoryValueAtRetail;
    }

    public int getTotalMedicinesCount() {
        return totalMedicinesCount;
    }

    public void setTotalMedicinesCount(int totalMedicinesCount) {
        this.totalMedicinesCount = totalMedicinesCount;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getNearExpiryCount() {
        return nearExpiryCount;
    }

    public void setNearExpiryCount(int nearExpiryCount) {
        this.nearExpiryCount = nearExpiryCount;
    }

    public int getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(int expiredCount) {
        this.expiredCount = expiredCount;
    }
}
