# 1 этап: сборка приложения внутри контейнера
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Копируем файлы сборщика и исходный код всего проекта
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts .
COPY order-service order-service
COPY auth-service auth-service
COPY inventory-service inventory-service

RUN chmod +x gradlew && ./gradlew :order-service:bootJar -x test --no-daemon

# 2 этап: минимальный финальный образ только с JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/order-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]