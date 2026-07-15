package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByCategory(String category);
    List<Ingredient> findByNameContaining(String name);
    java.util.Optional<Ingredient> findByName(String name);
    boolean existsByName(String name);
}