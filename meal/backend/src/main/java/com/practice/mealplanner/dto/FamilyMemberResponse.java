package com.practice.mealplanner.dto;

import lombok.Data;

@Data
public class FamilyMemberResponse {
    private Long id;
    private String name;
    private Integer age;
    private String personTag;
    private Integer appetite;
    private String dietTaboo;
    private String dietaryType;
}