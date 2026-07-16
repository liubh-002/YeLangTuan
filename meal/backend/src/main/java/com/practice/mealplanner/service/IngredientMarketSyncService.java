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
                createIngredient("海带", new BigDecimal("10.00"), "kg", "蔬菜", "碘,健康", "全年", "碘含量高", "本地菜市场", LocalDate.now()),
                createIngredient("全脂奶粉", new BigDecimal("60.00"), "kg", "肉蛋水产", "蛋白质,钙质", "全年", "优质蛋白", "超市", LocalDate.now()),
                createIngredient("白砂糖", new BigDecimal("8.00"), "kg", "粮油主食", "调味,能量", "全年", "碳水化合物", "超市", LocalDate.now()),
                createIngredient("植物油", new BigDecimal("60.00"), "L", "粮油主食", "调味,烹饪", "全年", "不饱和脂肪酸", "超市", LocalDate.now()),
                createIngredient("料酒", new BigDecimal("12.00"), "L", "粮油主食", "调味,去腥", "全年", "去腥增香", "超市", LocalDate.now()),
                createIngredient("香醋", new BigDecimal("10.00"), "L", "粮油主食", "调味,酸味", "全年", "乙酸", "超市", LocalDate.now()),
                createIngredient("辣椒油", new BigDecimal("30.00"), "L", "粮油主食", "调味,辣味", "全年", "辣椒素", "超市", LocalDate.now()),
                createIngredient("胡椒粉", new BigDecimal("50.00"), "kg", "粮油主食", "调味,去腥", "全年", "胡椒碱", "超市", LocalDate.now()),
                createIngredient("鸡精", new BigDecimal("35.00"), "kg", "粮油主食", "调味,增鲜", "全年", "谷氨酸钠", "超市", LocalDate.now()),
                createIngredient("味精", new BigDecimal("25.00"), "kg", "粮油主食", "调味,增鲜", "全年", "谷氨酸钠", "超市", LocalDate.now()),
                createIngredient("香油", new BigDecimal("45.00"), "L", "粮油主食", "调味,增香", "全年", "芝麻香气", "超市", LocalDate.now()),
                createIngredient("豆瓣酱", new BigDecimal("18.00"), "kg", "粮油主食", "调味,香辣", "全年", "发酵豆制品", "超市", LocalDate.now()),
                createIngredient("火锅底料", new BigDecimal("40.00"), "kg", "粮油主食", "调味,麻辣", "全年", "复合调味料", "超市", LocalDate.now()),
                createIngredient("花椒", new BigDecimal("60.00"), "kg", "粮油主食", "调味,麻味", "全年", "花椒素", "超市", LocalDate.now()),
                createIngredient("干辣椒", new BigDecimal("35.00"), "kg", "粮油主食", "调味,辣味", "全年", "辣椒素", "超市", LocalDate.now()),
                createIngredient("茴香", new BigDecimal("45.00"), "kg", "粮油主食", "调味,香料", "全年", "挥发性精油", "超市", LocalDate.now()),
                createIngredient("桂皮", new BigDecimal("50.00"), "kg", "粮油主食", "调味,香料", "全年", "肉桂醛", "超市", LocalDate.now()),
                createIngredient("八角", new BigDecimal("40.00"), "kg", "粮油主食", "调味,香料", "全年", "挥发性精油", "超市", LocalDate.now()),
                createIngredient("料酒", new BigDecimal("12.00"), "L", "粮油主食", "调味,去腥", "全年", "去腥增香", "超市", LocalDate.now()),
                createIngredient("草鱼", new BigDecimal("12.00"), "kg", "肉蛋水产", "蛋白质,经济", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鲈鱼", new BigDecimal("35.00"), "kg", "肉蛋水产", "蛋白质,鲜美", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鲤鱼", new BigDecimal("14.00"), "kg", "肉蛋水产", "蛋白质,经济", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("虾", new BigDecimal("45.00"), "kg", "肉蛋水产", "蛋白质,鲜美", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("蟹", new BigDecimal("60.00"), "kg", "肉蛋水产", "蛋白质,鲜美", "秋季", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("牛肉", new BigDecimal("65.00"), "kg", "肉蛋水产", "蛋白质,优质", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("羊肉", new BigDecimal("75.00"), "kg", "肉蛋水产", "蛋白质,温补", "冬季", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("牛腩", new BigDecimal("55.00"), "kg", "肉蛋水产", "蛋白质,炖菜", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("牛腱子", new BigDecimal("60.00"), "kg", "肉蛋水产", "蛋白质,腱肉", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("五花肉", new BigDecimal("30.00"), "kg", "肉蛋水产", "脂肪,红烧", "全年", "肥瘦相间", "本地菜市场", LocalDate.now()),
                createIngredient("猪蹄", new BigDecimal("28.00"), "kg", "肉蛋水产", "胶原蛋白", "全年", "胶原蛋白丰富", "本地菜市场", LocalDate.now()),
                createIngredient("牛百叶", new BigDecimal("40.00"), "kg", "肉蛋水产", "口感,火锅", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("鸭", new BigDecimal("28.00"), "kg", "肉蛋水产", "蛋白质,鸭肉", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鸭腿", new BigDecimal("20.00"), "kg", "肉蛋水产", "蛋白质,鸭肉", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鸭血", new BigDecimal("8.00"), "kg", "肉蛋水产", "铁,补血", "全年", "铁含量高", "本地菜市场", LocalDate.now()),
                createIngredient("鹅", new BigDecimal("55.00"), "kg", "肉蛋水产", "蛋白质,鹅肉", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鸽子", new BigDecimal("45.00"), "kg", "肉蛋水产", "蛋白质,滋补", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鹌鹑蛋", new BigDecimal("25.00"), "kg", "肉蛋水产", "蛋白质,小巧", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("鸭蛋", new BigDecimal("22.00"), "kg", "肉蛋水产", "蛋白质,营养", "全年", "优质蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("皮蛋", new BigDecimal("30.00"), "kg", "肉蛋水产", "风味,独特", "全年", "碱性食品", "本地菜市场", LocalDate.now()),
                createIngredient("咸蛋", new BigDecimal("28.00"), "kg", "肉蛋水产", "风味,独特", "全年", "盐渍蛋", "本地菜市场", LocalDate.now()),
                createIngredient("韭菜", new BigDecimal("8.00"), "kg", "蔬菜", "调味,壮阳", "春季", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("香菜", new BigDecimal("15.00"), "kg", "蔬菜", "调味,香气", "全年", "挥发油", "本地菜市场", LocalDate.now()),
                createIngredient("生菜", new BigDecimal("6.00"), "kg", "蔬菜", "清爽,生食", "全年", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("油麦菜", new BigDecimal("7.00"), "kg", "蔬菜", "清爽,绿叶", "全年", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("茼蒿", new BigDecimal("9.00"), "kg", "蔬菜", "风味,独特", "春秋", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("苋菜", new BigDecimal("8.00"), "kg", "蔬菜", "铁,补血", "夏季", "铁含量高", "本地菜市场", LocalDate.now()),
                createIngredient("空心菜", new BigDecimal("6.00"), "kg", "蔬菜", "清爽,夏季", "夏季", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("芥蓝", new BigDecimal("10.00"), "kg", "蔬菜", "营养,健康", "全年", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("芦笋", new BigDecimal("25.00"), "kg", "蔬菜", "营养,高档", "春季", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("秋葵", new BigDecimal("18.00"), "kg", "蔬菜", "黏液,营养", "夏季", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("莲藕", new BigDecimal("8.00"), "kg", "蔬菜", "脆嫩,营养", "秋季", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("山药", new BigDecimal("12.00"), "kg", "蔬菜", "滋补,健康", "秋冬", "黏液蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("芋头", new BigDecimal("6.00"), "kg", "蔬菜", "淀粉,软糯", "秋季", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("土豆粉", new BigDecimal("15.00"), "kg", "粮油主食", "淀粉,粉条", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("红薯粉", new BigDecimal("12.00"), "kg", "粮油主食", "淀粉,粉条", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("绿豆", new BigDecimal("10.00"), "kg", "粮油主食", "清热,粗粮", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("红豆", new BigDecimal("12.00"), "kg", "粮油主食", "补血,粗粮", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("黄豆", new BigDecimal("8.00"), "kg", "粮油主食", "蛋白,粗粮", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("黑豆", new BigDecimal("15.00"), "kg", "粮油主食", "补肾,粗粮", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("燕麦", new BigDecimal("12.00"), "kg", "粮油主食", "纤维,健康", "全年", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("糯米", new BigDecimal("8.00"), "kg", "粮油主食", "黏性,粽子", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("黑米", new BigDecimal("15.00"), "kg", "粮油主食", "营养,粗粮", "全年", "花青素", "本地菜市场", LocalDate.now()),
                createIngredient("紫米", new BigDecimal("18.00"), "kg", "粮油主食", "营养,粗粮", "全年", "花青素", "本地菜市场", LocalDate.now()),
                createIngredient("荞麦", new BigDecimal("10.00"), "kg", "粮油主食", "健康,粗粮", "全年", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("高粱", new BigDecimal("9.00"), "kg", "粮油主食", "健康,粗粮", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("青稞", new BigDecimal("15.00"), "kg", "粮油主食", "健康,粗粮", "全年", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("小米面", new BigDecimal("12.00"), "kg", "粮油主食", "健康,面食", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("玉米面", new BigDecimal("8.00"), "kg", "粮油主食", "健康,面食", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("荞麦面", new BigDecimal("15.00"), "kg", "粮油主食", "健康,面食", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("馄饨皮", new BigDecimal("12.00"), "kg", "粮油主食", "面食,馄饨", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("饺子皮", new BigDecimal("10.00"), "kg", "粮油主食", "面食,饺子", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("汤圆粉", new BigDecimal("15.00"), "kg", "粮油主食", "糯米,汤圆", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("年糕", new BigDecimal("12.00"), "kg", "粮油主食", "糯米,传统", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("粽子", new BigDecimal("40.00"), "kg", "粮油主食", "糯米,传统", "端午", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("月饼", new BigDecimal("80.00"), "kg", "粮油主食", "传统,中秋", "中秋", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("麻花", new BigDecimal("30.00"), "kg", "粮油主食", "油炸,零食", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("饼干", new BigDecimal("40.00"), "kg", "粮油主食", "零食,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("蛋糕", new BigDecimal("60.00"), "kg", "粮油主食", "甜点,生日", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("面包", new BigDecimal("30.00"), "kg", "粮油主食", "早餐,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("吐司", new BigDecimal("35.00"), "kg", "粮油主食", "早餐,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("三明治", new BigDecimal("50.00"), "kg", "粮油主食", "早餐,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("汉堡", new BigDecimal("60.00"), "kg", "粮油主食", "快餐,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("披萨", new BigDecimal("80.00"), "kg", "粮油主食", "西餐,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("寿司", new BigDecimal("70.00"), "kg", "粮油主食", "日料,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("方便面", new BigDecimal("40.00"), "kg", "粮油主食", "快餐,方便", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("速冻水饺", new BigDecimal("35.00"), "kg", "粮油主食", "方便,快捷", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("速冻包子", new BigDecimal("30.00"), "kg", "粮油主食", "方便,快捷", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("速冻汤圆", new BigDecimal("40.00"), "kg", "粮油主食", "方便,快捷", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("火腿肠", new BigDecimal("35.00"), "kg", "肉蛋水产", "方便,快捷", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("午餐肉", new BigDecimal("50.00"), "kg", "肉蛋水产", "方便,快捷", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("腊肉", new BigDecimal("60.00"), "kg", "肉蛋水产", "风味,独特", "冬季", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("香肠", new BigDecimal("45.00"), "kg", "肉蛋水产", "风味,独特", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("鱼丸", new BigDecimal("30.00"), "kg", "肉蛋水产", "火锅,方便", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("虾丸", new BigDecimal("45.00"), "kg", "肉蛋水产", "火锅,方便", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("蟹棒", new BigDecimal("35.00"), "kg", "肉蛋水产", "火锅,方便", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("豆腐皮", new BigDecimal("12.00"), "kg", "豆制品", "口感,层次", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("油豆腐", new BigDecimal("10.00"), "kg", "豆制品", "吸味,火锅", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("臭豆腐", new BigDecimal("15.00"), "kg", "豆制品", "风味,独特", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("霉豆腐", new BigDecimal("20.00"), "kg", "豆制品", "风味,独特", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("腐竹皮", new BigDecimal("30.00"), "kg", "豆制品", "干货,火锅", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("豆干", new BigDecimal("18.00"), "kg", "豆制品", "口感,零食", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("素鸡", new BigDecimal("15.00"), "kg", "豆制品", "口感,仿肉", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("面筋", new BigDecimal("12.00"), "kg", "豆制品", "口感,吸味", "全年", "植物蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("魔芋", new BigDecimal("8.00"), "kg", "蔬菜", "低卡,健康", "全年", "膳食纤维", "本地菜市场", LocalDate.now()),
                createIngredient("银耳", new BigDecimal("80.00"), "kg", "蔬菜", "滋补,甜品", "全年", "胶原蛋白", "本地菜市场", LocalDate.now()),
                createIngredient("百合", new BigDecimal("60.00"), "kg", "蔬菜", "滋补,健康", "秋季", "淀粉", "本地菜市场", LocalDate.now()),
                createIngredient("莲子", new BigDecimal("50.00"), "kg", "蔬菜", "滋补,健康", "秋季", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("枸杞", new BigDecimal("120.00"), "kg", "蔬菜", "滋补,健康", "全年", "枸杞多糖", "本地菜市场", LocalDate.now()),
                createIngredient("红枣", new BigDecimal("35.00"), "kg", "蔬菜", "补血,健康", "全年", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("桂圆", new BigDecimal("40.00"), "kg", "蔬菜", "补血,健康", "秋季", "葡萄糖", "本地菜市场", LocalDate.now()),
                createIngredient("核桃", new BigDecimal("80.00"), "kg", "蔬菜", "补脑,健康", "秋季", "不饱和脂肪酸", "本地菜市场", LocalDate.now()),
                createIngredient("杏仁", new BigDecimal("100.00"), "kg", "蔬菜", "健康,坚果", "全年", "不饱和脂肪酸", "本地菜市场", LocalDate.now()),
                createIngredient("花生", new BigDecimal("25.00"), "kg", "蔬菜", "健康,坚果", "秋季", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("瓜子", new BigDecimal("20.00"), "kg", "蔬菜", "零食,坚果", "全年", "脂肪", "本地菜市场", LocalDate.now()),
                createIngredient("芝麻", new BigDecimal("35.00"), "kg", "蔬菜", "健康,调料", "全年", "不饱和脂肪酸", "本地菜市场", LocalDate.now()),
                createIngredient("蜂蜜", new BigDecimal("120.00"), "kg", "粮油主食", "天然,甜味", "全年", "葡萄糖", "本地菜市场", LocalDate.now()),
                createIngredient("冰糖", new BigDecimal("10.00"), "kg", "粮油主食", "调味,甜品", "全年", "蔗糖", "本地菜市场", LocalDate.now()),
                createIngredient("红糖", new BigDecimal("12.00"), "kg", "粮油主食", "补血,调味", "全年", "蔗糖", "本地菜市场", LocalDate.now()),
                createIngredient("黑糖", new BigDecimal("15.00"), "kg", "粮油主食", "滋补,调味", "全年", "蔗糖", "本地菜市场", LocalDate.now()),
                createIngredient("麦芽糖", new BigDecimal("25.00"), "kg", "粮油主食", "传统,甜味", "全年", "麦芽糖", "本地菜市场", LocalDate.now()),
                createIngredient("木糖醇", new BigDecimal("60.00"), "kg", "粮油主食", "无糖,甜味", "全年", "代糖", "本地菜市场", LocalDate.now()),
                createIngredient("淀粉", new BigDecimal("8.00"), "kg", "粮油主食", "勾芡,烹饪", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("面粉淀粉", new BigDecimal("6.00"), "kg", "粮油主食", "勾芡,烹饪", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("玉米淀粉", new BigDecimal("7.00"), "kg", "粮油主食", "勾芡,烹饪", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("红薯淀粉", new BigDecimal("10.00"), "kg", "粮油主食", "勾芡,烹饪", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("藕粉", new BigDecimal("40.00"), "kg", "粮油主食", "早餐,方便", "全年", "淀粉", "本地菜市场", LocalDate.now()),
                createIngredient("葛根粉", new BigDecimal("80.00"), "kg", "粮油主食", "滋补,健康", "全年", "淀粉", "本地菜市场", LocalDate.now()),
                createIngredient("山药粉", new BigDecimal("60.00"), "kg", "粮油主食", "滋补,健康", "全年", "淀粉", "本地菜市场", LocalDate.now()),
                createIngredient("蛋白粉", new BigDecimal("200.00"), "kg", "肉蛋水产", "健身,补剂", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("奶粉", new BigDecimal("80.00"), "kg", "肉蛋水产", "婴儿,营养", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("酸奶", new BigDecimal("15.00"), "L", "肉蛋水产", "发酵,健康", "全年", "益生菌", "本地菜市场", LocalDate.now()),
                createIngredient("奶酪", new BigDecimal("80.00"), "kg", "肉蛋水产", "发酵,营养", "全年", "蛋白质", "本地菜市场", LocalDate.now()),
                createIngredient("黄油", new BigDecimal("60.00"), "kg", "肉蛋水产", "烘焙,调味", "全年", "脂肪", "本地菜市场", LocalDate.now()),
                createIngredient("奶油", new BigDecimal("50.00"), "L", "肉蛋水产", "烘焙,调味", "全年", "脂肪", "本地菜市场", LocalDate.now()),
                createIngredient("炼乳", new BigDecimal("40.00"), "kg", "肉蛋水产", "调味,甜点", "全年", "牛奶浓缩", "本地菜市场", LocalDate.now()),
                createIngredient("奶茶粉", new BigDecimal("30.00"), "kg", "粮油主食", "冲调,饮品", "全年", "碳水化合物", "本地菜市场", LocalDate.now()),
                createIngredient("咖啡粉", new BigDecimal("100.00"), "kg", "粮油主食", "冲调,饮品", "全年", "咖啡因", "本地菜市场", LocalDate.now()),
                createIngredient("茶叶", new BigDecimal("200.00"), "kg", "粮油主食", "冲调,饮品", "全年", "茶多酚", "本地菜市场", LocalDate.now()),
                createIngredient("果汁", new BigDecimal("15.00"), "L", "粮油主食", "饮品,果汁", "全年", "维生素C", "本地菜市场", LocalDate.now()),
                createIngredient("可乐", new BigDecimal("8.00"), "L", "粮油主食", "饮品,碳酸", "全年", "糖分", "本地菜市场", LocalDate.now()),
                createIngredient("矿泉水", new BigDecimal("2.00"), "L", "粮油主食", "饮品,纯净", "全年", "矿物质", "本地菜市场", LocalDate.now()),
                createIngredient("纯净水", new BigDecimal("1.50"), "L", "粮油主食", "饮品,纯净", "全年", "H2O", "本地菜市场", LocalDate.now()),
                createIngredient("啤酒", new BigDecimal("5.00"), "L", "粮油主食", "饮品,酒精", "全年", "酒精", "本地菜市场", LocalDate.now()),
                createIngredient("白酒", new BigDecimal("50.00"), "L", "粮油主食", "饮品,酒精", "全年", "酒精", "本地菜市场", LocalDate.now()),
                createIngredient("红酒", new BigDecimal("80.00"), "L", "粮油主食", "饮品,酒精", "全年", "酒精", "本地菜市场", LocalDate.now()),
                createIngredient("黄酒", new BigDecimal("20.00"), "L", "粮油主食", "调味,酒精", "全年", "酒精", "本地菜市场", LocalDate.now())
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