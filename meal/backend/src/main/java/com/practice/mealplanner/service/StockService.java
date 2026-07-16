package com.practice.mealplanner.service;

import com.practice.mealplanner.model.FoodStock;
import com.practice.mealplanner.repository.FoodStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final FoodStockRepository foodStockRepository;

    public List<FoodStock> getAllStocks(Long userId) {
        return foodStockRepository.findByUserId(userId);
    }

    public FoodStock addStock(String foodName, String unit, BigDecimal stockNum, Long userId) {
        FoodStock stock = new FoodStock();
        stock.setFoodName(foodName);
        stock.setUnit(unit);
        stock.setStockNum(stockNum);
        stock.setUserId(userId);
        return foodStockRepository.save(stock);
    }

    public FoodStock updateStock(Long id, String foodName, String unit, BigDecimal stockNum, Long userId) {
        FoodStock stock = foodStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("库存食材不存在"));
        if (stock.getUserId() == null || !stock.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此库存");
        }
        if (foodName != null) stock.setFoodName(foodName);
        if (unit != null) stock.setUnit(unit);
        if (stockNum != null) stock.setStockNum(stockNum);
        return foodStockRepository.save(stock);
    }

    @Transactional
    public void deductStock(String foodName, BigDecimal quantity, Long userId) {
        FoodStock stock = foodStockRepository.findByFoodNameAndUserId(foodName, userId)
                .orElseThrow(() -> new RuntimeException("库存中未找到: " + foodName));
        BigDecimal newNum = stock.getStockNum().subtract(quantity);
        if (newNum.compareTo(BigDecimal.ZERO) < 0) {
            newNum = BigDecimal.ZERO;
        }
        stock.setStockNum(newNum);
        foodStockRepository.save(stock);
    }

    @Transactional
    public void restoreStock(String foodName, BigDecimal quantity, Long userId) {
        FoodStock stock = foodStockRepository.findByFoodNameAndUserId(foodName, userId)
                .orElse(null);
        if (stock == null) {
            stock = new FoodStock();
            stock.setFoodName(foodName);
            stock.setUnit("斤");
            stock.setStockNum(quantity);
            stock.setUserId(userId);
        } else {
            stock.setStockNum(stock.getStockNum().add(quantity));
        }
        foodStockRepository.save(stock);
    }

    public void deleteStock(Long id, Long userId) {
        FoodStock stock = foodStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("库存食材不存在"));
        if (stock.getUserId() == null || !stock.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此库存");
        }
        foodStockRepository.deleteById(id);
    }
}
