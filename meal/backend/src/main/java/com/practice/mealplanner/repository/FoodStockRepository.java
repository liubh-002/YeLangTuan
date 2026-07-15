package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.FoodStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodStockRepository extends JpaRepository<FoodStock, Long> {
    Optional<FoodStock> findByFoodName(String foodName);
    boolean existsByFoodName(String foodName);
}
