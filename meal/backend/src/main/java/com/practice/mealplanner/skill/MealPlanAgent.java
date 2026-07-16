package com.practice.mealplanner.skill;

import com.practice.mealplanner.dto.FamilyMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MealPlanAgent {

    private final Skill1BaseMealPlan skill1;
    private final Skill2BudgetWarning skill2;
    private final Skill3RagPriceLookup skill3;
    private final Skill4MultiMemberAdaptation skill4;
    private final Skill5CustomDishBudgetRecalc skill5;

    public String generatePersonalPlan(int peopleCount, String taste, BigDecimal weeklyBudget,
                                       BigDecimal monthlySalary, List<String> avoidIngredients,
                                       List<String> favoriteDishes, List<String> breakfastWant,
                                       List<String> lunchWant, List<String> dinnerWant, String customRequirements, boolean savingMode) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("peopleCount", peopleCount);
        params.put("taste", taste);
        params.put("avoidIngredients", avoidIngredients);
        params.put("favoriteDishes", favoriteDishes);
        params.put("breakfastWant", breakfastWant != null ? breakfastWant : new ArrayList<String>());
        params.put("lunchWant", lunchWant != null ? lunchWant : new ArrayList<String>());
        params.put("dinnerWant", dinnerWant != null ? dinnerWant : new ArrayList<String>());
        params.put("customRequirements", customRequirements);
        params.put("savingMode", savingMode);
        params.put("weeklyBudget", weeklyBudget);

        String planJson = skill1.execute(params);

        BigDecimal weeklyCost = extractCost(planJson);
        Map<String, Object> warningParams = new HashMap<String, Object>();
        warningParams.put("weeklyCost", weeklyCost);
        warningParams.put("weeklyBudget", weeklyBudget);
        warningParams.put("monthlySalary", monthlySalary);
        String warningJson = skill2.execute(warningParams);

        return mergeJson(planJson, warningJson);
    }

    public String generateFamilyPlan(List<FamilyMemberResponse> members, int peopleCount, String taste,
                                     BigDecimal weeklyBudget, BigDecimal monthlySalary) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("members", members);
        params.put("peopleCount", peopleCount);
        params.put("taste", taste);
        params.put("weeklyBudget", weeklyBudget);

        String planJson = skill4.execute(params);

        BigDecimal weeklyCost = extractCost(planJson);
        Map<String, Object> warningParams = new HashMap<String, Object>();
        warningParams.put("weeklyCost", weeklyCost);
        warningParams.put("weeklyBudget", weeklyBudget);
        warningParams.put("monthlySalary", monthlySalary);
        String warningJson = skill2.execute(warningParams);

        return mergeJson(planJson, warningJson);
    }

    public String recalcCustomDish(String dishName, List<String> ingredients, BigDecimal estimatedCost,
                                   BigDecimal currentWeeklyCost, BigDecimal weeklyBudget) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dishName", dishName);
        params.put("ingredients", ingredients);
        params.put("estimatedCost", estimatedCost);
        params.put("currentWeeklyCost", currentWeeklyCost);
        params.put("weeklyBudget", weeklyBudget);

        return skill5.execute(params);
    }

    public String searchRecipes(String keyword) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("keyword", keyword);
        return skill3.execute(params);
    }
    
    public String generatePersonalPlanWithCustomDish(int peopleCount, String taste, BigDecimal weeklyBudget,
                                       BigDecimal monthlySalary, List<String> avoidIngredients,
                                       List<String> favoriteDishes, List<String> breakfastWant,
                                       List<String> lunchWant, List<String> dinnerWant, boolean savingMode,
                                       int dayIndex, int mealIndex, String newDishName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("peopleCount", peopleCount);
        params.put("taste", taste);
        params.put("avoidIngredients", avoidIngredients);
        params.put("favoriteDishes", favoriteDishes);
        params.put("breakfastWant", breakfastWant != null ? breakfastWant : new ArrayList<String>());
        params.put("lunchWant", lunchWant != null ? lunchWant : new ArrayList<String>());
        params.put("dinnerWant", dinnerWant != null ? dinnerWant : new ArrayList<String>());
        params.put("savingMode", savingMode);
        params.put("weeklyBudget", weeklyBudget);
        params.put("dayIndex", dayIndex);
        params.put("mealIndex", mealIndex);
        params.put("newDishName", newDishName);

        String planJson = skill1.execute(params);

        BigDecimal weeklyCost = extractCost(planJson);
        Map<String, Object> warningParams = new HashMap<String, Object>();
        warningParams.put("weeklyCost", weeklyCost);
        warningParams.put("weeklyBudget", weeklyBudget);
        warningParams.put("monthlySalary", monthlySalary);
        String warningJson = skill2.execute(warningParams);

        return mergeJson(planJson, warningJson);
    }
    
    public String generateFamilyPlanWithCustomDish(List<FamilyMemberResponse> members, int peopleCount, String taste,
                                     BigDecimal weeklyBudget, BigDecimal monthlySalary,
                                     List<String> breakfastWant, List<String> lunchWant, List<String> dinnerWant,
                                     boolean savingMode, int dayIndex, int mealIndex, String newDishName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("members", members);
        params.put("peopleCount", peopleCount);
        params.put("taste", taste);
        params.put("weeklyBudget", weeklyBudget);
        params.put("breakfastWant", breakfastWant != null ? breakfastWant : new ArrayList<String>());
        params.put("lunchWant", lunchWant != null ? lunchWant : new ArrayList<String>());
        params.put("dinnerWant", dinnerWant != null ? dinnerWant : new ArrayList<String>());
        params.put("savingMode", savingMode);
        params.put("dayIndex", dayIndex);
        params.put("mealIndex", mealIndex);
        params.put("newDishName", newDishName);

        String planJson = skill4.execute(params);

        BigDecimal weeklyCost = extractCost(planJson);
        Map<String, Object> warningParams = new HashMap<String, Object>();
        warningParams.put("weeklyCost", weeklyCost);
        warningParams.put("weeklyBudget", weeklyBudget);
        warningParams.put("monthlySalary", monthlySalary);
        String warningJson = skill2.execute(warningParams);

        return mergeJson(planJson, warningJson);
    }

    public String replaceIngredient(String dishName, List<String> currentIngredients, String replaceIngredient, String taste, int peopleCount) {
        return skill1.replaceIngredient(dishName, currentIngredients, replaceIngredient, taste, peopleCount);
    }

    private BigDecimal extractCost(String json) {
        int idx = json.indexOf("\"weeklyCost\":");
        if (idx == -1) return BigDecimal.ZERO;
        int endIdx = json.indexOf(",", idx);
        if (endIdx == -1) endIdx = json.indexOf("}", idx);
        String costStr = json.substring(idx + 13, endIdx).trim();
        return new BigDecimal(costStr);
    }

    private String mergeJson(String planJson, String warningJson) {
        planJson = planJson.substring(0, planJson.length() - 1);
        warningJson = warningJson.substring(1);
        return planJson + "," + warningJson;
    }
}