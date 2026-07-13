package com.practice.mealplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiService {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.dashscope.api-key}")
    private String apiKey;

    public AiService(ObjectMapper objectMapper) {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = objectMapper;
    }

    public String generateMealPlan(int peopleCount, String taste, List<String> avoidIngredients,
                                   List<String> favoriteDishes, List<String> breakfastWant,
                                   List<String> lunchWant, List<String> dinnerWant,
                                   boolean savingMode, String weeklyBudget) {
        String prompt = "你是一个专业的膳食规划师。请根据以下条件生成一周的膳食菜单：\n\n" +
                "条件：\n" +
                "- 用餐人数：" + peopleCount + "人\n" +
                "- 口味偏好：" + taste + "\n" +
                "- 饮食忌口：" + (avoidIngredients != null ? String.join("、", avoidIngredients) : "无") + "\n" +
                "- 爱吃的菜：" + (favoriteDishes != null ? String.join("、", favoriteDishes) : "无") + "\n" +
                "- 早餐想吃：" + (breakfastWant != null ? String.join("、", breakfastWant) : "无") + "\n" +
                "- 午餐想吃：" + (lunchWant != null ? String.join("、", lunchWant) : "无") + "\n" +
                "- 晚餐想吃：" + (dinnerWant != null ? String.join("、", dinnerWant) : "无") + "\n" +
                "- 预算模式：" + (savingMode ? "平价" : "常规") + "\n" +
                "- 每周预算：" + weeklyBudget + "元\n\n" +
                "要求：\n" +
                "1. 生成周一到周日共7天的菜单\n" +
                "2. 每天包含早餐、午餐、晚餐\n" +
                "3. 早餐要使用早餐食材（牛奶、鸡蛋、粥、面条、包子等）\n" +
                "4. 午餐和晚餐要搭配合理，包含蛋白质和蔬菜\n" +
                "5. 预算模式为true时优先选择平价食材\n" +
                "6. 如果用户有指定想吃的食材，必须包含在对应的餐次中\n" +
                "7. 严格按照JSON格式输出，不要包含其他文字\n\n" +
                "JSON格式示例：\n" +
                "{\n" +
                "  \"days\": [\n" +
                "    {\n" +
                "      \"day\": \"周一\",\n" +
                "      \"meals\": [\n" +
                "        {\n" +
                "          \"mealType\": \"早餐\",\n" +
                "          \"dishName\": \"菜名\",\n" +
                "          \"ingredients\": [\"食材1\", \"食材2\"],\n" +
                "          \"cost\": 8.5,\n" +
                "          \"nutritionNote\": \"营养说明\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"mealType\": \"午餐\",\n" +
                "          \"dishName\": \"菜名\",\n" +
                "          \"ingredients\": [\"食材1\", \"食材2\", \"食材3\"],\n" +
                "          \"cost\": 15.0,\n" +
                "          \"nutritionNote\": \"营养说明\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"mealType\": \"晚餐\",\n" +
                "          \"dishName\": \"菜名\",\n" +
                "          \"ingredients\": [\"食材1\", \"食材2\"],\n" +
                "          \"cost\": 12.0,\n" +
                "          \"nutritionNote\": \"营养说明\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"dailyCost\": 35.5\n" +
                "    }\n" +
                "  ],\n" +
                "  \"weeklyCost\": 248.0\n" +
                "}\n\n" +
                "注意：\n" +
                "- 价格要根据菜市场实际单价计算（如：白菜3元/kg，肉25元/kg）\n" +
                "- 豆腐请使用\"白豆腐\"而不是\"北豆腐\"\n" +
                "- 食材用量根据人数调整\n" +
                "- 必须返回有效的JSON格式";

        return callAI(prompt);
    }

    public String generateFamilyMealPlan(int peopleCount, String taste, List<String> allTaboos,
                                         List<String> allTags, List<String> breakfastWant,
                                         List<String> lunchWant, List<String> dinnerWant,
                                         boolean savingMode, String weeklyBudget) {
        String prompt = "你是一个专业的家庭膳食规划师。请根据以下家庭条件生成一周的膳食菜单：\n\n" +
                "条件：\n" +
                "- 用餐人数：" + peopleCount + "人\n" +
                "- 口味偏好：" + taste + "\n" +
                "- 家庭人群标签：" + (allTags != null ? String.join("、", allTags) : "无") + "\n" +
                "- 饮食忌口：" + (allTaboos != null ? String.join("、", allTaboos) : "无") + "\n" +
                "- 早餐想吃：" + (breakfastWant != null ? String.join("、", breakfastWant) : "无") + "\n" +
                "- 午餐想吃：" + (lunchWant != null ? String.join("、", lunchWant) : "无") + "\n" +
                "- 晚餐想吃：" + (dinnerWant != null ? String.join("、", dinnerWant) : "无") + "\n" +
                "- 预算模式：" + (savingMode ? "平价" : "常规") + "\n" +
                "- 每周预算：" + weeklyBudget + "元\n\n" +
                "家庭人群饮食特点：\n" +
                "- 减肥：低热量、高蛋白、高膳食纤维\n" +
                "- 病号：清淡、易消化、营养均衡\n" +
                "- 青少年：高蛋白、高钙、富含维生素\n" +
                "- 老年人：软烂、低盐、易消化\n\n" +
                "要求：\n" +
                "1. 生成周一到周日共7天的菜单，每天轮换适配不同家庭成员的饮食需求\n" +
                "2. 每天包含早餐、午餐、晚餐\n" +
                "3. 早餐要使用早餐食材（牛奶、鸡蛋、粥、面条、包子等）\n" +
                "4. 午餐和晚餐要搭配合理，包含蛋白质和蔬菜\n" +
                "5. 必须避开所有家庭成员的饮食忌口\n" +
                "6. 预算模式为true时优先选择平价食材\n" +
                "7. 如果用户有指定想吃的食材，必须包含在对应的餐次中\n" +
                "8. 严格按照JSON格式输出，不要包含其他文字\n\n" +
                "JSON格式示例：\n" +
                "{\n" +
                "  \"days\": [\n" +
                "    {\n" +
                "      \"day\": \"周一\",\n" +
                "      \"adaptedTag\": \"青少年\",\n" +
                "      \"meals\": [\n" +
                "        {\n" +
                "          \"mealType\": \"早餐\",\n" +
                "          \"dishName\": \"菜名\",\n" +
                "          \"ingredients\": [\"食材1\", \"食材2\"],\n" +
                "          \"cost\": 8.5,\n" +
                "          \"nutritionNote\": \"营养说明\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"mealType\": \"午餐\",\n" +
                "          \"dishName\": \"菜名\",\n" +
                "          \"ingredients\": [\"食材1\", \"食材2\", \"食材3\"],\n" +
                "          \"cost\": 15.0,\n" +
                "          \"nutritionNote\": \"营养说明\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"mealType\": \"晚餐\",\n" +
                "          \"dishName\": \"菜名\",\n" +
                "          \"ingredients\": [\"食材1\", \"食材2\"],\n" +
                "          \"cost\": 12.0,\n" +
                "          \"nutritionNote\": \"营养说明\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"dailyCost\": 35.5\n" +
                "    }\n" +
                "  ],\n" +
                "  \"weeklyCost\": 248.0\n" +
                "}\n\n" +
                "注意：\n" +
                "- 价格要根据菜市场实际单价计算（如：白菜3元/kg，肉25元/kg）\n" +
                "- 豆腐请使用\"白豆腐\"而不是\"北豆腐\"\n" +
                "- 食材用量根据人数调整\n" +
                "- 必须返回有效的JSON格式";

        return callAI(prompt);
    }

    public String generateRecipes(String ingredient) {
        String prompt = "你是一个专业的厨师。请为食材\"" + ingredient + "\"生成5种不同的做法。\n\n" +
                "要求：\n" +
                "1. 每种做法要有独特的风味和烹饪方式\n" +
                "2. 包含详细的食材清单和烹饪步骤\n" +
                "3. 标注难度和适合人群\n" +
                "4. 严格按照JSON格式输出\n\n" +
                "JSON格式示例：\n" +
                "[\n" +
                "  {\n" +
                "    \"name\": \"菜名\",\n" +
                "    \"category\": \"分类\",\n" +
                "    \"tags\": \"标签\",\n" +
                "    \"ingredients\": [\"食材1\", \"食材2\"],\n" +
                "    \"steps\": [\"步骤1\", \"步骤2\", \"步骤3\"],\n" +
                "    \"difficulty\": \"简单/中等/困难\",\n" +
                "    \"suitableFor\": [\"人群1\", \"人群2\"]\n" +
                "  }\n" +
                "]\n\n" +
                "注意：\n" +
                "- 使用\"白豆腐\"而不是\"北豆腐\"\n" +
                "- 步骤要详细清晰\n" +
                "- 必须返回有效的JSON格式";

        return callAI(prompt);
    }

    public String generateDishInfo(String dishName, String mealType) {
        String prompt = "你是一个专业的营养师。请为菜品\"" + dishName + "\"（" + mealType + "）生成食材清单和营养说明。\n\n" +
                "要求：\n" +
                "1. 生成主要食材清单\n" +
                "2. 估算成本（根据菜市场单价）\n" +
                "3. 提供营养说明\n" +
                "4. 严格按照JSON格式输出\n\n" +
                "JSON格式：\n" +
                "{\n" +
                "  \"ingredients\": [\"食材1\", \"食材2\", \"食材3\"],\n" +
                "  \"cost\": 15.0,\n" +
                "  \"nutritionNote\": \"营养说明\"\n" +
                "}\n\n" +
                "注意：\n" +
                "- 使用\"白豆腐\"而不是\"北豆腐\"\n" +
                "- 必须返回有效的JSON格式";

        return callAI(prompt);
    }

    private String callAI(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            requestBody.put("model", "qwen-turbo");
            requestBody.put("temperature", 0.7);
            
            Map<String, Object> message = new HashMap<String, Object>();
            message.put("role", "user");
            message.put("content", prompt);
            List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();
            messages.add(message);
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("AI API request failed: {}", response.code());
                    return null;
                }

                String responseBody = response.body() != null ? response.body().string() : null;
                if (responseBody == null) {
                    log.warn("AI API returned empty response");
                    return null;
                }

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode content = choices.get(0).get("message").get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
                log.warn("AI API response format unexpected: {}", responseBody);
                return null;
            }
        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage(), e);
            return null;
        }
    }
}