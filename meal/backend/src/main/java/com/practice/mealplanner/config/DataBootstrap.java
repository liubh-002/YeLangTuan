package com.practice.mealplanner.config;

import com.practice.mealplanner.repository.FoodStockRepository;
import com.practice.mealplanner.repository.PurchaseRecordRepository;
import com.practice.mealplanner.repository.WeekBillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataBootstrap implements CommandLineRunner {

    private final FoodStockRepository foodStockRepository;
    private final PurchaseRecordRepository purchaseRecordRepository;
    private final WeekBillRepository weekBillRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Clean up orphaned records that have no userId (from before the data isolation migration)
        // These records were created before the userId field was added and cannot be attributed to any user
        int deletedStock = foodStockRepository.deleteByUserIdIsNull();
        int deletedPurchases = purchaseRecordRepository.deleteByUserIdIsNull();
        int deletedBills = weekBillRepository.deleteByUserIdIsNull();
        if (deletedStock > 0 || deletedPurchases > 0 || deletedBills > 0) {
            System.out.println("DataBootstrap: Cleaned up orphaned records - stock=" + deletedStock +
                    ", purchases=" + deletedPurchases + ", bills=" + deletedBills);
        }
    }
}
