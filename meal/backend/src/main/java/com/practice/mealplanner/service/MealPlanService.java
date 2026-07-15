package com.practice.mealplanner.service;

import com.practice.mealplanner.dto.MealPlanRequest;
import com.practice.mealplanner.dto.MealPlanResponse;
import com.practice.mealplanner.model.Ingredient;
import com.practice.mealplanner.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private static final List<String> DAYS = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
    private static final List<String> BREAKFAST_STYLES = Arrays.asList("营养粥", "热汤面", "早餐蛋饼", "蔬菜拌饭", "暖胃米糊", "清爽饭团", "家常汤粉");
    private static final List<String> LUNCH_STYLES = Arrays.asList("小炒", "盖饭", "炖菜", "炒饭", "焖饭", "拌面", "家常煲");
    private static final List<String> DINNER_STYLES = Arrays.asList("清汤", "蒸菜", "炖汤", "小炒", "汤饭", "拌菜", "砂锅");

    private final IngredientRepository ingredientRepository;
    private final IngredientMarketSyncService syncService;

    @Transactional
    public MealPlanResponse generate(MealPlanRequest request) {
        if (ingredientRepository.count() == 0) {
            syncService.syncMarketData();
        }
        List<Ingredient> ingredients = ingredientRepository.findAll();
        List<Ingredient> available = filterAvoided(ingredients, request.getAvoidIngredients());
        if (available.isEmpty()) {
            available = ingredients;
        }

        MealPlanResponse plan = buildPlan(request, available, false);
        applyWarning(plan);
        if ("HEAVY".equals(plan.getWarningLevel())) {
            List<Ingredient> cheaper = available.stream()
                    .sorted(Comparator.comparing(Ingredient::getPricePerKg))
                    .limit(Math.max(10, available.size() / 2))
                    .collect(Collectors.toList());
            plan = buildPlan(request, cheaper, true);
            applyWarning(plan);
        }
        return plan;
    }

    private MealPlanResponse buildPlan(MealPlanRequest request, List<Ingredient> ingredients, boolean savingMode) {
        List<Ingredient> proteins = byCategories(ingredients, "肉蛋水产", "豆制品");
        List<Ingredient> vegetables = byCategories(ingredients, "蔬菜");
        List<Ingredient> staples = byCategories(ingredients, "粮油主食");
        if (proteins.isEmpty()) {
            proteins = ingredients;
        }
        if (vegetables.isEmpty()) {
            vegetables = ingredients;
        }
        if (staples.isEmpty()) {
            staples = ingredients;
        }
        proteins = sortForMode(proteins, savingMode, request.getTaste());
        vegetables = sortForMode(vegetables, savingMode, request.getTaste());
        staples = sortForMode(staples, true, request.getTaste());

        MealPlanResponse response = new MealPlanResponse();
        response.setPeopleCount(request.getPeopleCount());
        response.setTaste(request.getTaste());
        response.setWeeklyBudget(request.getWeeklyBudget().setScale(2, RoundingMode.HALF_UP));
        response.setMonthlySalary(request.getMonthlySalary() == null ? BigDecimal.ZERO : request.getMonthlySalary().setScale(2, RoundingMode.HALF_UP));
        response.setFavoriteDishes(request.getFavoriteDishes() == null ? new ArrayList<String>() : request.getFavoriteDishes());
        response.setOptimized(savingMode);

        BigDecimal weeklyCost = BigDecimal.ZERO;
        for (int i = 0; i < DAYS.size(); i++) {
            MealPlanResponse.DailyPlan day = new MealPlanResponse.DailyPlan();
            day.setDay(DAYS.get(i));
            day.getMeals().add(createBreakfast(i, staples, proteins, vegetables, request, savingMode));
            day.getMeals().add(createLunch(i, proteins, vegetables, staples, request, savingMode));
            day.getMeals().add(createDinner(i, proteins, vegetables, staples, request, savingMode));
            BigDecimal dailyCost = day.getMeals().stream()
                    .map(MealPlanResponse.MealItem::getEstimatedCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            day.setDailyCost(dailyCost);
            weeklyCost = weeklyCost.add(dailyCost);
            response.getDays().add(day);
        }
        response.setWeeklyCost(weeklyCost.setScale(2, RoundingMode.HALF_UP));
        response.setMonthlyFoodCost(weeklyCost.multiply(new BigDecimal("4.3")).setScale(2, RoundingMode.HALF_UP));
        response.setBudgetUsageRate(weeklyCost.divide(request.getWeeklyBudget(), 4, RoundingMode.HALF_UP));
        if (request.getMonthlySalary() != null && request.getMonthlySalary().compareTo(BigDecimal.ZERO) > 0) {
            response.setSalaryUsageRate(response.getMonthlyFoodCost().divide(request.getMonthlySalary(), 4, RoundingMode.HALF_UP));
        } else {
            response.setSalaryUsageRate(BigDecimal.ZERO);
        }
        return response;
    }

    private MealPlanResponse.MealItem createBreakfast(int index, List<Ingredient> staples, List<Ingredient> proteins,
                                                       List<Ingredient> vegetables, MealPlanRequest request, boolean savingMode) {
        Ingredient staple = pick(staples, index);
        Ingredient protein = pick(proteins, index + 1);
        Ingredient vegetable = pick(vegetables, index + 2);
        String name = favoriteOrGenerated(request.getFavoriteDishes(), index, clean(staple.getName()) + clean(protein.getName()) + BREAKFAST_STYLES.get(index));
        List<Ingredient> used = Arrays.asList(staple, protein, vegetable);
        BigDecimal cost = cost(used, request.getPeopleCount(), savingMode ? new BigDecimal("0.16") : new BigDecimal("0.22"));
        return meal("早餐", name, used, cost, "主食搭配蛋白和蔬菜，适合早晨稳定供能", savingMode);
    }

    private MealPlanResponse.MealItem createLunch(int index, List<Ingredient> proteins, List<Ingredient> vegetables,
                                                  List<Ingredient> staples, MealPlanRequest request, boolean savingMode) {
        Ingredient protein = pick(proteins, index);
        Ingredient vegetable = pick(vegetables, index + 1);
        Ingredient staple = pick(staples, index + 2);
        String generated = dishName(request.getTaste(), protein, vegetable, LUNCH_STYLES.get(index));
        String name = favoriteOrGenerated(request.getFavoriteDishes(), index + 7, generated + "配" + clean(staple.getName()));
        List<Ingredient> used = Arrays.asList(protein, vegetable, staple);
        BigDecimal cost = cost(used, request.getPeopleCount(), savingMode ? new BigDecimal("0.24") : new BigDecimal("0.32"));
        return meal("午餐", name, used, cost, "午餐蛋白占比更高，兼顾饱腹和膳食纤维", savingMode);
    }

    private MealPlanResponse.MealItem createDinner(int index, List<Ingredient> proteins, List<Ingredient> vegetables,
                                                   List<Ingredient> staples, MealPlanRequest request, boolean savingMode) {
        Ingredient protein = pick(proteins, index + 3);
        Ingredient vegetable = pick(vegetables, index + 4);
        Ingredient staple = pick(staples, index + 5);
        String generated = dishName(request.getTaste(), protein, vegetable, DINNER_STYLES.get(index));
        String name = favoriteOrGenerated(request.getFavoriteDishes(), index + 14, generated + "配" + clean(staple.getName()));
        List<Ingredient> used = Arrays.asList(protein, vegetable, staple);
        BigDecimal cost = cost(used, request.getPeopleCount(), savingMode ? new BigDecimal("0.20") : new BigDecimal("0.28"));
        return meal("晚餐", name, used, cost, "晚餐减少油腻感，保留足量蔬菜", savingMode);
    }

    private String dishName(String taste, Ingredient protein, Ingredient vegetable, String style) {
        String proteinName = clean(protein.getName());
        String vegetableName = clean(vegetable.getName());
        if ("清淡".equals(taste) || "清汤".equals(style) || "炖汤".equals(style)) {
            return vegetableName + proteinName + style;
        }
        if ("香辣".equals(taste)) {
            return "香辣" + vegetableName + proteinName;
        }
        if ("高蛋白".equals(taste)) {
            return proteinName + vegetableName + style;
        }
        return vegetableName + proteinName + style;
    }

    private String favoriteOrGenerated(List<String> favorites, int index, String generated) {
        if (favorites == null || favorites.isEmpty()) {
            return generated;
        }
        List<String> cleaned = favorites.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
        if (cleaned.isEmpty() || index % 3 != 0) {
            return generated;
        }
        return cleaned.get(Math.floorMod(index, cleaned.size())) + "（偏好菜）";
    }

    private String clean(String name) {
        if (name == null) {
            return "";
        }
        return name.replace("北豆腐", "白豆腐")
                .replace("大白菜", "白菜")
                .replace("西红柿", "番茄")
                .replace("猪里脊", "里脊肉")
                .trim();
    }

    private MealPlanResponse.MealItem meal(String type, String name, List<Ingredient> used, BigDecimal cost,
                                           String note, boolean savingMode) {
        MealPlanResponse.MealItem item = new MealPlanResponse.MealItem();
        item.setMealType(type);
        item.setDishName(name);
        item.setIngredients(used.stream().map(Ingredient::getName).collect(Collectors.toList()));
        item.setEstimatedCost(cost.setScale(2, RoundingMode.HALF_UP));
        item.setNutritionNote(note);
        item.setSavingNote(savingMode ? "已优先选择低价同类食材并缩小高价食材占比" : "当前按常规营养搭配估算");
        return item;
    }

    private BigDecimal cost(List<Ingredient> used, int peopleCount, BigDecimal kgPerPerson) {
        BigDecimal unitSum = used.stream()
                .map(Ingredient::getPricePerKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return unitSum.multiply(kgPerPerson)
                .multiply(BigDecimal.valueOf(peopleCount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<Ingredient> filterAvoided(List<Ingredient> ingredients, List<String> avoidIngredients) {
        if (avoidIngredients == null || avoidIngredients.isEmpty()) {
            return ingredients;
        }
        Set<String> avoid = avoidIngredients.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return ingredients.stream()
                .filter(item -> avoid.stream().noneMatch(word -> item.getName().toLowerCase(Locale.ROOT).contains(word)))
                .collect(Collectors.toList());
    }

    private List<Ingredient> byCategories(List<Ingredient> ingredients, String... categories) {
        Set<String> expected = new HashSet<String>(Arrays.asList(categories));
        return ingredients.stream()
                .filter(item -> expected.stream().anyMatch(category -> item.getCategory() != null && item.getCategory().contains(category)))
                .collect(Collectors.toList());
    }

    private List<Ingredient> sortForMode(List<Ingredient> ingredients, boolean savingMode, String taste) {
        Comparator<Ingredient> comparator = Comparator.comparing(Ingredient::getPricePerKg);
        if (!savingMode) {
            Map<Boolean, List<Ingredient>> grouped = ingredients.stream()
                    .sorted(comparator)
                    .collect(Collectors.partitioningBy(item -> item.getTags() != null && item.getTags().contains(taste)));
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

    private void applyWarning(MealPlanResponse response) {
        BigDecimal rate = response.getBudgetUsageRate();
        BigDecimal salaryRate = response.getSalaryUsageRate() == null ? BigDecimal.ZERO : response.getSalaryUsageRate();
        if (rate.compareTo(BigDecimal.ONE) > 0 || salaryRate.compareTo(new BigDecimal("0.20")) > 0) {
            response.setWarningLevel("HEAVY");
            response.setWarningMessage("食材花销偏高，已尝试替换平价同口味菜品");
        } else if (rate.compareTo(new BigDecimal("0.80")) >= 0 || salaryRate.compareTo(new BigDecimal("0.12")) >= 0) {
            response.setWarningLevel("LIGHT");
            response.setWarningMessage("食材花销接近预算线，建议关注肉类和菌菇类采购量");
        } else {
            response.setWarningLevel("NONE");
            response.setWarningMessage("预算健康，当前菜谱可直接采购");
        }
    }
}