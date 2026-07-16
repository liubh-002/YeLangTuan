# ============================================
# Build stage
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS build

# Install all tools via apk (no curl download)
RUN apk add --no-cache curl tar bash nodejs npm maven

# Build frontend
WORKDIR /app
COPY frontend/package.json ./frontend/
RUN cd frontend && npm install --legacy-peer-deps
COPY frontend/ ./frontend/
RUN cd frontend && npm run build && \
    mkdir -p backend/src/main/resources/static && \
    cp -r dist/* backend/src/main/resources/static/

# Build backend with Maven
COPY backend/ ./backend/
RUN mvn -f backend/pom.xml clean package -DskipTests

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
