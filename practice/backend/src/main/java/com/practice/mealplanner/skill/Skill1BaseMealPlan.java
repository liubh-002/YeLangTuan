package com.practice.mealplanner.skill;

import com.practice.mealplanner.model.Ingredient;
import com.practice.mealplanner.repository.IngredientRepository;
import com.practice.mealplanner.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Skill1BaseMealPlan implements Skill {

    private final IngredientRepository ingredientRepository;
    private final AiService aiService;

    private static final List<String> DAYS = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
    private static final List<String> BREAKFAST_STYLES = Arrays.asList("营养粥", "热汤面", "早餐蛋饼", "蔬菜蛋羹", "暖胃米糊", "清爽饭团", "豆浆油条");
    private static final List<String> LUNCH_STYLES = Arrays.asList("小炒", "盖饭", "炖菜", "炒饭", "焖饭", "拌面", "家常煲");
    private static final List<String> DINNER_STYLES = Arrays.asList("清汤", "蒸菜", "炖汤", "小炒", "汤饭", "拌菜", "砂锅");
    
    private static final List<String> BREAKFAST_INGREDIENTS = Arrays.asList("鸡蛋", "大米", "小米", "面粉", "牛奶", "豆浆", "油条", "包子", "馒头", "面条", "豆腐脑", "麦片", "玉米", "红薯", "南瓜");

    @Override
    public String execute(Map<String, Object> params) {
        int peopleCount = (int) params.get("peopleCount");
        String taste = (String) params.get("taste");
        List<String> avoidIngredients = (List<String>) params.get("avoidIngredients");
        List<String> favoriteDishes = (List<String>) params.getOrDefault("favoriteDishes", new ArrayList<String>());
        List<String> breakfastWant = (List<String>) params.getOrDefault("breakfastWant", new ArrayList<String>());
        List<String> lunchWant = (List<String>) params.getOrDefault("lunchWant", new ArrayList<String>());
        List<String> dinnerWant = (List<String>) params.getOrDefault("dinnerWant", new ArrayList<String>());
        boolean savingMode = params.get("savingMode") != null ? (Boolean) params.get("savingMode") : false;
        BigDecimal weeklyBudget = params.get("weeklyBudget") != null ? (BigDecimal) params.get("weeklyBudget") : new BigDecimal("1000");

        Integer dayIndex = params.get("dayIndex") != null ? ((Number) params.get("dayIndex")).intValue() : null;
        Integer mealIndex = params.get("mealIndex") != null ? ((Number) params.get("mealIndex")).intValue() : null;
        String newDishName = (String) params.get("newDishName");

        try {
            if (newDishName != null && !newDishName.trim().isEmpty() && dayIndex != null && mealIndex != null) {
                String mealType = getMealType(mealIndex);
                String dishInfo = aiService.generateDishInfo(newDishName, mealType);
                if (dishInfo != null && !dishInfo.isEmpty()) {
                    return generatePlanWithCustomDish(peopleCount, taste, avoidIngredients,
                            favoriteDishes, breakfastWant, lunchWant, dinnerWant,
                            savingMode, weeklyBudget, dayIndex, mealIndex, newDishName, dishInfo);
                }
            }
            
            return aiService.generateMealPlan(peopleCount, taste, avoidIngredients,
                    favoriteDishes, breakfastWant, lunchWant, dinnerWant,
                    savingMode, weeklyBudget.toString());
        } catch (Exception e) {
            List<Ingredient> ingredients = ingredientRepository.findAll();
            List<Ingredient> available = filterAvoided(ingredients, avoidIngredients);
            if (available.isEmpty()) {
                available = ingredients;
            }

            if (!savingMode && shouldSave(weeklyBudget, peopleCount)) {
                savingMode = true;
            }
            
            return buildPlan(peopleCount, taste, available, favoriteDishes, breakfastWant, lunchWant, dinnerWant, 
                    savingMode, weeklyBudget, dayIndex, mealIndex, newDishName);
        }
    }
    
    private String getMealType(int mealIndex) {
        if (mealIndex == 0) return "早餐";
        if (mealIndex == 1) return "午餐";
        return "晚餐";
    }
    
    private String generatePlanWithCustomDish(int peopleCount, String taste, List<String> avoidIngredients,
                                               List<String> favoriteDishes, List<String> breakfastWant,
                                               List<String> lunchWant, List<String> dinnerWant,
                                               boolean savingMode, BigDecimal weeklyBudget,
                                               int dayIndex, int mealIndex, String newDishName, String dishInfo) {
        String planJson = aiService.generateMealPlan(peopleCount, taste, avoidIngredients,
                favoriteDishes, breakfastWant, lunchWant, dinnerWant,
                savingMode, weeklyBudget.toString());
        
        if (planJson == null || planJson.isEmpty()) {
            List<Ingredient> ingredients = ingredientRepository.findAll();
            List<Ingredient> available = filterAvoided(ingredients, avoidIngredients);
            if (available.isEmpty()) available = ingredients;
            return buildPlan(peopleCount, taste, available, favoriteDishes, breakfastWant, lunchWant, dinnerWant,
                    savingMode, weeklyBudget, dayIndex, mealIndex, newDishName);
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> plan = mapper.readValue(planJson, Map.class);
            List<Map<String, Object>> days = (List<Map<String, Object>>) plan.get("days");
            
            if (dayIndex < days.size()) {
                Map<String, Object> day = days.get(dayIndex);
                List<Map<String, Object>> meals = (List<Map<String, Object>>) day.get("meals");
                
                if (mealIndex < meals.size()) {
                    Map<String, Object> dishInfoMap = mapper.readValue(dishInfo, Map.class);
                    Map<String, Object> meal = meals.get(mealIndex);
                    meal.put("dishName", newDishName);
                    meal.put("ingredients", dishInfoMap.get("ingredients"));
                    meal.put("cost", dishInfoMap.get("cost"));
                    meal.put("nutritionNote", dishInfoMap.get("nutritionNote"));
                }
                
                double dailyCost = 0;
                for (Map<String, Object> m : meals) {
                    dailyCost += (Double) m.get("cost");
                }
                day.put("dailyCost", dailyCost);
                
                double weeklyCost = 0;
                for (Map<String, Object> d : days) {
                    weeklyCost += (Double) d.get("dailyCost");
                }
                plan.put("weeklyCost", weeklyCost);
                
                return mapper.writeValueAsString(plan);
            }
        } catch (Exception e) {
            log.error("Failed to merge custom dish info", e);
        }
        
        return planJson;
    }
    
    private boolean shouldSave(BigDecimal weeklyBudget, int peopleCount) {
        BigDecimal dailyBudget = weeklyBudget.divide(new BigDecimal("7"), 2, RoundingMode.HALF_UP);
        BigDecimal perPersonDaily = dailyBudget.divide(BigDecimal.valueOf(peopleCount), 2, RoundingMode.HALF_UP);
        return perPersonDaily.compareTo(new BigDecimal("20")) < 0;
    }

    public String buildPlan(int peopleCount, String taste, List<Ingredient> ingredients,
                            List<String> favoriteDishes, List<String> breakfastWant,
                            List<String> lunchWant, List<String> dinnerWant,
                            boolean savingMode, BigDecimal weeklyBudget,
                            Integer customDayIndex, Integer customMealIndex, String customDishName) {
        List<Ingredient> proteins = byCategories(ingredients, "肉蛋水产", "豆制品");
        List<Ingredient> vegetables = byCategories(ingredients, "蔬菜");
        List<Ingredient> staples = byCategories(ingredients, "粮油主食");
        List<Ingredient> breakfastIngredients = filterBreakfastIngredients(staples, proteins);

        if (proteins.isEmpty()) proteins = ingredients;
        if (vegetables.isEmpty()) vegetables = ingredients;
        if (staples.isEmpty()) staples = ingredients;
        if (breakfastIngredients.isEmpty()) breakfastIngredients = staples;

        proteins = sortForMode(proteins, savingMode, taste);
        vegetables = sortForMode(vegetables, savingMode, taste);
        staples = sortForMode(staples, true, taste);
        breakfastIngredients = sortForMode(breakfastIngredients, true, taste);

        BigDecimal dailyBudget = weeklyBudget.divide(new BigDecimal("7"), 2, RoundingMode.HALF_UP);
        
        StringBuilder result = new StringBuilder();
        BigDecimal weeklyCost = BigDecimal.ZERO;

        for (int i = 0; i < DAYS.size(); i++) {
            result.append("{\"day\":\"").append(DAYS.get(i)).append("\",\"meals\":[");

            boolean isCustomBreakfast = customDayIndex != null && customDayIndex == i && customMealIndex == 0;
            Map<String, Object> breakfast = isCustomBreakfast ? 
                    createCustomMeal(customDishName, breakfastIngredients, staples, "早餐", peopleCount, savingMode) :
                    createBreakfast(i, breakfastIngredients, staples, 
                            taste, favoriteDishes, breakfastWant, i, peopleCount, savingMode);
            result.append(mapToJson(breakfast)).append(",");

            BigDecimal remainingDailyBudget = dailyBudget.subtract((BigDecimal) breakfast.get("cost"));
            BigDecimal mealBudget = remainingDailyBudget.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);

            boolean isCustomLunch = customDayIndex != null && customDayIndex == i && customMealIndex == 1;
            Map<String, Object> lunch = isCustomLunch ?
                    createCustomMeal(customDishName, proteins, vegetables, "午餐", peopleCount, savingMode) :
                    createMeal(i, proteins, vegetables, staples, "午餐", LUNCH_STYLES,
                            taste, favoriteDishes, lunchWant, i + 7, peopleCount, savingMode, mealBudget);
            result.append(mapToJson(lunch)).append(",");

            BigDecimal dinnerBudget = remainingDailyBudget.subtract((BigDecimal) lunch.get("cost"));
            boolean isCustomDinner = customDayIndex != null && customDayIndex == i && customMealIndex == 2;
            Map<String, Object> dinner = isCustomDinner ?
                    createCustomMeal(customDishName, proteins, vegetables, "晚餐", peopleCount, savingMode) :
                    createMeal(i, proteins, vegetables, staples, "晚餐", DINNER_STYLES,
                            taste, favoriteDishes, dinnerWant, i + 14, peopleCount, savingMode, dinnerBudget);
            result.append(mapToJson(dinner));

            BigDecimal breakfastCost = (BigDecimal) breakfast.get("cost");
            BigDecimal lunchCost = (BigDecimal) lunch.get("cost");
            BigDecimal dinnerCost = (BigDecimal) dinner.get("cost");
            BigDecimal dailyCost = breakfastCost.add(lunchCost).add(dinnerCost);
            weeklyCost = weeklyCost.add(dailyCost);

            result.append("],\"dailyCost\":").append(dailyCost).append("}");
            if (i < DAYS.size() - 1) result.append(",");
        }

        return "{\"days\":[" + result + "],\"weeklyCost\":" + weeklyCost + "}";
    }
    
    private List<Ingredient> filterBreakfastIngredients(List<Ingredient> staples, List<Ingredient> proteins) {
        List<Ingredient> result = new ArrayList<Ingredient>();
        for (Ingredient ing : staples) {
            if (isBreakfastIngredient(ing.getName())) {
                result.add(ing);
            }
        }
        for (Ingredient ing : proteins) {
            if ("鸡蛋".equals(clean(ing.getName())) || ing.getName().contains("鸡蛋")) {
                result.add(ing);
            }
        }
        return result;
    }
    
    private boolean isBreakfastIngredient(String name) {
        String cleanName = clean(name);
        return BREAKFAST_INGREDIENTS.stream().anyMatch(cleanName::contains);
    }

    private Map<String, Object> createBreakfast(int index, List<Ingredient> breakfastIngredients, 
                                               List<Ingredient> staples, String taste, 
                                               List<String> favorites, List<String> breakfastWant,
                                               int favIndex, int peopleCount, boolean savingMode) {
        Ingredient main = pick(breakfastIngredients, index);
        Ingredient side = pick(staples, index + 3);
        
        String style = BREAKFAST_STYLES.get(index % BREAKFAST_STYLES.size());
        String generated = breakfastName(style, main, side);
        String name = favoriteOrGenerated(favorites, favIndex, generated);

        if (breakfastWant != null && !breakfastWant.isEmpty()) {
            String wantItem = breakfastWant.get(Math.floorMod(index, breakfastWant.size()));
            if (wantItem != null && !wantItem.trim().isEmpty()) {
                name = wantItem.trim();
            }
        }

        List<String> used = Arrays.asList(main.getName(), side.getName());
        BigDecimal mealCost = calculateCost(Arrays.asList(main, side), peopleCount, true, "早餐");

        Map<String, Object> meal = new HashMap<String, Object>();
        meal.put("mealType", "早餐");
        meal.put("dishName", name);
        meal.put("ingredients", used);
        meal.put("cost", mealCost);
        meal.put("nutritionNote", "早餐营养均衡，包含碳水和蛋白，开启活力一天");
        meal.put("savingNote", "早餐优先选择经济实惠的搭配");
        return meal;
    }
    
    private String breakfastName(String style, Ingredient main, Ingredient side) {
        String mainName = clean(main.getName());
        String sideName = clean(side.getName());
        
        if (style.contains("粥")) {
            return mainName + sideName + "粥";
        }
        if (style.contains("面")) {
            return mainName + "汤面";
        }
        if (style.contains("蛋")) {
            return mainName + "蛋饼";
        }
        if (style.contains("豆浆")) {
            return "豆浆配" + sideName;
        }
        return style + "套餐";
    }
    
    private Map<String, Object> createMeal(int index, List<Ingredient> list1, List<Ingredient> list2,
                                           List<Ingredient> list3, String mealType, List<String> styles,
                                           String taste, List<String> favorites, List<String> want,
                                           int favIndex, int peopleCount, boolean savingMode, BigDecimal maxBudget) {
        Ingredient item1 = pick(list1, index);
        Ingredient item2 = pick(list2, index + 1);
        Ingredient item3 = pick(list3, index + 2);

        String generated = dishName(taste, item1, item2, styles.get(index % styles.size()));
        String name = favoriteOrGenerated(favorites, favIndex, generated);

        if (want != null && !want.isEmpty()) {
            String wantItem = want.get(Math.floorMod(index, want.size()));
            if (wantItem != null && !wantItem.trim().isEmpty()) {
                name = wantItem.trim();
            }
        }

        List<String> used = Arrays.asList(item1.getName(), item2.getName(), item3.getName());
        BigDecimal mealCost = calculateCost(Arrays.asList(item1, item2, item3), peopleCount, savingMode, mealType);

        if (maxBudget != null && mealCost.compareTo(maxBudget) > 0) {
            item1 = findCheaperAlternative(list1, item1, savingMode);
            item2 = findCheaperAlternative(list2, item2, savingMode);
            mealCost = calculateCost(Arrays.asList(item1, item2, item3), peopleCount, true, mealType);
            used = Arrays.asList(item1.getName(), item2.getName(), item3.getName());
            generated = dishName(taste, item1, item2, styles.get(index % styles.size()));
            name = favoriteOrGenerated(favorites, favIndex, generated);
        }

        Map<String, Object> meal = new HashMap<String, Object>();
        meal.put("mealType", mealType);
        meal.put("dishName", name);
        meal.put("ingredients", used);
        meal.put("cost", mealCost);
        meal.put("nutritionNote", nutritionNote(mealType));
        meal.put("savingNote", savingMode ? "已优先选择低价同类食材" : "常规营养搭配");
        return meal;
    }
    
    private Ingredient findCheaperAlternative(List<Ingredient> list, Ingredient current, boolean alreadyCheap) {
        if (alreadyCheap || list.size() < 2) return current;
        List<Ingredient> sorted = list.stream()
                .sorted(Comparator.comparing(Ingredient::getPricePerKg))
                .collect(Collectors.toList());
        return sorted.get(0);
    }

    private String dishName(String taste, Ingredient item1, Ingredient item2, String style) {
        String n1 = clean(item1.getName());
        String n2 = clean(item2.getName());
        if ("清淡".equals(taste) || "清汤".equals(style) || "炖汤".equals(style)) {
            return n2 + n1 + style;
        }
        if ("香辣".equals(taste)) {
            return "香辣" + n2 + n1;
        }
        if ("高蛋白".equals(taste)) {
            return n1 + n2 + style;
        }
        return n2 + n1 + style;
    }

    private String favoriteOrGenerated(List<String> favorites, int index, String generated) {
        if (favorites == null || favorites.isEmpty()) return generated;
        List<String> cleaned = favorites.stream().filter(f -> f != null && !f.trim().isEmpty()).collect(Collectors.toList());
        if (cleaned.isEmpty() || index % 3 != 0) return generated;
        return cleaned.get(Math.floorMod(index, cleaned.size())) + "（偏好菜）";
    }

    private String clean(String name) {
        if (name == null) return "";
        return name.replace("北豆腐", "白豆腐").replace("大白菜", "白菜")
                .replace("西红柿", "番茄").replace("猪里脊", "里脊肉").trim();
    }

    private BigDecimal calculateCost(List<Ingredient> used, int peopleCount, boolean savingMode, String mealType) {
        BigDecimal ratio = savingMode ? new BigDecimal("0.7") : BigDecimal.ONE;
        
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Ingredient ing : used) {
            BigDecimal weight = getWeightByMealType(mealType, ing.getCategory());
            totalCost = totalCost.add(ing.getPricePerKg().multiply(weight));
        }
        
        return totalCost.multiply(ratio).multiply(BigDecimal.valueOf(peopleCount)).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal getWeightByMealType(String mealType, String category) {
        if ("早餐".equals(mealType)) {
            if (category != null && category.contains("肉蛋水产")) {
                return new BigDecimal("0.10");
            }
            return new BigDecimal("0.15");
        } else if ("午餐".equals(mealType)) {
            if (category != null && category.contains("肉蛋水产")) {
                return new BigDecimal("0.20");
            }
            if (category != null && category.contains("蔬菜")) {
                return new BigDecimal("0.30");
            }
            return new BigDecimal("0.25");
        } else {
            if (category != null && category.contains("肉蛋水产")) {
                return new BigDecimal("0.15");
            }
            if (category != null && category.contains("蔬菜")) {
                return new BigDecimal("0.25");
            }
            return new BigDecimal("0.20");
        }
    }

    private String nutritionNote(String mealType) {
        switch (mealType) {
            case "早餐": return "主食搭配蛋白和蔬菜，适合早晨稳定供能";
            case "午餐": return "午餐蛋白占比更高，兼顾饱腹和膳食纤维";
            case "晚餐": return "晚餐减少油腻感，保留足量蔬菜";
            default: return "";
        }
    }

    private List<Ingredient> filterAvoided(List<Ingredient> ingredients, List<String> avoid) {
        if (avoid == null || avoid.isEmpty()) return ingredients;
        Set<String> avoidSet = avoid.stream().map(s -> s.toLowerCase()).collect(Collectors.toSet());
        return ingredients.stream().filter(i -> avoidSet.stream()
                .noneMatch(word -> i.getName().toLowerCase().contains(word))).collect(Collectors.toList());
    }

    private List<Ingredient> byCategories(List<Ingredient> ingredients, String... categories) {
        Set<String> expected = new HashSet<String>(Arrays.asList(categories));
        return ingredients.stream().filter(i -> expected.stream()
                .anyMatch(cat -> i.getCategory() != null && i.getCategory().contains(cat))).collect(Collectors.toList());
    }

    private List<Ingredient> sortForMode(List<Ingredient> ingredients, boolean savingMode, String taste) {
        Comparator<Ingredient> comparator = Comparator.comparing(Ingredient::getPricePerKg);
        if (!savingMode) {
            Map<Boolean, List<Ingredient>> grouped = ingredients.stream().sorted(comparator)
                    .collect(Collectors.partitioningBy(i -> i.getTags() != null && i.getTags().contains(taste)));
            List<Ingredient> sorted = new ArrayList<Ingredient>();
            sorted.addAll(grouped.get(true));
            sorted.addAll(grouped.get(false));
            return sorted;
        }
        return ingredients.stream().sorted(comparator).collect(Collectors.toList());
    }

    private Ingredient pick(List<Ingredient> list, int index) {
        return list.get(Math.floorMod(index, list.size()));
    }

    private Map<String, Object> createCustomMeal(String dishName, List<Ingredient> proteins, 
                                                List<Ingredient> vegetables, String mealType,
                                                int peopleCount, boolean savingMode) {
        Ingredient protein = proteins.isEmpty() ? null : pick(proteins, 0);
        Ingredient vegetable = vegetables.isEmpty() ? null : pick(vegetables, 0);
        
        List<String> used = new ArrayList<String>();
        BigDecimal mealCost = BigDecimal.ZERO;
        
        if (protein != null) {
            used.add(protein.getName());
            BigDecimal weight = getWeightByMealType(mealType, protein.getCategory());
            mealCost = mealCost.add(protein.getPricePerKg().multiply(weight));
        }
        if (vegetable != null) {
            used.add(vegetable.getName());
            BigDecimal weight = getWeightByMealType(mealType, vegetable.getCategory());
            mealCost = mealCost.add(vegetable.getPricePerKg().multiply(weight));
        }
        
        BigDecimal ratio = savingMode ? new BigDecimal("0.7") : BigDecimal.ONE;
        mealCost = mealCost.multiply(ratio).multiply(BigDecimal.valueOf(peopleCount)).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> meal = new HashMap<String, Object>();
        meal.put("mealType", mealType);
        meal.put("dishName", dishName);
        meal.put("ingredients", used);
        meal.put("cost", mealCost);
        meal.put("nutritionNote", "根据您的喜好定制，营养均衡搭配");
        meal.put("savingNote", savingMode ? "已优先选择低价同类食材" : "常规营养搭配");
        return meal;
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"mealType\":\"").append(map.get("mealType")).append("\",");
        sb.append("\"dishName\":\"").append(map.get("dishName")).append("\",");
        sb.append("\"ingredients\":[\"").append(String.join("\",\"", (List<String>) map.get("ingredients"))).append("\"],");
        sb.append("\"cost\":").append(map.get("cost")).append(",");
        sb.append("\"nutritionNote\":\"").append(map.get("nutritionNote")).append("\",");
        sb.append("\"savingNote\":\"").append(map.get("savingNote")).append("\"}");
        return sb.toString();
    }
}