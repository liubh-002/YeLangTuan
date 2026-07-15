package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.FoodStock;
import com.practice.mealplanner.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock")
public class IngredientController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<FoodStock>> getAll() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @PostMapping
    public ResponseEntity<FoodStock> add(@RequestBody Map<String, Object> body) {
        String foodName = (String) body.get("foodName");
        String unit = (String) body.getOrDefault("unit", "斤");
        BigDecimal stockNum = new BigDecimal(body.get("stockNum").toString());
        return ResponseEntity.ok(stockService.addStock(foodName, unit, stockNum));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodStock> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String foodName = (String) body.get("foodName");
        String unit = (String) body.get("unit");
        BigDecimal stockNum = body.get("stockNum") != null ? new BigDecimal(body.get("stockNum").toString()) : null;
        return ResponseEntity.ok(stockService.updateStock(id, foodName, unit, stockNum));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deduct")
    public ResponseEntity<Void> deduct(@RequestBody Map<String, Object> body) {
        String foodName = (String) body.get("foodName");
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        stockService.deductStock(foodName, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deduct-batch")
    public ResponseEntity<Void> deductBatch(@RequestBody List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            String foodName = (String) item.get("foodName");
            BigDecimal quantity = new BigDecimal(item.get("quantity").toString());
            try {
                stockService.deductStock(foodName, quantity);
            } catch (Exception e) {
                // 如果食材在库存中不存在，跳过
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore-batch")
    public ResponseEntity<Void> restoreBatch(@RequestBody List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            String foodName = (String) item.get("foodName");
            BigDecimal quantity = new BigDecimal(item.get("quantity").toString());
            stockService.restoreStock(foodName, quantity);
        }
        return ResponseEntity.ok().build();
    }
}