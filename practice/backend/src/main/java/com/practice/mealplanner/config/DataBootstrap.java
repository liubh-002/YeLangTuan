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
        if (foodStockRepository.count() == 0) {
            List<FoodStock> defaults = Arrays.asList(
                createStock("鸡蛋", "个", 30),
                createStock("大米", "斤", 10),
                createStock("小米", "斤", 5),
                createStock("面粉", "斤", 5),
                createStock("牛奶", "盒", 12),
                createStock("猪肉", "斤", 3),
                createStock("鸡胸肉", "斤", 2),
                createStock("番茄", "斤", 3),
                createStock("土豆", "斤", 5),
                createStock("白菜", "斤", 4),
                createStock("胡萝卜", "斤", 3),
                createStock("黄瓜", "斤", 2),
                createStock("面条", "斤", 3),
                createStock("包子", "个", 10),
                createStock("馒头", "个", 12),
                createStock("豆腐", "块", 4)
            );
            foodStockRepository.saveAll(defaults);
        }
    }

    private FoodStock createStock(String name, String unit, int num) {
        FoodStock stock = new FoodStock();
        stock.setFoodName(name);
        stock.setUnit(unit);
        stock.setStockNum(BigDecimal.valueOf(num));
        return stock;
    }
}
