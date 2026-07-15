package com.practice.mealplanner.config;

import com.practice.mealplanner.model.FoodStock;
import com.practice.mealplanner.repository.FoodStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataBootstrap implements CommandLineRunner {

    private final FoodStockRepository foodStockRepository;

    @Override
    public void run(String... args) {
        // Default stock data removed - new users start with empty stock
    }

    private FoodStock createStock(String name, String unit, int num) {
        FoodStock stock = new FoodStock();
        stock.setFoodName(name);
        stock.setUnit(unit);
        stock.setStockNum(BigDecimal.valueOf(num));
        return stock;
    }
}
