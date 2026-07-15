package com.practice.mealplanner.skill;

import com.practice.mealplanner.model.Ingredient;
import com.practice.mealplanner.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Skill3RagPriceLookup implements Skill {

    private final IngredientRepository ingredientRepository;

    @Override
    public String execute(Map<String, Object> params) {
        String keyword = (String) params.get("keyword");
        List<Ingredient> ingredients = ingredientRepository.findAll();

        List<Ingredient> matches = ingredients.stream()
                .filter(i -> i.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < matches.size(); i++) {
            Ingredient item = matches.get(i);
            result.append("{\"name\":\"").append(item.getName()).append("\",")
                    .append("\"pricePerKg\":").append(item.getPricePerKg()).append(",")
                    .append("\"unit\":\"").append(item.getUnit() != null ? item.getUnit() : "").append("\",")
                    .append("\"category\":\"").append(item.getCategory() != null ? item.getCategory() : "").append("\",")
                    .append("\"tags\":\"").append(item.getTags() != null ? item.getTags() : "").append("\"}");
            if (i < matches.size() - 1) result.append(",");
        }
        result.append("]");
        return result.toString();
    }

    public List<Ingredient> findSimilar(String name) {
        List<Ingredient> all = ingredientRepository.findAll();
        return all.stream()
                .filter(i -> i.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Ingredient> findCheaperAlternatives(String name) {
        List<Ingredient> all = ingredientRepository.findAll();
        List<Ingredient> similar = all.stream()
                .filter(i -> i.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
        if (similar.isEmpty()) return similar;
        return all.stream()
                .filter(i -> !i.getName().equals(name))
                .sorted((a, b) -> a.getPricePerKg().compareTo(b.getPricePerKg()))
                .limit(3)
                .collect(Collectors.toList());
    }
}