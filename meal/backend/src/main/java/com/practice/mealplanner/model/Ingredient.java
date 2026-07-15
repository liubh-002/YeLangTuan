package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "ingredients", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "price_per_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKg;

    private String unit;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String tags;

    private String season;

    @Column(columnDefinition = "TEXT")
    private String nutritionInfo;

    @Column(columnDefinition = "TEXT")
    private String source;

    @Column(name = "update_time")
    private String updateTime;

    @Column(name = "is_available")
    private Boolean isAvailable = true;
}