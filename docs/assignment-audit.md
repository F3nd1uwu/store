# Сверка реализации с заданием

## 1. Предметная область и 10–15 агрегатов

Онлайн-магазин. Реализованы 10 корней: User, CustomerProfile, Order, Cart, Coupon, Product, Category, Reservation, Wallet, Payment. OrderItem — внутренняя сущность, Money — value object; технические записи не считаются агрегатами. Подробности — architecture.md.

## 2. DDD и ограниченные контексты

Identity: User/CustomerProfile. Sales: Order/Cart/Coupon. Inventory с внутренним биллингом: Product/Category/Reservation/Wallet/Payment. Каждый контекст владеет своей базой и API; между контекстами используются UUID, gRPC и события.

## 3. Микросервисы, REST и чистая архитектура

Три самостоятельно запускаемых Spring Boot приложения. Для каждого есть отдельные Gradle-модули domain и application, а исполняемый модуль содержит REST/persistence/messaging/security adapters. Domain не зависит от Spring; application зависит от портов. contracts содержит общий межсервисный протокол. Карта основных операций находится в code-map.md.

## 4. Docker и Compose

Dockerfile каждого приложения поддерживает multistage-сборку. Dockerfile.runtime и docker-compose.local.yml позволяют упаковать готовые JAR. Основной Compose запускает приложения, PostgreSQL, Redis, Kafka и Kafka UI. Healthchecks и depends_on service_healthy задают ожидание инфраструктуры, restart policy обеспечивает перезапуск контейнеров.

## 5. Redis

Кеш заказов orders и товаров products с TTL 10 минут. При изменении результатов саги кеши инвалидируются. Null не кешируется. PostgreSQL — источник истины. Основные операции: GetOrderUseCase.execute и CatalogService.get/create.

## 6. PostgreSQL

Базы auth_db/order_db/inventory_db. Auth и Order используют JPA плюс JDBC для вспомогательных записей; Inventory — JDBC. Схемы создаются Hibernate update и schema.sql. Скрипт первого запуска создаёт отдельные базы. Версионируемые миграции не являются частью текущего учебного стенда.

## 7. Распределённая транзакция

Хореографическая сага: заказ → резерв → оплата → подтверждение. При отказе оплаты отдельная компенсирующая транзакция освобождает резерв. Реализация InventorySaga, PostgresSagaStore, ApplySagaResult. Подробные границы транзакций — saga.md.

## 8. Асинхронное взаимодействие с outbox/inbox

Order и Inventory атомарно сохраняют исходящие события вместе с бизнес-данными. Publishers ждут Kafka ack. Consumers сохраняют eventId в inbox с бизнес-операцией; повтор сообщения не приводит к повторному списанию.

## 9. Шина данных

Kafka в KRaft-режиме. Топики store.order.v1 и store.inventory.v1. KafkaTopics создаёт топики; SagaMessaging и OrderSagaListener обрабатывают сообщения. Kafka UI доступен на 8085.

## 10. gRPC

Order вызывает Catalog Quote в Inventory для получения цены. Контракт catalog.proto и CatalogRpc; adapters CatalogGrpcClient/CatalogGrpcServer. Вызов unary с protobuf StringValue и JSON товара внутри ответа, deadline 3 секунды. Внутренний порт 9093.

## 11. JWT

Auth выдаёт JWT HS256 после регистрации/входа. Все три сервиса проверяют подпись и exp; userId берётся из sub. Доступ к заказам и персональным документам ограничен владельцем. /health и auth endpoints публичны.

## 12. Тесты

JUnit/Mockito: сценарии регистрации, заказа, доменных инвариантов и шагов саги. scripts/integration.py выполняет несколько сценариев на настоящих сервисах с PostgreSQL, Redis, gRPC и Kafka, включая компенсацию, повтор сообщения и конкурентные покупки. Результаты конкретного запуска — verification.md.

## 13. GitHub Actions

.github/workflows/ci.yml: unit-tests → assemble → integration-tests → package-images. Workflow запускается на push, pull_request и вручную. JUnit XML/HTML и логи Compose сохраняются в artifacts. Интеграционные тесты используют Docker Compose на Ubuntu runner без Docker-in-Docker. Сборка также создаёт архив ручного деплоя.

## 14. Сборка и деплой на сервер

После успешных тестов основной ветки package-images публикует образы с тегом полного commit SHA в ghcr.io/f3nd1uwu/store/<service>. Отдельный .github/workflows/deploy.yml запускается только вручную с release_sha и использует environment production. Он копирует серверную конфигурацию и выполняет pull/up по SSH. Секреты БД/JWT остаются в .env сервера.

Ручной деплой без Actions также поддерживается: scripts/package-release.py создаёт архив, scripts/deploy-manual.sh строит образы на сервере из JAR. Выбранный каталог /home/yasakovd и домен описаны в deployment.md. CI на GitHub успешно проверен: [unit-tests, assemble и integration-tests](https://github.com/F3nd1uwu/store/actions/runs/44056814481). Внешний деплой выполнен 25 ноября 2026 года вручную из собранных JAR; HTTPS и интеграционные сценарии проверены на сервере. [Результат](deployment-result.md). Публикация образов основной ветки и workflow Deploy production остаются отдельным способом обновления.
