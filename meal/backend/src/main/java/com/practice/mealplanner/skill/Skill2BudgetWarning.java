package com.practice.mealplanner.skill;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class Skill2BudgetWarning implements Skill {

    @Override
    public String execute(Map<String, Object> params) {
        BigDecimal weeklyCost = new BigDecimal(params.get("weeklyCost").toString());
        BigDecimal weeklyBudget = new BigDecimal(params.get("weeklyBudget").toString());
        BigDecimal monthlySalary = params.get("monthlySalary") != null ?
                new BigDecimal(params.get("monthlySalary").toString()) : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<String, Object>();
        BigDecimal rate = weeklyCost.divide(weeklyBudget, 4, java.math.RoundingMode.HALF_UP);

        if (rate.compareTo(BigDecimal.ONE) > 0) {
            result.put("warningLevel", "HEAVY");
            result.put("warningMessage", "食材花销已超预算，建议调整菜品或替换平价食材");
            result.put("optimizationNeeded", true);
        } else if (rate.compareTo(new BigDecimal("0.8")) >= 0) {
            result.put("warningLevel", "LIGHT");
            result.put("warningMessage", "食材花销接近预算线，建议关注采购量");
            result.put("optimizationNeeded", false);
        } else {
            result.put("warningLevel", "NONE");
            result.put("warningMessage", "预算健康，当前菜谱可直接采购");
            result.put("optimizationNeeded", false);
        }

        if (monthlySalary.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyFoodCost = weeklyCost.multiply(new BigDecimal("4.3"));
            BigDecimal salaryRate = monthlyFoodCost.divide(monthlySalary, 4, java.math.RoundingMode.HALF_UP);
            result.put("salaryUsageRate", salaryRate);
        }

        return "{\"warningLevel\":\"" + result.get("warningLevel") + "\"," +
                "\"warningMessage\":\"" + result.get("warningMessage") + "\"," +
                "\"optimizationNeeded\":" + result.get("optimizationNeeded") + "}";
    }
}