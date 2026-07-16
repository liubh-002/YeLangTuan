package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "family_member")
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    private Integer age;

    @Column(name = "crowd", length = 20)
    private String crowd;

    @Column(name = "diet_taboo", columnDefinition = "TEXT")
    private String dietTaboo;

    @Column(name = "food_volume", length = 20)
    private String foodVolume;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "sub_account_user_id")
    private Long subAccountUserId;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String status;

    @Column(name = "is_sub_account")
    private Boolean isSubAccount = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
