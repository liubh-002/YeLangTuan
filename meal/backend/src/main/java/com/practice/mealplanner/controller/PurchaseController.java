package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.PurchaseRecord;
import com.practice.mealplanner.model.User;
import com.practice.mealplanner.service.AuthService;
import com.practice.mealplanner.service.PurchaseService;
import com.practice.mealplanner.repository.PurchaseRecordRepository;
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
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseRecordRepository purchaseRecordRepository;
    private final AuthService authService;

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<PurchaseRecord>> getByPlan(HttpServletRequest request, @PathVariable Long planId) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(purchaseService.getRecordsByPlan(planId, userId));
    }

    @PostMapping("/plan/{planId}")
    public ResponseEntity<PurchaseRecord> add(HttpServletRequest request, @PathVariable Long planId, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        String foodName = (String) body.get("foodName");
        BigDecimal needNum = new BigDecimal(body.get("needNum").toString());
        return ResponseEntity.ok(purchaseService.addRecord(planId, foodName, needNum, userId, body.get("unit") != null ? body.get("unit").toString() : null));
    }

    @PostMapping("/{recordId}/purchased")
    public ResponseEntity<Void> markPurchased(HttpServletRequest request, @PathVariable Long recordId) {
        Long userId = getUserId(request);
        purchaseService.markAsPurchased(recordId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{recordId}/unpurchased")
    public ResponseEntity<Void> markUnpurchased(HttpServletRequest request, @PathVariable Long recordId) {
        Long userId = getUserId(request);
        purchaseService.markAsUnpurchased(recordId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseRecord>> getByStatus(HttpServletRequest request, @PathVariable String status) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(purchaseService.getByStatus(status, userId));
    }

    @PutMapping("/{recordId}/quantity")
    public ResponseEntity<Void> updateUnit(HttpServletRequest request, @PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        String unit = (String) body.get("unit");
        purchaseService.updateUnit(recordId, unit, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{recordId}/update-quantity")
    public ResponseEntity<Void> updateQuantity(HttpServletRequest request, @PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        purchaseService.updateQuantity(recordId, quantity, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all")
    @Transactional
    public ResponseEntity<Void> deleteAll(HttpServletRequest request) {
        Long userId = getUserId(request);
        purchaseRecordRepository.deleteByUserId(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-from-ingredients")
    public ResponseEntity<Void> batchFromIngredients(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        Long planId = body.get("planId") != null ? Long.valueOf(body.get("planId").toString()) : 0L;
        List<String> ingredients = (List<String>) body.get("ingredients");
        purchaseService.batchAddFromIngredients(planId, ingredients, userId);
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
