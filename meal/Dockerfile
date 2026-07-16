# ============================================
# Build stage
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS build

# Install Maven
RUN apk add --no-cache curl tar bash && \
    curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -o /tmp/maven.tar.gz && \
    tar xzf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.9/bin/mvn /usr/local/bin/mvn && \
    rm /tmp/maven.tar.gz

# Install Node.js for frontend
RUN apk add --no-cache nodejs npm

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
