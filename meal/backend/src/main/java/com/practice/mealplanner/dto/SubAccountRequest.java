package com.practice.mealplanner.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SubAccountRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String name;
    private Integer age;
    private String personTag = "普通";
    private String dietTaboo = "无";
    private Integer appetite = 3;
}
