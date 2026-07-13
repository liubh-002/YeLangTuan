package com.practice.mealplanner.service;

import com.practice.mealplanner.model.WeekBill;
import com.practice.mealplanner.repository.WeekBillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final WeekBillRepository weekBillRepository;

    public WeekBill recordWeekBill(BigDecimal weekCost, BigDecimal weekBudget, BigDecimal monthSalary) {
        WeekBill bill = new WeekBill();
        LocalDate now = LocalDate.now();
        int weekOfYear = now.get(java.time.temporal.WeekFields.of(java.util.Locale.CHINA).weekOfYear());
        bill.setWeekCycle(now.getYear() + "-W" + String.format("%02d", weekOfYear));
        bill.setWeekCost(weekCost);
        bill.setWeekBudget(weekBudget);
        bill.setMonthSalary(monthSalary);
        bill.setOverFlag(weekCost.compareTo(weekBudget) > 0);
        bill.setRecordDate(now);
        return weekBillRepository.save(bill);
    }

    public List<WeekBill> getAllBills() {
        return weekBillRepository.findAllByOrderByRecordDateDesc();
    }

    public WeekBill updateBill(Long id, BigDecimal weekCost, BigDecimal weekBudget) {
        WeekBill bill = weekBillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账单记录不存在"));
        bill.setWeekCost(weekCost);
        bill.setWeekBudget(weekBudget);
        bill.setOverFlag(weekCost.compareTo(weekBudget) > 0);
        return weekBillRepository.save(bill);
    }

    public void deleteBill(Long id) {
        weekBillRepository.deleteById(id);
    }
}
