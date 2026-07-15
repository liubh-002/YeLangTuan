package com.practice.mealplanner.dto;

import lombok.Data;

import java.util.List;

@Data
public class FamilyGroupResponse {
    private Long id;
    private String inviteCode;
    private List<FamilyMemberResponse> members;
}