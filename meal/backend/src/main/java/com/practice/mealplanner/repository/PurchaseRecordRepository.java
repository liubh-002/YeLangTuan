package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, Long> {
    List<PurchaseRecord> findByPlanIdAndUserId(Long planId, Long userId);
    List<PurchaseRecord> findByPlanId(Long planId);
    List<PurchaseRecord> findByStatusAndUserId(String status, Long userId);
    List<PurchaseRecord> findByUserId(Long userId);
    int deleteByUserIdIsNull();
    void deleteByUserId(Long userId);
}
