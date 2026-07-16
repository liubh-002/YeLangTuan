package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.Ingredient;
import com.practice.mealplanner.repository.IngredientRepository;
import com.practice.mealplanner.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeController {

    private final IngredientRepository ingredientRepository;
    private final AiService aiService;

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String keyword) {
        String result = aiService.generateRecipes(keyword);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        
        List<Ingredient> ingredients = ingredientRepository.findByNameContaining(keyword);
        if (ingredients.isEmpty()) {
            return ResponseEntity.ok(generateMockRecipes(keyword));
        }
        
        return ResponseEntity.ok(generateMockRecipes(keyword));
    }

    private String generateMockRecipes(String ingredient) {
        List<String> recipes = new ArrayList<String>();
        
        recipes.add("{\"name\":\"清炒" + ingredient + "\",\"category\":\"家常菜\",\"tags\":\"清淡,快手\",\"ingredients\":[\"" + ingredient + "\",\"大蒜\",\"盐\",\"生抽\"],\"steps\":[\"" + ingredient + "洗净切片\",\"大蒜切末\",\"热锅放油爆香大蒜\",\"放入" + ingredient + "翻炒\",\"加盐和生抽调味\",\"翻炒均匀出锅\"],\"difficulty\":\"简单\",\"suitableFor\":[\"普通人群\",\"减肥人士\"]}");
        recipes.add("{\"name\":\"" + ingredient + "炒肉\",\"category\":\"家常菜\",\"tags\":\"下饭,营养\",\"ingredients\":[\"" + ingredient + "\",\"猪肉\",\"葱姜\",\"料酒\",\"生抽\",\"盐\"],\"steps\":[\"猪肉切片用料酒腌制\",\"" + ingredient + "洗净切块\",\"热锅放油炒肉片变色\",\"加入葱姜爆香\",\"放入" + ingredient + "翻炒\",\"加生抽和盐调味\",\"翻炒均匀出锅\"],\"difficulty\":\"中等\",\"suitableFor\":[\"普通人群\",\"青少年\"]}");
        recipes.add("{\"name\":\"" + ingredient + "鸡蛋汤\",\"category\":\"汤羹\",\"tags\":\"清淡,营养\",\"ingredients\":[\"" + ingredient + "\",\"鸡蛋\",\"葱花\",\"盐\",\"香油\"],\"steps\":[\"鸡蛋打散备用\",\"" + ingredient + "洗净切丝\",\"锅中加水烧开\",\"放入" + ingredient + "丝煮2分钟\",\"缓缓倒入蛋液\",\"加盐调味\",\"撒葱花淋香油\"],\"difficulty\":\"简单\",\"suitableFor\":[\"普通人群\",\"病号\"]}");
        recipes.add("{\"name\":\"红烧" + ingredient + "\",\"category\":\"家常菜\",\"tags\":\"浓郁,下饭\",\"ingredients\":[\"" + ingredient + "\",\"五花肉\",\"葱姜蒜\",\"酱油\",\"糖\",\"八角\"],\"steps\":[\"五花肉切块焯水\",\"" + ingredient + "切块\",\"锅中放油炒糖色\",\"加入五花肉翻炒上色\",\"加葱姜蒜和八角爆香\",\"加水没过食材\",\"放入" + ingredient + "\",\"加酱油调味\",\"小火炖煮30分钟\",\"大火收汁\"],\"difficulty\":\"中等\",\"suitableFor\":[\"普通人群\",\"老年人\"]}");
        recipes.add("{\"name\":\"蒜蓉蒸" + ingredient + "\",\"category\":\"蒸菜\",\"tags\":\"健康,低脂\",\"ingredients\":[\"" + ingredient + "\",\"大蒜\",\"粉丝\",\"生抽\",\"蚝油\",\"葱花\"],\"steps\":[\"粉丝泡软铺盘底\",\"" + ingredient + "洗净铺在粉丝上\",\"大蒜切末\",\"热油爆香蒜末\",\"加生抽和蚝油调成酱汁\",\"淋在" + ingredient + "上\",\"蒸锅蒸10分钟\",\"撒葱花即可\"],\"difficulty\":\"简单\",\"suitableFor\":[\"减肥人士\",\"健康人群\"]}");
        
        return "[" + String.join(",", recipes) + "]";
    }

    @GetMapping("/detail/{name}")
    public ResponseEntity<String> getDetail(@PathVariable String name) {
        return ResponseEntity.ok(aiService.generateDishInfo(name, "午餐"));
    }
}