package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.FoodStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodStockRepository extends JpaRepository<FoodStock, Long> {
    Optional<FoodStock> findByFoodNameAndUserId(String foodName, Long userId);
    boolean existsByFoodNameAndUserId(String foodName, Long userId);
    List<FoodStock> findByUserId(Long userId);
    int deleteByUserIdIsNull();
    void deleteByUserId(Long userId);
}
