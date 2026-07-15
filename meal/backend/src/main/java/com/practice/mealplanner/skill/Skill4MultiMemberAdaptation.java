package com.practice.mealplanner.skill;

import com.practice.mealplanner.dto.FamilyMemberResponse;
import com.practice.mealplanner.model.Ingredient;
import com.practice.mealplanner.repository.IngredientRepository;
import com.practice.mealplanner.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Skill4MultiMemberAdaptation implements Skill {

    private final IngredientRepository ingredientRepository;
    private final AiService aiService;

    private static final List<String> DAYS = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
    private static final Map<String, List<String>> TAG_SPECIALTY = new HashMap<String, List<String>>();
    private static final List<String> BREAKFAST_INGREDIENTS = Arrays.asList("鸡蛋", "大米", "小米", "面粉", "牛奶", "豆浆", "油条", "包子", "馒头", "面条", "豆腐脑", "麦片", "玉米", "红薯", "南瓜");
    static {
        TAG_SPECIALTY.put("减肥", Arrays.asList("低热量", "高蛋白", "膳食纤维"));
        TAG_SPECIALTY.put("病号", Arrays.asList("清淡", "易消化", "营养均衡"));
        TAG_SPECIALTY.put("青少年", Arrays.asList("高蛋白", "钙质", "维生素"));
        TAG_SPECIALTY.put("老年人", Arrays.asList("软烂", "低盐", "易消化"));
    }

    @Override
    public String execute(Map<String, Object> params) {
        List<FamilyMemberResponse> members = (List<FamilyMemberResponse>) params.get("members");
        int peopleCount = (int) params.get("peopleCount");
        String taste = (String) params.get("taste");
        BigDecimal weeklyBudget = params.get("weeklyBudget") != null ? (BigDecimal) params.get("weeklyBudget") : new BigDecimal("1000");
        List<String> breakfastWant = (List<String>) params.getOrDefault("breakfastWant", new ArrayList<String>());
        List<String> lunchWant = (List<String>) params.getOrDefault("lunchWant", new ArrayList<String>());
        List<String> dinnerWant = (List<String>) params.getOrDefault("dinnerWant", new ArrayList<String>());
        boolean savingMode = params.get("savingMode") != null ? (Boolean) params.get("savingMode") : false;

        List<String> allTaboos = new ArrayList<String>();
        List<String> allTags = new ArrayList<String>();
        int totalAppetite = 0;

        for (FamilyMemberResponse member : members) {
            if (!"无".equals(member.getDietTaboo())) {
                allTaboos.addAll(Arrays.asList(member.getDietTaboo().split("[，,]")));
            }
            allTags.add(member.getPersonTag());
            totalAppetite += member.getAppetite();
        }

        try {
            return aiService.generateFamilyMealPlan(peopleCount, taste, allTaboos, allTags,
                    breakfastWant, lunchWant, dinnerWant, savingMode, weeklyBudget.toString());
        } catch (Exception e) {
            List<Ingredient> ingredients = ingredientRepository.findAll();
            List<Ingredient> available = filterAvoided(ingredients, allTaboos);
            if (available.isEmpty()) available = ingredients;

            Integer dayIndex = params.get("dayIndex") != null ? ((Number) params.get("dayIndex")).intValue() : null;
            Integer mealIndex = params.get("mealIndex") != null ? ((Number) params.get("mealIndex")).intValue() : null;
            String newDishName = (String) params.get("newDishName");
            
            return buildMixedPlan(available, peopleCount, taste, allTags, totalAppetite, 
                    breakfastWant, lunchWant, dinnerWant, savingMode, weeklyBudget,
                    dayIndex, mealIndex, newDishName);
        }
    }

    private String buildMixedPlan(List<Ingredient> ingredients, int peopleCount, String taste,
                                   List<String> tags, int totalAppetite, List<String> breakfastWant,
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

        BigDecimal dailyBudget = weeklyBudget.divide(new BigDecimal("7"), 2, RoundingMode.HALF_UP);
        BigDecimal appetiteFactor = BigDecimal.valueOf(totalAppetite).divide(BigDecimal.valueOf(tags.size() * 3), 2, RoundingMode.HALF_UP);
        dailyBudget = dailyBudget.multiply(appetiteFactor);

        StringBuilder result = new StringBuilder();
        BigDecimal weeklyCost = BigDecimal.ZERO;

        for (int i = 0; i < DAYS.size(); i++) {
            String tag = tags.get(Math.floorMod(i, tags.size()));
            List<String> preferences = TAG_SPECIALTY.getOrDefault(tag, Collections.<String>emptyList());

            result.append("{\"day\":\"").append(DAYS.get(i)).append("\",\"adaptedTag\":\"").append(tag).append("\",\"meals\":[");

            boolean isCustomBreakfast = customDayIndex != null && customDayIndex == i && customMealIndex == 0;
            Map<String, Object> breakfast = isCustomBreakfast ?
                    createCustomMeal(customDishName, breakfastIngredients, staples, "早餐", peopleCount, tag, savingMode) :
                    createBreakfast(i, breakfastIngredients, staples, taste, tag, preferences, breakfastWant, peopleCount);
            result.append(mapToJson(breakfast)).append(",");

            BigDecimal remainingDailyBudget = dailyBudget.subtract((BigDecimal) breakfast.get("cost"));
            BigDecimal mealBudget = remainingDailyBudget.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);

            boolean isCustomLunch = customDayIndex != null && customDayIndex == i && customMealIndex == 1;
            Map<String, Object> lunch = isCustomLunch ?
                    createCustomMeal(customDishName, proteins, vegetables, "午餐", peopleCount, tag, savingMode) :
                    createMeal(i, proteins, vegetables, staples, "午餐",
                            Arrays.asList("小炒", "盖饭", "炖菜"), taste, tag, preferences, lunchWant, peopleCount, savingMode, mealBudget);
            result.append(mapToJson(lunch)).append(",");

            BigDecimal dinnerBudget = remainingDailyBudget.subtract((BigDecimal) lunch.get("cost"));
            boolean isCustomDinner = customDayIndex != null && customDayIndex == i && customMealIndex == 2;
            Map<String, Object> dinner = isCustomDinner ?
                    createCustomMeal(customDishName, proteins, vegetables, "晚餐", peopleCount, tag, savingMode) :
                    createMeal(i, proteins, vegetables, staples, "晚餐",
                            Arrays.asList("清汤", "蒸菜", "炖汤"), taste, tag, preferences, dinnerWant, peopleCount, savingMode, dinnerBudget);
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
                                               String tag, List<String> preferences,
                                               List<String> breakfastWant, int peopleCount) {
        Ingredient main = pick(breakfastIngredients, index);
        Ingredient side = pick(staples, index + 3);
        
        List<String> styles = Arrays.asList("营养粥", "热汤面", "早餐蛋饼", "蔬菜蛋羹", "暖胃米糊", "清爽饭团", "豆浆油条");
        String style = styles.get(index % styles.size());
        String prefix = "";
        if ("减肥".equals(tag)) prefix = "低脂";
        else if ("病号".equals(tag)) prefix = "清淡";
        else if ("青少年".equals(tag)) prefix = "营养";
        else if ("老年人".equals(tag)) prefix = "软烂";
        
        String name = breakfastName(prefix, style, main, side);

        if (breakfastWant != null && !breakfastWant.isEmpty()) {
            String wantItem = breakfastWant.get(Math.floorMod(index, breakfastWant.size()));
            if (wantItem != null && !wantItem.trim().isEmpty()) {
                name = wantItem.trim();
            }
        }

        List<String> used = Arrays.asList(main.getName(), side.getName());
        BigDecimal cost = calculateCost(Arrays.asList(main, side), peopleCount, tag, "早餐");

        Map<String, Object> meal = new HashMap<String, Object>();
        meal.put("mealType", "早餐");
        meal.put("dishName", name);
        meal.put("ingredients", used);
        meal.put("cost", cost);
        meal.put("nutritionNote", "早餐营养均衡，包含碳水和蛋白，开启活力一天");
        meal.put("savingNote", "早餐优先选择经济实惠的搭配");
        return meal;
    }
    
    private String breakfastName(String prefix, String style, Ingredient main, Ingredient side) {
        String mainName = clean(main.getName());
        String sideName = clean(side.getName());
        
        if (style.contains("粥")) {
            return prefix + mainName + sideName + "粥";
        }
        if (style.contains("面")) {
            return prefix + mainName + "汤面";
        }
        if (style.contains("蛋")) {
            return prefix + mainName + "蛋饼";
        }
        if (style.contains("豆浆")) {
            return "豆浆配" + prefix + sideName;
        }
        return prefix + style + "套餐";
    }
    
    private Map<String, Object> createMeal(int index, List<Ingredient> list1, List<Ingredient> list2,
                                           List<Ingredient> list3, String mealType, List<String> styles,
                                           String taste, String tag, List<String> preferences,
                                           List<String> want, int peopleCount, boolean savingMode, BigDecimal maxBudget) {
        Ingredient item1 = pick(list1, index);
        Ingredient item2 = pick(list2, index + 1);
        Ingredient item3 = pick(list3, index + 2);

        String style = styles.get(Math.floorMod(index, styles.size()));
        String generated = adaptDishName(taste, item1, item2, style, tag);

        if (want != null && !want.isEmpty()) {
            String wantItem = want.get(Math.floorMod(index, want.size()));
            if (wantItem != null && !wantItem.trim().isEmpty()) {
                generated = wantItem.trim();
            }
        }

        List<String> used = Arrays.asList(item1.getName(), item2.getName(), item3.getName());
        BigDecimal cost = calculateCost(Arrays.asList(item1, item2, item3), peopleCount, tag, mealType);

        if (maxBudget != null && cost.compareTo(maxBudget) > 0 || savingMode) {
            item1 = findCheaperAlternative(list1, item1);
            item2 = findCheaperAlternative(list2, item2);
            cost = calculateCost(Arrays.asList(item1, item2, item3), peopleCount, tag, mealType);
            used = Arrays.asList(item1.getName(), item2.getName(), item3.getName());
            generated = adaptDishName(taste, item1, item2, style, tag);
        }

        Map<String, Object> meal = new HashMap<String, Object>();
        meal.put("mealType", mealType);
        meal.put("dishName", generated);
        meal.put("ingredients", used);
        meal.put("cost", cost);
        meal.put("nutritionNote", nutritionNote(mealType, tag, preferences));
        meal.put("savingNote", savingMode ? "已优先选择低价同类食材" : "混合适配全家饮食需求");
        return meal;
    }
    
    private Ingredient findCheaperAlternative(List<Ingredient> list, Ingredient current) {
        if (list.size() < 2) return current;
        List<Ingredient> sorted = list.stream()
                .sorted(Comparator.comparing(Ingredient::getPricePerKg))
                .collect(Collectors.toList());
        return sorted.get(0);
    }

    private String adaptDishName(String taste, Ingredient item1, Ingredient item2, String style, String tag) {
        String n1 = clean(item1.getName());
        String n2 = clean(item2.getName());
        String prefix = "";
        if ("减肥".equals(tag)) prefix = "低脂";
        else if ("病号".equals(tag)) prefix = "清淡";
        else if ("青少年".equals(tag)) prefix = "营养";
        else if ("老年人".equals(tag)) prefix = "软烂";
        return prefix + n2 + n1 + style;
    }

    private String nutritionNote(String mealType, String tag, List<String> preferences) {
        StringBuilder note = new StringBuilder();
        note.append(mealType).append("：");
        if ("减肥".equals(tag)) note.append("低热量高蛋白，适合减脂人群");
        else if ("病号".equals(tag)) note.append("清淡易消化，适合恢复期病人");
        else if ("青少年".equals(tag)) note.append("高蛋白高钙，促进生长发育");
        else if ("老年人".equals(tag)) note.append("软烂低盐，保护肠胃功能");
        else note.append("营养均衡搭配");
        note.append("。特点：").append(String.join("、", preferences));
        return note.toString();
    }

    private BigDecimal calculateCost(List<Ingredient> used, int peopleCount, String tag, String mealType) {
        BigDecimal ratio = "减肥".equals(tag) ? new BigDecimal("0.8") :
                "老年人".equals(tag) ? new BigDecimal("0.9") : BigDecimal.ONE;
        
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

    private String clean(String name) {
        if (name == null) return "";
        return name.replace("北豆腐", "白豆腐").replace("大白菜", "白菜")
                .replace("西红柿", "番茄").replace("猪里脊", "里脊肉").trim();
    }

    private List<Ingredient> filterAvoided(List<Ingredient> ingredients, List<String> avoid) {
        if (avoid == null || avoid.isEmpty()) return ingredients;
        Set<String> avoidSet = avoid.stream().map(s -> s.toLowerCase().trim()).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        return ingredients.stream().filter(i -> avoidSet.stream()
                .noneMatch(word -> i.getName().toLowerCase().contains(word))).collect(Collectors.toList());
    }

    private List<Ingredient> byCategories(List<Ingredient> ingredients, String... categories) {
        Set<String> expected = new HashSet<String>(Arrays.asList(categories));
        return ingredients.stream().filter(i -> expected.stream()
                .anyMatch(cat -> i.getCategory() != null && i.getCategory().contains(cat))).collect(Collectors.toList());
    }

    private Ingredient pick(List<Ingredient> list, int index) {
        return list.get(Math.floorMod(index, list.size()));
    }

    private Map<String, Object> createCustomMeal(String dishName, List<Ingredient> proteins, 
                                                List<Ingredient> vegetables, String mealType,
                                                int peopleCount, String tag, boolean savingMode) {
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
        
        BigDecimal ratio = savingMode ? new BigDecimal("0.7") : 
                ("减肥".equals(tag) ? new BigDecimal("0.8") : 
                 "老年人".equals(tag) ? new BigDecimal("0.9") : BigDecimal.ONE);
        mealCost = mealCost.multiply(ratio).multiply(BigDecimal.valueOf(peopleCount)).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> meal = new HashMap<String, Object>();
        meal.put("mealType", mealType);
        meal.put("dishName", dishName);
        meal.put("ingredients", used);
        meal.put("cost", mealCost);
        meal.put("nutritionNote", "根据您的喜好定制，适合" + tag + "人群");
        meal.put("savingNote", savingMode ? "已优先选择低价同类食材" : "混合适配全家饮食需求");
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