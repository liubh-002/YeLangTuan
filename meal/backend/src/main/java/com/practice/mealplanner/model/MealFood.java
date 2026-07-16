package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "meal_food")
public class MealFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "week_day", length = 10)
    private String weekDay;

    @Column(name = "meal_type", length = 10)
    private String mealType;

    @Column(name = "dish_name", length = 100)
    private String dishName;

    @Column(name = "need_food_list", columnDefinition = "TEXT")
    private String needFoodList;

    @Column(name = "is_edit")
    private Boolean isEdit = false;
}
