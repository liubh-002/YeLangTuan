package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String phone;

    @Column(nullable = false)
    private String password;

    private String nickname;
    
    private String name;
    
    private String gender;
    
    private Integer age;

    @Column(name = "month_salary", precision = 10, scale = 2)
    private BigDecimal monthSalary;

    @Column(name = "taste_prefer", columnDefinition = "TEXT")
    private String tastePrefer;

    @Column(name = "diet_taboo", columnDefinition = "TEXT")
    private String dietTaboo;

    @Column(name = "family_id")
    private Long familyId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
