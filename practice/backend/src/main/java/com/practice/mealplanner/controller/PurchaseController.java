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
}
