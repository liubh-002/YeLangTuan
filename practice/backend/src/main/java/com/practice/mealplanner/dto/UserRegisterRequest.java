package com.practice.mealplanner.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String name;
    
    private String gender;
    
    private Integer age;
    
    private BigDecimal monthSalary;
    
    private String tastePrefer;
    
    private String dietTaboo;
}
