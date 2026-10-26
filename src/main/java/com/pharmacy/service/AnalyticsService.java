package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.ProfitLossSummary;
import com.pharmacy.model.RestockRecommendation;
import com.pharmacy.model.Sale;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final SaleRepository saleRepository;
    private final MedicineRepository medicineRepository;
    private final BorrowerService borrowerService;

    public AnalyticsService(SaleRepository saleRepository,
                            MedicineRepository medicineRepository,
                            BorrowerService borrowerService) {
        this.saleRepository = saleRepository;
        this.medicineRepository = medicineRepository;
        this.borrowerService = borrowerService;
    }

    public ProfitLossSummary calculateProfitLoss(Integer daysFilter) {
        List<Sale> sales;
        if (daysFilter != null && daysFilter > 0) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(daysFilter);
            sales = saleRepository.findSalesSince(cutoff);
        } else {
            sales = saleRepository.findAll();
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCogs = BigDecimal.ZERO;
        int totalUnitsSold = 0;

        for (Sale sale : sales) {
            totalRevenue = totalRevenue.add(sale.getTotalRevenue());
            totalCogs = totalCogs.add(sale.getCogs());
            totalUnitsSold += sale.getQuantitySold();
        }

        BigDecimal grossProfit = totalRevenue.subtract(totalCogs);
        BigDecimal pendingReceivables = borrowerService.getTotalOutstandingDebt();

        List<Medicine> allMedicines = medicineRepository.findAll();
        BigDecimal inventoryCost = BigDecimal.ZERO;
        BigDecimal inventoryRetail = BigDecimal.ZERO;
        int lowStockCount = 0;
        int nearExpiryCount = 0;
        int expiredCount = 0;

        for (Medicine m : allMedicines) {
            BigDecimal qty = BigDecimal.valueOf(m.getQuantityInStock());
            if (m.getPurchasePrice() != null) {
                inventoryCost = inventoryCost.add(m.getPurchasePrice().multiply(qty));
            }
            if (m.getSellingPrice() != null) {
                inventoryRetail = inventoryRetail.add(m.getSellingPrice().multiply(qty));
            }
            if (m.isLowStock()) {
                lowStockCount++;
            }
            if (m.isExpired()) {
                expiredCount++;
            } else if (m.isNearExpiry(60)) {
                nearExpiryCount++;
            }
        }

        ProfitLossSummary summary = new ProfitLossSummary();
        summary.setTotalRevenue(totalRevenue);
        summary.setTotalCogs(totalCogs);
        summary.setGrossProfit(grossProfit);
        summary.setTotalUnitsSold(totalUnitsSold);
        summary.setTotalTransactions(sales.size());
        summary.setPendingReceivables(pendingReceivables);
        summary.setInventoryValueAtCost(inventoryCost);
        summary.setInventoryValueAtRetail(inventoryRetail);
        summary.setTotalMedicinesCount(allMedicines.size());
        summary.setLowStockCount(lowStockCount);
        summary.setNearExpiryCount(nearExpiryCount);
        summary.setExpiredCount(expiredCount);
        summary.calculateMargin();

        return summary;
    }

    public List<RestockRecommendation> calculateRestockEstimates(int daysToAnalyze) {
        int nDays = daysToAnalyze > 0 ? daysToAnalyze : 30;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(nDays);
        List<Sale> periodSales = saleRepository.findSalesSince(cutoff);

        // Group sales quantity by medicine ID
        Map<String, Integer> salesByMedicine = periodSales.stream()
                .filter(s -> s.getMedicineId() != null)
                .collect(Collectors.groupingBy(
                        Sale::getMedicineId,
                        Collectors.summingInt(Sale::getQuantitySold)
                ));

        List<Medicine> medicines = medicineRepository.findAll();
        List<RestockRecommendation> recommendations = new ArrayList<>();

        for (Medicine medicine : medicines) {
            int totalSold = salesByMedicine.getOrDefault(medicine.getId(), 0);
            double avgDailySales = (double) totalSold / (double) nDays;
            double projected30DayDemand = avgDailySales * 30.0;
            double bufferStock = projected30DayDemand * 0.20; // 20% safety buffer

            // Recommended Purchase: max(0, Projected 30-Day Demand + Buffer (20%) - Current Stock)
            double rawRecommended = projected30DayDemand + bufferStock - medicine.getQuantityInStock();
            int recommendedPurchaseQty = Math.max(0, (int) Math.ceil(rawRecommended));

            BigDecimal unitCost = medicine.getPurchasePrice() != null ? medicine.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal estimatedCost = unitCost.multiply(BigDecimal.valueOf(recommendedPurchaseQty))
                    .setScale(2, RoundingMode.HALF_UP);

            String urgency;
            if (medicine.getQuantityInStock() == 0 && (projected30DayDemand > 0 || totalSold > 0)) {
                urgency = "CRITICAL_OUT_OF_STOCK";
            } else if (medicine.getQuantityInStock() < 10) {
                urgency = "LOW_STOCK";
            } else if (recommendedPurchaseQty > 0) {
                urgency = "REORDER_RECOMMENDED";
            } else {
                urgency = "HEALTHY_STOCK";
            }

            RestockRecommendation rec = new RestockRecommendation(
                    medicine.getId(),
                    medicine.getName(),
                    medicine.getGenericName(),
                    medicine.getQuantityInStock(),
                    totalSold,
                    nDays,
                    Math.round(avgDailySales * 100.0) / 100.0,
                    Math.round(projected30DayDemand * 10.0) / 10.0,
                    Math.round(bufferStock * 10.0) / 10.0,
                    recommendedPurchaseQty,
                    unitCost,
                    estimatedCost,
                    urgency
            );
            recommendations.add(rec);
        }

        // Sort by urgency: CRITICAL_OUT_OF_STOCK first, then LOW_STOCK, then REORDER_RECOMMENDED, then HEALTHY
        recommendations.sort((a, b) -> Integer.compare(getUrgencyRank(a.getUrgency()), getUrgencyRank(b.getUrgency())));

        return recommendations;
    }

    private int getUrgencyRank(String urgency) {
        return switch (urgency) {
            case "CRITICAL_OUT_OF_STOCK" -> 1;
            case "LOW_STOCK" -> 2;
            case "REORDER_RECOMMENDED" -> 3;
            default -> 4;
        };
    }
}
