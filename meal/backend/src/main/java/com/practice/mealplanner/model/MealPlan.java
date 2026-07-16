package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "meal_plan")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_or_family_id", nullable = false)
    private Long userOrFamilyId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "week_budget", precision = 10, scale = 2)
    private BigDecimal weekBudget;

    @Column(name = "week_cost", precision = 10, scale = 2)
    private BigDecimal weekCost;

    @Column(length = 20)
    private String mode;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
