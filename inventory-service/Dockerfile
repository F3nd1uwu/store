FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY contracts contracts
COPY build.gradle.kts settings.gradle.kts .
COPY order-service order-service
COPY auth-service auth-service
COPY inventory-service inventory-service
RUN chmod +x gradlew && ./gradlew :inventory-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/inventory-service/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
