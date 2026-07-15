package com.practice.mealplanner.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String name;
    private String phone;
    private String gender;
    private Integer age;
    private String tastePrefer;
    private String dietTaboo;
    private String token;
}