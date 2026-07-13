package com.practice.mealplanner.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OptimizationResult {
    private BigDecimal newWeeklyCost;
    private BigDecimal budgetRate;
    private boolean optimizationNeeded;
    private String message;
    private List<String> suggestedReplacements;
}