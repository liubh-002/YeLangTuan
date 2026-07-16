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

    public List<PurchaseRecord> getRecordsByPlan(Long planId, Long userId) {
        return purchaseRecordRepository.findByPlanIdAndUserId(planId, userId);
    }

    public PurchaseRecord addRecord(Long planId, String foodName, BigDecimal needNum, Long userId, String unit) {
        PurchaseRecord record = new PurchaseRecord();
        record.setPlanId(planId);
        record.setFoodName(foodName);
        record.setNeedNum(needNum);
        record.setStatus("待采购");
        record.setUnit(unit != null ? unit : "斤");
        record.setUserId(userId);
        return purchaseRecordRepository.save(record);
    }

    @Transactional
    public void markAsPurchased(Long recordId, Long userId) {
        PurchaseRecord record = purchaseRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        if (record.getUserId() == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }
        record.setStatus("已采购");
        purchaseRecordRepository.save(record);

        // Auto-add to stock
        FoodStock stock = foodStockRepository.findByFoodNameAndUserId(record.getFoodName(), userId)
                .orElseGet(() -> {
                    FoodStock newStock = new FoodStock();
                    newStock.setFoodName(record.getFoodName());
                    newStock.setUnit("斤");
                    newStock.setStockNum(BigDecimal.ZERO);
                    newStock.setUserId(userId);
                    return newStock;
                });
        stock.setStockNum(stock.getStockNum().add(record.getNeedNum()));
        foodStockRepository.save(stock);
    }

    @Transactional
    public void markAsUnpurchased(Long recordId, Long userId) {
        PurchaseRecord record = purchaseRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        if (record.getUserId() == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }
        record.setStatus("待采购");
        purchaseRecordRepository.save(record);

        // Deduct from stock
        FoodStock stock = foodStockRepository.findByFoodNameAndUserId(record.getFoodName(), userId).orElse(null);
        if (stock != null) {
            stock.setStockNum(stock.getStockNum().subtract(record.getNeedNum()));
            foodStockRepository.save(stock);
        }
    }

    @Transactional
    public void updateUnit(Long recordId, String unit, Long userId) {
        PurchaseRecord record = purchaseRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        if (record.getUserId() == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }
        record.setUnit(unit);
        purchaseRecordRepository.save(record);
    }

    @Transactional
    public void updateQuantity(Long recordId, BigDecimal quantity, Long userId) {
        PurchaseRecord record = purchaseRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        if (record.getUserId() == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }
        record.setNeedNum(quantity);
        purchaseRecordRepository.save(record);
    }

    @Transactional
    public void batchAddFromIngredients(Long planId, List<String> ingredientNames, Long userId) {
        for (String name : ingredientNames) {
            boolean existsInStock = foodStockRepository.existsByFoodNameAndUserId(name, userId);
            if (!existsInStock) {
                PurchaseRecord record = new PurchaseRecord();
                record.setPlanId(planId);
                record.setFoodName(name);
                record.setNeedNum(new BigDecimal("1"));
                record.setStatus("待采购");
                record.setUserId(userId);
                purchaseRecordRepository.save(record);
            }
        }
    }

    public List<PurchaseRecord> getByStatus(String status, Long userId) {
        return purchaseRecordRepository.findByStatusAndUserId(status, userId);
    }
}
