package com.pharmacy.controller;

import com.pharmacy.model.ProfitLossSummary;
import com.pharmacy.model.RestockRecommendation;
import com.pharmacy.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/pnl")
    public ProfitLossSummary getProfitLossSummary(@RequestParam(required = false) Integer days) {
        return analyticsService.calculateProfitLoss(days);
    }

    @GetMapping("/restock-estimates")
    public List<RestockRecommendation> getRestockEstimates(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.calculateRestockEstimates(days);
    }
}
