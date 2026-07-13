package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.MealFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealFoodRepository extends JpaRepository<MealFood, Long> {
    List<MealFood> findByPlanId(Long planId);
    void deleteByPlanId(Long planId);
}
