package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.WeekBill;
import com.practice.mealplanner.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<List<WeekBill>> getAll() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    @PostMapping
    public ResponseEntity<WeekBill> create(@RequestBody Map<String, Object> body) {
        BigDecimal weekCost = new BigDecimal(body.get("weekCost").toString());
        BigDecimal weekBudget = new BigDecimal(body.get("weekBudget").toString());
        BigDecimal monthSalary = body.get("monthSalary") != null ?
                new BigDecimal(body.get("monthSalary").toString()) : BigDecimal.ZERO;
        return ResponseEntity.ok(billService.recordWeekBill(weekCost, weekBudget, monthSalary));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WeekBill> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal weekCost = new BigDecimal(body.get("weekCost").toString());
        BigDecimal weekBudget = new BigDecimal(body.get("weekBudget").toString());
        return ResponseEntity.ok(billService.updateBill(id, weekCost, weekBudget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok().build();
    }
}
