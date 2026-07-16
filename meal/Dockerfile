# Stage 1: Build Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ .
RUN npm run build

# Stage 2: Build Backend (maven image includes mvn)
FROM maven:3.9-eclipse-temurin-17 AS backend-builder
WORKDIR /app
COPY --from=frontend-builder /app/frontend/dist ./backend/src/main/resources/static/
COPY backend/ ./backend/
RUN mvn -f backend/pom.xml clean package -DskipTests

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-builder /app/backend/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-Xmx256m", "-jar", "app.jar"]
