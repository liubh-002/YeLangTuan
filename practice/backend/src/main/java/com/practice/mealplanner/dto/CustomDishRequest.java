package com.practice.mealplanner.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CustomDishRequest {

    private Long userId;
    private Long familyId;
    private String dishName;
    private List<String> ingredients;
    private BigDecimal estimatedCost;
    private String day;
    private String mealType;
}