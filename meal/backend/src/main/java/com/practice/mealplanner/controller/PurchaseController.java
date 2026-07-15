package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.PurchaseRecord;
import com.practice.mealplanner.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<PurchaseRecord>> getByPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(purchaseService.getRecordsByPlan(planId));
    }

    @PostMapping("/plan/{planId}")
    public ResponseEntity<PurchaseRecord> add(@PathVariable Long planId, @RequestBody Map<String, Object> body) {
        String foodName = (String) body.get("foodName");
        BigDecimal needNum = new BigDecimal(body.get("needNum").toString());
        return ResponseEntity.ok(purchaseService.addRecord(planId, foodName, needNum));
    }

    @PostMapping("/{recordId}/purchased")
    public ResponseEntity<Void> markPurchased(@PathVariable Long recordId) {
        purchaseService.markAsPurchased(recordId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{recordId}/unpurchased")
    public ResponseEntity<Void> markUnpurchased(@PathVariable Long recordId) {
        purchaseService.markAsUnpurchased(recordId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseRecord>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(purchaseService.getByStatus(status));
    }



    @PutMapping("/{recordId}/quantity")
    public ResponseEntity<Void> updateQuantity(@PathVariable Long recordId, @RequestBody java.util.Map<String, Object> body) {
        java.math.BigDecimal quantity = new java.math.BigDecimal(body.get("quantity").toString());
        purchaseService.updateQuantity(recordId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-from-ingredients")
    public ResponseEntity<Void> batchFromIngredients(@RequestBody java.util.Map<String, Object> body) {
        Long planId = body.get("planId") != null ? Long.valueOf(body.get("planId").toString()) : 0L;
        java.util.List<String> ingredients = (java.util.List<String>) body.get("ingredients");
        purchaseService.batchAddFromIngredients(planId, ingredients);
        return ResponseEntity.ok().build();
    }

}