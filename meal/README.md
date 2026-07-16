# AI 智能膳食规划系统

这是一个前后端分离的完整示例项目，后端使用 Java Spring Boot，前端使用 React + Vite。

核心能力：

- 定时同步新发地公开行情数据，清洗食材、价格、时令、标签后入库
- 用户输入人数、忌口、口味、每周预算后生成一周早中晚菜谱
- 登录页维护用户姓名、性别、年龄、手机号等个人信息
- 支持输入自己爱吃的菜，生成菜谱时穿插偏好菜
- 支持输入月工资，自动计算月食材花销占工资比例
- 前端展示工资/食材花销柱形图和生活占比饼图
- 自动计算每日和每周买菜成本
- 预算达到 80% 轻度预警，超预算重度预警
- 超预算时自动替换为更平价且口味相近的菜品
- 无外部 AI Key 时使用本地规则规划器；配置 OpenAI Key 后可扩展为真实大模型规划

## 项目结构

```text
backend/   Java Spring Boot 后端
frontend/  React 前端
```

## 后端运行

需要 JDK 8+ 和 Maven。

```bash
cd backend
mvn spring-boot:run
```[resources](backend%2Fsrc%2Fmain%2Fresources)

默认服务地址：`http://localhost:8080`

H2 控制台：`http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:file:./data/meal_planner`
- User: `sa`
- Password: 空

## 前端运行

```bash
cd frontend
npm install
npm run dev
```

默认前端地址：`http://localhost:5173`

## 常用接口

同步食材行情：

```bash
curl -X POST http://localhost:8080/api/ingredients/sync
```

查询食材：

```bash
curl http://localhost:8080/api/ingredients
```

生成菜谱：

```bash
curl -X POST http://localhost:8080/api/plans/generate \
  -H "Content-Type: application/json" \
  -d '{"peopleCount":3,"avoidIngredients":["香菜"],"taste":"清淡","weeklyBudget":500}'
```

## 新发地数据说明

后端会优先尝试请求新发地公开行情接口。不同时间接口可能调整或限制访问，项目内置了兜底样例数据，保证开发环境首次运行即可生成菜谱。
