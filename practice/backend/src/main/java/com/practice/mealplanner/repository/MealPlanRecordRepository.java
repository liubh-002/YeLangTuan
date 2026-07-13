package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.MealPlanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPlanRecordRepository extends JpaRepository<MealPlanRecord, Long> {
    List<MealPlanRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<MealPlanRecord> findByUserIdAndPlanType(Long userId, String planType);
}