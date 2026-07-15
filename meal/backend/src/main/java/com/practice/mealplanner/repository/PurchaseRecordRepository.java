package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, Long> {
    List<PurchaseRecord> findByPlanId(Long planId);
    List<PurchaseRecord> findByStatus(String status);
}
