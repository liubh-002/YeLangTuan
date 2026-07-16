package com.practice.mealplanner.controller;

import com.practice.mealplanner.dto.MealPlanRequest;
import com.practice.mealplanner.dto.MealPlanResponse;
import com.practice.mealplanner.dto.OptimizationResult;
import com.practice.mealplanner.model.FamilyGroup;
import com.practice.mealplanner.model.User;
import com.practice.mealplanner.repository.FamilyGroupRepository;
import com.practice.mealplanner.service.AuthService;
import com.practice.mealplanner.service.FamilyMemberService;
import com.practice.mealplanner.service.FamilyGroupService;
import com.practice.mealplanner.skill.MealPlanAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plans")
public class MealPlanController {

    private final MealPlanAgent mealPlanAgent;
    private final FamilyGroupService familyGroupService;
    private final FamilyMemberService familyMemberService;
    private final FamilyGroupRepository familyGroupRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @PostMapping("/generate")
    public ResponseEntity<MealPlanResponse> generate(HttpServletRequest request,
                                                      @Valid @RequestBody MealPlanRequest planRequest) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        String mode = familyGroupService.getMode(user.getId());

        if ("FAMILY".equals(mode)) {
            return ResponseEntity.ok(generateFamilyPlan(user, planRequest));
        } else {
            return ResponseEntity.ok(generatePersonalPlan(user, planRequest));
        }
    }

    @PostMapping("/replace-ingredient")
    public ResponseEntity<String> replaceIngredient(HttpServletRequest request,
                                                     @RequestBody Map<String, Object> body) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        String dishName = (String) body.get("dishName");
        List<String> currentIngredients = (List<String>) body.get("currentIngredients");
        String replaceIngredient = (String) body.get("replaceIngredient");
        String taste = (String) body.get("taste");
        int peopleCount = body.get("peopleCount") != null ? ((Number) body.get("peopleCount")).intValue() : 1;

        String resultJson = mealPlanAgent.replaceIngredient(dishName, currentIngredients, replaceIngredient, taste, peopleCount);
        return ResponseEntity.ok(resultJson);
    }

    @PostMapping("/custom-dish")
    public ResponseEntity<OptimizationResult> addCustomDish(HttpServletRequest request,
                                                             @RequestBody Map<String, Object> body) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        String dishName = (String) body.get("dishName");
        List<String> ingredients = (List<String>) body.get("ingredients");
        BigDecimal estimatedCost = new BigDecimal(body.get("estimatedCost").toString());
        BigDecimal currentWeeklyCost = new BigDecimal(body.get("currentWeeklyCost").toString());
        BigDecimal weeklyBudget = new BigDecimal(body.get("weeklyBudget").toString());

        String resultJson = mealPlanAgent.recalcCustomDish(dishName, ingredients, estimatedCost,
                currentWeeklyCost, weeklyBudget);

        try {
            return ResponseEntity.ok(objectMapper.readValue(resultJson, OptimizationResult.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析优化结果失败", e);
        }
    }

    @PostMapping("/edit-dish")
    public ResponseEntity<MealPlanResponse> editDish(HttpServletRequest request,
                                                      @RequestBody Map<String, Object> body) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        String mode = familyGroupService.getMode(user.getId());
        BigDecimal weeklyBudget = new BigDecimal(body.get("weeklyBudget").toString());
        BigDecimal monthlySalary = body.get("monthlySalary") != null ?
                new BigDecimal(body.get("monthlySalary").toString()) : BigDecimal.ZERO;

        MealPlanRequest planRequest = buildRequest(body, weeklyBudget, monthlySalary);
        
        String resultJson;
        if ("FAMILY".equals(mode)) {
            resultJson = mealPlanAgent.generateFamilyPlanWithCustomDish(
                    familyMemberService.getMembersByFamily(
                            familyGroupRepository.findByMasterUserId(user.getId()).orElseThrow(() -> new RuntimeException("家庭组不存在")).getId()),
                    planRequest.getPeopleCount(),
                    planRequest.getTaste(),
                    planRequest.getWeeklyBudget(),
                    planRequest.getMonthlySalary(),
                    planRequest.getBreakfastWant(),
                    planRequest.getLunchWant(),
                    planRequest.getDinnerWant(),
                    planRequest.isSavingMode(),
                    (int) body.get("dayIndex"),
                    (int) body.get("mealIndex"),
                    (String) body.get("newDishName")
            );
        } else {
            resultJson = mealPlanAgent.generatePersonalPlanWithCustomDish(
                    planRequest.getPeopleCount(),
                    planRequest.getTaste(),
                    planRequest.getWeeklyBudget(),
                    planRequest.getMonthlySalary(),
                    planRequest.getAvoidIngredients(),
                    planRequest.getFavoriteDishes(),
                    planRequest.getBreakfastWant(),
                    planRequest.getLunchWant(),
                    planRequest.getDinnerWant(),
                    planRequest.isSavingMode(),
                    (int) body.get("dayIndex"),
                    (int) body.get("mealIndex"),
                    (String) body.get("newDishName")
            );
        }

        try {
            MealPlanResponse response = objectMapper.readValue(resultJson, MealPlanResponse.class);
            response.setTaste(planRequest.getTaste());
            response.setPeopleCount(planRequest.getPeopleCount());
            response.setWeeklyBudget(planRequest.getWeeklyBudget());
            response.setMonthlySalary(planRequest.getMonthlySalary());
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析菜谱失败", e);
        }
    }

    private MealPlanResponse generatePersonalPlan(User user, MealPlanRequest request) {
        List<String> avoidIngredients = request.getAvoidIngredients();
        if (user.getDietTaboo() != null && !"无".equals(user.getDietTaboo())) {
            avoidIngredients.addAll(java.util.Arrays.asList(user.getDietTaboo().split("[，,]")));
        }

        String taste = request.getTaste();
        if (user.getTastePrefer() != null && !user.getTastePrefer().isEmpty()) {
            taste = user.getTastePrefer();
        }

        String resultJson = mealPlanAgent.generatePersonalPlan(
                request.getPeopleCount(),
                taste,
                request.getWeeklyBudget(),
                request.getMonthlySalary(),
                avoidIngredients,
                request.getFavoriteDishes(),
                request.getBreakfastWant(),
                request.getLunchWant(),
                request.getDinnerWant(),
                request.getCustomRequirements(),
                request.isSavingMode()
        );

        try {
            MealPlanResponse response = objectMapper.readValue(resultJson, MealPlanResponse.class);
            response.setTaste(taste);
            response.setPeopleCount(request.getPeopleCount());
            response.setWeeklyBudget(request.getWeeklyBudget());
            response.setMonthlySalary(request.getMonthlySalary());
            response.setFavoriteDishes(request.getFavoriteDishes());
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析菜谱失败", e);
        }
    }

    private MealPlanResponse generateFamilyPlan(User user, MealPlanRequest request) {
        FamilyGroup group = familyGroupRepository.findByMasterUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("家庭组不存在"));

        List<com.practice.mealplanner.dto.FamilyMemberResponse> members = familyMemberService.getMembersByFamily(group.getId());
        List<String> allTaboos = members.stream()
                .filter(m -> m.getDietTaboo() != null && !"无".equals(m.getDietTaboo()))
                .flatMap(m -> java.util.Arrays.asList(m.getDietTaboo().split("[，,]")).stream())
                .collect(Collectors.toList());

        String resultJson = mealPlanAgent.generateFamilyPlan(
                members,
                request.getPeopleCount(),
                request.getTaste(),
                request.getWeeklyBudget(),
                request.getMonthlySalary()
        );

        try {
            MealPlanResponse response = objectMapper.readValue(resultJson, MealPlanResponse.class);
            response.setTaste(request.getTaste());
            response.setPeopleCount(request.getPeopleCount());
            response.setWeeklyBudget(request.getWeeklyBudget());
            response.setMonthlySalary(request.getMonthlySalary());
            response.setFavoriteDishes(request.getFavoriteDishes());
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析菜谱失败", e);
        }
    }

    private MealPlanRequest buildRequest(Map<String, Object> body, BigDecimal weeklyBudget, BigDecimal monthlySalary) {
        MealPlanRequest request = new MealPlanRequest();
        request.setPeopleCount(body.get("peopleCount") != null ? ((Number) body.get("peopleCount")).intValue() : 1);
        request.setTaste((String) body.get("taste"));
        request.setWeeklyBudget(weeklyBudget);
        request.setMonthlySalary(monthlySalary);
        request.setAvoidIngredients((List<String>) body.get("avoidIngredients"));
        request.setFavoriteDishes((List<String>) body.get("favoriteDishes"));
        request.setBreakfastWant((List<String>) body.get("breakfastWant"));
        request.setLunchWant((List<String>) body.get("lunchWant"));
        request.setDinnerWant((List<String>) body.get("dinnerWant"));
        request.setSavingMode(body.get("savingMode") != null ? (Boolean) body.get("savingMode") : false);
        return request;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未提供认证令牌");
        }
        return authHeader.substring(7);
    }
}
