package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "purchase_record")
public class PurchaseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "food_name", length = 100)
    private String foodName;

    @Column(name = "need_num", precision = 10, scale = 2)
    private BigDecimal needNum;

    @Column(length = 20)
    private String status;

    @Column(name = "user_id")
    private Long userId;
}
