package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    @Query("SELECT m FROM MealPlan m WHERE m.userOrFamilyId = :userOrFamilyId ORDER BY m.createTime DESC")
    List<MealPlan> findByUserOrFamilyIdOrderByCreateTimeDesc(Long userOrFamilyId);
}
