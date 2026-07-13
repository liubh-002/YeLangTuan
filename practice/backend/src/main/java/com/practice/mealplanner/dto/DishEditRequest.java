package com.practice.mealplanner.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DishEditRequest {

    private Long planId;
    private String day;
    private String mealType;
    private String originalDishName;
    private String newDishName;
    private List<String> newIngredients;
    private BigDecimal newCost;
}