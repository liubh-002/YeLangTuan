package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.WeekBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeekBillRepository extends JpaRepository<WeekBill, Long> {
    List<WeekBill> findAllByOrderByRecordDateDesc();
}
