package com.practice.mealplanner.dto;

import lombok.Data;

import java.util.List;

@Data
public class FamilyMemberResponse {
    private Long id;
    private String name;
    private Integer age;
    private String personTag;
    private Integer appetite;
    private String dietTaboo;
    private String dietaryType;
    private Long subAccountUserId;
    private String phone;
    private String status;
    private Boolean isSubAccount;
}
