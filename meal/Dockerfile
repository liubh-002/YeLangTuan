# ============================================
# Build stage
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS build

# Install all tools via apk
RUN apk add --no-cache curl tar bash nodejs npm maven

# Build frontend
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ .
RUN npm run build

# Copy frontend build to backend static resources
RUN mkdir -p /app/backend/src/main/resources/static && \
    cp -r /app/frontend/dist/* /app/backend/src/main/resources/static/

# Build backend with Maven
WORKDIR /app
COPY backend/ ./backend/
RUN mvn -f /app/backend/pom.xml clean package -DskipTests

# ============================================
# Runtime stage
# ============================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
USER appuser
CMD ["java", "-Xmx256m", "-jar", "app.jar"]
