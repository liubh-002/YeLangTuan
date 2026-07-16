package com.practice.mealplanner.skill;

import com.practice.mealplanner.model.Ingredient;
import com.practice.mealplanner.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Skill5CustomDishBudgetRecalc implements Skill {

    private final IngredientRepository ingredientRepository;

    @Override
    public String execute(Map<String, Object> params) {
        String dishName = (String) params.get("dishName");
        List<String> ingredients = (List<String>) params.get("ingredients");
        BigDecimal estimatedCost = new BigDecimal(params.get("estimatedCost").toString());
        BigDecimal currentWeeklyCost = new BigDecimal(params.get("currentWeeklyCost").toString());
        BigDecimal weeklyBudget = new BigDecimal(params.get("weeklyBudget").toString());

        BigDecimal newWeeklyCost = currentWeeklyCost.add(estimatedCost);
        BigDecimal rate = newWeeklyCost.divide(weeklyBudget, 4, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("newWeeklyCost", newWeeklyCost);
        result.put("budgetRate", rate);

        if (rate.compareTo(BigDecimal.ONE) > 0) {
            result.put("optimizationNeeded", true);
            result.put("message", "自定义菜品导致预算超支，建议替换部分食材或调整用量");
            
            List<Ingredient> similar = findSimilarIngredients(ingredients);
            List<Ingredient> alternatives = findCheaperAlternatives(similar);
            if (!alternatives.isEmpty()) {
                result.put("suggestedReplacements", alternatives.stream()
                        .map(i -> i.getName() + "(" + i.getPricePerKg() + "元)")
                        .collect(Collectors.toList()));
            }
        } else {
            result.put("optimizationNeeded", false);
            result.put("message", "自定义菜品在预算范围内");
        }

        return mapToJson(result);
    }

    private List<Ingredient> findSimilarIngredients(List<String> names) {
        List<Ingredient> all = ingredientRepository.findAll();
        List<Ingredient> result = new ArrayList<Ingredient>();
        for (String name : names) {
            result.addAll(all.stream()
                    .filter(i -> i.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList()));
        }
        return result;
    }

    private List<Ingredient> findCheaperAlternatives(List<Ingredient> ingredients) {
        List<Ingredient> all = ingredientRepository.findAll();
        List<Ingredient> result = new ArrayList<Ingredient>();
        
        for (Ingredient target : ingredients) {
            List<Ingredient> cheaper = all.stream()
                    .filter(i -> !i.getName().equals(target.getName()))
                    .filter(i -> i.getCategory() != null && i.getCategory().equals(target.getCategory()))
                    .filter(i -> i.getPricePerKg().compareTo(target.getPricePerKg()) < 0)
                    .sorted((a, b) -> a.getPricePerKg().compareTo(b.getPricePerKg()))
                    .limit(2)
                    .collect(Collectors.toList());
            result.addAll(cheaper);
        }
        
        return result.stream().limit(3).collect(Collectors.toList());
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"newWeeklyCost\":").append(map.get("newWeeklyCost")).append(",");
        sb.append("\"budgetRate\":").append(map.get("budgetRate")).append(",");
        sb.append("\"optimizationNeeded\":").append(map.get("optimizationNeeded")).append(",");
        sb.append("\"message\":\"").append(map.get("message")).append("\"");
        
        if (map.containsKey("suggestedReplacements")) {
            sb.append(",\"suggestedReplacements\":[\"")
                    .append(String.join("\",\"", (List<String>) map.get("suggestedReplacements")))
                    .append("\"]");
        }
        
        sb.append("}");
        return sb.toString();
    }
}