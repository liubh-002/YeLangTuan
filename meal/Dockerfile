FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Install Node.js for building frontend
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g npm@latest

# Build frontend
COPY frontend/package.json ./frontend/
RUN cd frontend && npm install --legacy-peer-deps
COPY frontend/ ./frontend/
RUN mkdir -p ./backend/src/main/resources/static && \
    cd frontend && npm run build && \
    cp -r dist/* ../backend/src/main/resources/static/

# Build backend
COPY backend/ ./backend/
RUN mvn -f backend/pom.xml clean package -DskipTests -T 2

# Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
USER appuser
CMD ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]
