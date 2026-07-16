# ============================================
# Stage 1: Build Frontend
# ============================================
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ .
RUN npm run build

# ============================================
# Stage 2: Build Backend
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder
WORKDIR /app
COPY --from=frontend-builder /app/frontend/dist ./backend/src/main/resources/static/
COPY backend/ ./backend/
RUN mvn -f backend/pom.xml clean package -DskipTests -T 2

# ============================================
# Stage 3: Runtime
# ============================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=backend-builder /app/backend/target/*.jar app.jar
EXPOSE 8080
USER appuser
CMD ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]
