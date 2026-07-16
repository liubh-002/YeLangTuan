package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.FoodStock;
import com.practice.mealplanner.model.User;
import com.practice.mealplanner.service.AuthService;
import com.practice.mealplanner.service.StockService;
import com.practice.mealplanner.repository.FoodStockRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock")
public class IngredientController {

    private final StockService stockService;
    private final FoodStockRepository foodStockRepository;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<FoodStock>> getAll(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(stockService.getAllStocks(userId));
    }

    @PostMapping
    public ResponseEntity<FoodStock> add(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        String foodName = (String) body.get("foodName");
        String unit = (String) body.getOrDefault("unit", "斤");
        BigDecimal stockNum = new BigDecimal(body.get("stockNum").toString());
        return ResponseEntity.ok(stockService.addStock(foodName, unit, stockNum, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodStock> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        String foodName = (String) body.get("foodName");
        String unit = (String) body.get("unit");
        BigDecimal stockNum = body.get("stockNum") != null ? new BigDecimal(body.get("stockNum").toString()) : null;
        return ResponseEntity.ok(stockService.updateStock(id, foodName, unit, stockNum, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getUserId(request);
        stockService.deleteStock(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all")
    @Transactional
    public ResponseEntity<Void> deleteAll(HttpServletRequest request) {
        Long userId = getUserId(request);
        foodStockRepository.deleteByUserId(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deduct")
    public ResponseEntity<Void> deduct(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        String foodName = (String) body.get("foodName");
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        stockService.deductStock(foodName, quantity, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deduct-batch")
    public ResponseEntity<Void> deductBatch(HttpServletRequest request, @RequestBody List<Map<String, Object>> items) {
        Long userId = getUserId(request);
        for (Map<String, Object> item : items) {
            String foodName = (String) item.get("foodName");
            BigDecimal quantity = new BigDecimal(item.get("quantity").toString());
            try {
                stockService.deductStock(foodName, quantity, userId);
            } catch (Exception e) {
                // 如果食材在库存中不存在，跳过
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore-batch")
    public ResponseEntity<Void> restoreBatch(HttpServletRequest request, @RequestBody List<Map<String, Object>> items) {
        Long userId = getUserId(request);
        for (Map<String, Object> item : items) {
            String foodName = (String) item.get("foodName");
            BigDecimal quantity = new BigDecimal(item.get("quantity").toString());
            stockService.restoreStock(foodName, quantity, userId);
        }
        return ResponseEntity.ok().build();
    }

    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return user.getId();
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未提供认证令牌");
        }
        return authHeader.substring(7);
    }
}
