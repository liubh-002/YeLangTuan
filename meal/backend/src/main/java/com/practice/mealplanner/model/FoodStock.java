package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "food_stock")
public class FoodStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;

    @Column(length = 20)
    private String unit;

    @Column(name = "stock_num", precision = 10, scale = 2)
    private BigDecimal stockNum;

    @Column(name = "user_id")
    private Long userId;
}
