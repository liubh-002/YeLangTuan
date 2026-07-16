package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.User;
import com.practice.mealplanner.model.WeekBill;
import com.practice.mealplanner.service.AuthService;
import com.practice.mealplanner.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<WeekBill>> getAll(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(billService.getAllBills(userId));
    }

    @PostMapping
    public ResponseEntity<WeekBill> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        BigDecimal weekCost = new BigDecimal(body.get("weekCost").toString());
        BigDecimal weekBudget = new BigDecimal(body.get("weekBudget").toString());
        BigDecimal monthSalary = body.get("monthSalary") != null ?
                new BigDecimal(body.get("monthSalary").toString()) : BigDecimal.ZERO;
        return ResponseEntity.ok(billService.recordWeekBill(weekCost, weekBudget, monthSalary, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WeekBill> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        BigDecimal weekCost = new BigDecimal(body.get("weekCost").toString());
        BigDecimal weekBudget = new BigDecimal(body.get("weekBudget").toString());
        return ResponseEntity.ok(billService.updateBill(id, weekCost, weekBudget, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getUserId(request);
        billService.deleteBill(id, userId);
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
