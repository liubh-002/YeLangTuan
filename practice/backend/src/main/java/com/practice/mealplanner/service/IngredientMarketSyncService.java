package com.practice.mealplanner.service;

import com.practice.mealplanner.model.Ingredient;
import com.practice.mealplanner.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientMarketSyncService {

    private final IngredientRepository ingredientRepository;

    public void syncMarketData() {
        List<Ingredient> defaultIngredients = Arrays.asList(
                createIngredient("白菜", new BigDecimal("3.50"), "kg", "蔬菜", "清淡,常见", "全年", "富含维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("番茄", new BigDecimal("4.50"), "kg", "蔬菜", "酸甜,维生素", "夏秋", "富含番茄红素", "本地菜市场", LocalDate.now()),
                createIngredient("土豆", new BigDecimal("2.80"), "kg", "蔬菜", "淀粉,主食", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("胡萝卜", new BigDecimal("3.20"), "kg", "蔬菜", "维生素A,护眼", "秋冬", "β-胡萝卜素", "本地菜市场", LocalDate.now()),
                createIngredient("青椒", new BigDecimal("5.00"), "kg", "蔬菜", "维生素C,辣", "夏秋", "维生素C含量高", "本地菜市场", LocalDate.now()),
                createIngredient("黄瓜", new BigDecimal("4.00"), "kg", "蔬菜", "清爽,补水", "夏季", "水分充足", "本地菜市场", LocalDate.now()),
                createIngredient("冬瓜", new BigDecimal("2.50"), "kg", "蔬菜", "清淡,利水", "夏秋", "低热量高水分", "本地菜市场", LocalDate.now()),
                createIngredient("南瓜", new BigDecimal("3.00"), "kg", "蔬菜", "甜,维生素", "秋季", "富含β-胡萝卜素", "本地菜市场", LocalDate.now()),
                createIngredient("西兰花", new BigDecimal("8.00"), "kg", "蔬菜", "营养,健康", "全年", "抗癌蔬菜", "本地菜市场", LocalDate.now()),
                createIngredient("菠菜", new BigDecimal("6.00"), "kg", "蔬菜", "铁,维生素", "春秋", "铁含量丰富", "本地菜市场", LocalDate.now()),
                createIngredient("芹菜", new BigDecimal("4.50"), "kg", "蔬菜", "粗纤维,降压", "全年", "粗纤维含量高", "本地菜市场", LocalDate.now()),
                createIngredient("洋葱", new BigDecimal("3.80"), "kg", "蔬菜", "杀菌,调味", "全年", "含前列腺素", "本地菜市场", LocalDate.now()),
                createIngredient("蒜", new BigDecimal("8.00"), "kg", "蔬菜", "杀菌,调味", "全年", "大蒜素", "本地菜市场", LocalDate.now()),
                createIngredient("姜", new BigDecimal("12.00"), "kg", "蔬菜", "驱寒,调味", "全年", "姜辣素", "本地菜市场", LocalDate.now()),
                createIngredient("葱", new BigDecimal("5.00"), "kg", "蔬菜", "调味,杀菌", "全年", "挥发性精油", "本地菜市场", LocalDate.now()),
                createIngredient("猪肉", new BigDecimal("28.00"), "kg", "肉蛋水产", "蛋白质,常见", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("里脊肉", new BigDecimal("35.00"), "kg", "肉蛋水产", "瘦肉,高蛋白", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("排骨", new BigDecimal("32.00"), "kg", "肉蛋水产", "钙质,美味", "全年", "钙含量高", "本地菜市场", LocalDate.now()),
                createIngredient("鸡腿", new BigDecimal("22.00"), "kg", "肉蛋水产", "蛋白质,低脂", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鸡胸肉", new BigDecimal("25.00"), "kg", "肉蛋水产", "低脂,高蛋白", "全年", "低脂肪高蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鸡蛋", new BigDecimal("18.00"), "kg", "肉蛋水产", "蛋白质,营养", "全年", "优质蛋白来源", "本地菜市场", LocalDate.now()),
                createIngredient("鲫鱼", new BigDecimal("15.00"), "kg", "肉蛋水产", "蛋白质,补钙", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("草鱼", new BigDecimal("12.00"), "kg", "肉蛋水产", "蛋白质,经济", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("白豆腐", new BigDecimal("6.00"), "kg", "豆制品", "蛋白质,清淡", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("千张", new BigDecimal("10.00"), "kg", "豆制品", "蛋白质,口感", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("腐竹", new BigDecimal("25.00"), "kg", "豆制品", "蛋白质,干货", "全年", "高蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("大米", new BigDecimal("5.00"), "kg", "粮油主食", "碳水,主食", "全年", "主要能量来源", "本地菜市场", LocalDate.now()),
                createIngredient("小米", new BigDecimal("8.00"), "kg", "粮油主食", "营养,粗粮", "全年", "维生素B族", "本地菜市场", LocalDate.now()),
                createIngredient("面粉", new BigDecimal("4.50"), "kg", "粮油主食", "碳水,面食", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("面条", new BigDecimal("6.00"), "kg", "粮油主食", "碳水,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("花生油", new BigDecimal("80.00"), "L", "粮油主食", "调味,健康", "全年", "不饱和脂肪酸", "本地菜市场", LocalDate.now()),
                createIngredient("盐", new BigDecimal("3.00"), "kg", "粮油主食", "调味,必需", "全年", "钠", "本地菜市场", LocalDate.now()),
                createIngredient("糖", new BigDecimal("6.00"), "kg", "粮油主食", "调味,能量", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("酱油", new BigDecimal("15.00"), "L", "粮油主食", "调味,增色", "全年", "氨基酸", "本地菜市场", LocalDate.now()),
                createIngredient("牛奶", new BigDecimal("12.00"), "L", "肉蛋水产", "蛋白质,钙质", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("豆浆", new BigDecimal("5.00"), "L", "豆制品", "植物蛋白,健康", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("油条", new BigDecimal("15.00"), "kg", "粮油主食", "碳水,早餐", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("包子", new BigDecimal("20.00"), "kg", "粮油主食", "碳水,早餐", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("馒头", new BigDecimal("8.00"), "kg", "粮油主食", "碳水,主食", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("豆腐脑", new BigDecimal("8.00"), "kg", "豆制品", "清淡,早餐", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("麦片", new BigDecimal("30.00"), "kg", "粮油主食", "纤维,早餐", "全年", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("玉米", new BigDecimal("4.00"), "kg", "粮油主食", "纤维,粗粮", "夏季", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("红薯", new BigDecimal("3.00"), "kg", "粮油主食", "纤维,粗粮", "秋季", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("香菇", new BigDecimal("15.00"), "kg", "蔬菜", "鲜味,营养", "全年", "香菇多糖", "本地菜市场", LocalDate.now()),
                createIngredient("木耳", new BigDecimal("40.00"), "kg", "蔬菜", "纤维,干货", "全年", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("海带", new BigDecimal("10.00"), "kg", "蔬菜", "碘,健康", "全年", "碘含量高", "本地菜市场", LocalDate.now())
        );

        for (Ingredient ingredient : defaultIngredients) {
            if (!ingredientRepository.existsByName(ingredient.getName())) {
                ingredientRepository.save(ingredient);
                log.info("Added ingredient: {}", ingredient.getName());
            }
        }
    }

    private Ingredient createIngredient(String name, BigDecimal price, String unit, String category,
                                        String tags, String season, String nutrition, String source, LocalDate date) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setPricePerKg(price);
        ingredient.setUnit(unit);
        ingredient.setCategory(category);
        ingredient.setTags(tags);
        ingredient.setSeason(season);
        ingredient.setNutritionInfo(nutrition);
        ingredient.setSource(source);
        ingredient.setUpdateTime(date.toString());
        ingredient.setIsAvailable(true);
        return ingredient;
    }
}