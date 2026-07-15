package com.practice.mealplanner.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class FamilyMemberRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "年龄不能为空")
    private Integer age;

    @NotBlank(message = "人群标签不能为空")
    @Pattern(regexp = "^(青年人|老年人|青少年|病号|减肥|孕妇|普通)$", 
             message = "人群标签只能是：青年人、老年人、青少年、病号、减肥、孕妇、普通")
    private String personTag;

    @NotNull(message = "饭量不能为空")
    private Integer appetite;

    private String dietTaboo = "无";

    private String dietaryType = "普通饮食";
}