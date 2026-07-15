package com.practice.mealplanner.service;

import com.practice.mealplanner.model.FoodStock;
import com.practice.mealplanner.model.PurchaseRecord;
import com.practice.mealplanner.repository.FoodStockRepository;
import com.practice.mealplanner.repository.PurchaseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final FoodStockRepository foodStockRepository;

    public List<PurchaseRecord> getRecordsByPlan(Long planId) {
        return purchaseRecordRepository.findByPlanId(planId);
    }

    public PurchaseRecord addRecord(Long planId, String foodName, BigDecimal needNum) {
        PurchaseRecord record = new PurchaseRecord();
        record.setPlanId(planId);
        record.setFoodName(foodName);
        record.setNeedNum(needNum);
        record.setStatus("待采购");
        return purchaseRecordRepository.save(record);
    }

    @Transactional
    public void markAsPurchased(Long recordId) {
        PurchaseRecord record = purchaseRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        record.setStatus("已采购");
        purchaseRecordRepository.save(record);

        // Auto-add to stock
        FoodStock stock = foodStockRepository.findByFoodName(record.getFoodName())
                .orElseGet(() -> {
                    FoodStock newStock = new FoodStock();
                    newStock.setFoodName(record.getFoodName());
                    newStock.setUnit("斤");
                    newStock.setStockNum(BigDecimal.ZERO);
                    return newStock;
                });
        stock.setStockNum(stock.getStockNum().add(record.getNeedNum()));
        foodStockRepository.save(stock);
    }

    @Transactional
    public void markAsUnpurchased(Long recordId) {
        PurchaseRecord record = purchaseRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        record.setStatus("待采购");
        purchaseRecordRepository.save(record);

        // Deduct from stock
        FoodStock stock = foodStockRepository.findByFoodName(record.getFoodName()).orElse(null);
        if (stock != null) {
            stock.setStockNum(stock.getStockNum().subtract(record.getNeedNum()));
            foodStockRepository.save(stock);
        }
    }

    

    @Transactional
    public void updateQuantity(Long recordId, java.math.BigDecimal quantity) {
        PurchaseRecord record = purchaseRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        record.setNeedNum(quantity);
        purchaseRecordRepository.save(record);
    }

    @Transactional
    public void batchAddFromIngredients(Long planId, java.util.List<String> ingredientNames) {
        for (String name : ingredientNames) {
            boolean existsInStock = foodStockRepository.existsByFoodName(name);
            if (!existsInStock) {
                PurchaseRecord record = new PurchaseRecord();
                record.setPlanId(planId);
                record.setFoodName(name);
                record.setNeedNum(new java.math.BigDecimal("1"));
                record.setStatus("待采购");
                purchaseRecordRepository.save(record);
            }
        }
    }

    public List<PurchaseRecord> getByStatus(String status) {
        return purchaseRecordRepository.findByStatus(status);
    }
}
