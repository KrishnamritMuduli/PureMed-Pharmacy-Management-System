package com.pharmacy.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class RestockRecommendation implements Serializable {
    private String medicineId;
    private String medicineName;
    private String genericName;
    private int currentStock;
    private int totalSoldInPeriod;
    private int periodDays;
    private double avgDailySales;
    private double projected30DayDemand;
    private double bufferStock;
    private int recommendedPurchaseQty;
    private BigDecimal unitPurchasePrice;
    private BigDecimal estimatedReorderCost;
    private String urgency; // CRITICAL_OUT_OF_STOCK, LOW_STOCK, REORDER_RECOMMENDED, HEALTHY_STOCK

    public RestockRecommendation() {}

    public RestockRecommendation(String medicineId, String medicineName, String genericName,
                                 int currentStock, int totalSoldInPeriod, int periodDays,
                                 double avgDailySales, double projected30DayDemand, double bufferStock,
                                 int recommendedPurchaseQty, BigDecimal unitPurchasePrice,
                                 BigDecimal estimatedReorderCost, String urgency) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.currentStock = currentStock;
        this.totalSoldInPeriod = totalSoldInPeriod;
        this.periodDays = periodDays;
        this.avgDailySales = avgDailySales;
        this.projected30DayDemand = projected30DayDemand;
        this.bufferStock = bufferStock;
        this.recommendedPurchaseQty = recommendedPurchaseQty;
        this.unitPurchasePrice = unitPurchasePrice;
        this.estimatedReorderCost = estimatedReorderCost;
        this.urgency = urgency;
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

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public int getTotalSoldInPeriod() {
        return totalSoldInPeriod;
    }

    public void setTotalSoldInPeriod(int totalSoldInPeriod) {
        this.totalSoldInPeriod = totalSoldInPeriod;
    }

    public int getPeriodDays() {
        return periodDays;
    }

    public void setPeriodDays(int periodDays) {
        this.periodDays = periodDays;
    }

    public double getAvgDailySales() {
        return avgDailySales;
    }

    public void setAvgDailySales(double avgDailySales) {
        this.avgDailySales = avgDailySales;
    }

    public double getProjected30DayDemand() {
        return projected30DayDemand;
    }

    public void setProjected30DayDemand(double projected30DayDemand) {
        this.projected30DayDemand = projected30DayDemand;
    }

    public double getBufferStock() {
        return bufferStock;
    }

    public void setBufferStock(double bufferStock) {
        this.bufferStock = bufferStock;
    }

    public int getRecommendedPurchaseQty() {
        return recommendedPurchaseQty;
    }

    public void setRecommendedPurchaseQty(int recommendedPurchaseQty) {
        this.recommendedPurchaseQty = recommendedPurchaseQty;
    }

    public BigDecimal getUnitPurchasePrice() {
        return unitPurchasePrice;
    }

    public void setUnitPurchasePrice(BigDecimal unitPurchasePrice) {
        this.unitPurchasePrice = unitPurchasePrice;
    }

    public BigDecimal getEstimatedReorderCost() {
        return estimatedReorderCost;
    }

    public void setEstimatedReorderCost(BigDecimal estimatedReorderCost) {
        this.estimatedReorderCost = estimatedReorderCost;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
}
