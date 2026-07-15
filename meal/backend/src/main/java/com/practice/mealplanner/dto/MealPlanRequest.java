package com.practice.mealplanner.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class MealPlanRequest {
    @NotNull(message = "人数不能为空")
    @Min(value = 1, message = "人数至少为1")
    private Integer peopleCount;

    private String taste = "清淡";

    @NotNull(message = "每周预算不能为空")
    @DecimalMin(value = "0.01", message = "预算必须大于0")
    private BigDecimal weeklyBudget = new BigDecimal("500");

    private BigDecimal monthlySalary;

    private List<String> avoidIngredients = new ArrayList<String>();

    private List<String> favoriteDishes = new ArrayList<String>();

    private List<String> breakfastWant = new ArrayList<String>();

    private List<String> lunchWant = new ArrayList<String>();

    private List<String> dinnerWant = new ArrayList<String>();

    private String customRequirements;

    private boolean savingMode = false;
}