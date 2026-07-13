package com.practice.mealplanner.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class MealPlanResponse {
    private List<DailyPlan> days = new ArrayList<DailyPlan>();
    private BigDecimal weeklyCost;
    private BigDecimal weeklyBudget;
    private BigDecimal monthlySalary;
    private BigDecimal monthlyFoodCost;
    private BigDecimal budgetUsageRate;
    private BigDecimal salaryUsageRate;
    private String taste;
    private Integer peopleCount;
    private List<String> favoriteDishes;
    private String warningLevel;
    private String warningMessage;
    private boolean optimizationNeeded;
    private boolean optimized;

    @Data
    public static class DailyPlan {
        private String day;
        private String adaptedTag;
        private List<MealItem> meals = new ArrayList<MealItem>();
        private BigDecimal dailyCost;
    }

    @Data
    public static class MealItem {
        private String mealType;
        private String dishName;
        private List<String> ingredients = new ArrayList<String>();
        private BigDecimal estimatedCost;
        private String nutritionNote;
        private String savingNote;
    }
}