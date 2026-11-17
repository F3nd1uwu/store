# Онлайн-магазин на микросервисной архитектуре

Учебное приложение на Java 21 и Spring Boot. Три ограниченных контекста: идентификация покупателя, продажи, каталог со складом и учебным биллингом. Используются PostgreSQL, Redis, Kafka, gRPC, JWT, transactional outbox/inbox и сага с хореографией.

## Документация

- [Архитектура и DDD](docs/architecture.md): 10 агрегатов, границы контекстов, зависимости модулей.
- [Auth service](docs/auth-service.md): регистрация, BCrypt, JWT, профиль.
- [Order service](docs/order-service.md): заказ, корзина, купон, gRPC, outbox, обработка результата саги.
- [Inventory service](docs/inventory-service.md): каталог, резерв, кошелёк, платёж и компенсация.
- [Сага и доставка событий](docs/saga.md): транзакции, события, повторная доставка, отказ оплаты.
- [Соответствие заданию](docs/assignment-audit.md): все 14 требований.
- [Сценарий демонстрации](docs/presentation.md): последовательность показа и вопросы к защите.
- [Сборка, тесты и эксплуатация](docs/verification.md).
- [HTTP gateway](docs/gateway.md): единый адрес API и подключение к Nginx Proxy Manager.
- [GitHub Actions CI/CD и сервер](docs/deployment.md).
- [Навигация по операциям в коде](docs/code-map.md).

## Быстрый запуск

Требуются JDK 21 и запущенный Docker Engine с Compose v2.

```powershell
.\gradlew.bat test bootJar
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --build --wait
python scripts/integration.py
```

Linux: `./gradlew test bootJar`. Другой способ сборки из исходников внутри Docker: `docker compose up -d --build --wait`. Он требует сетевого доступа контейнера к Gradle distribution и Maven Central. Вариант с local override упаковывает JAR, заранее собранные на хосте.

HTTP: auth 8081, order 8082, inventory 8083. Kafka UI: http://localhost:8085. PostgreSQL с хоста: localhost:5433, Redis: localhost:6379, Kafka: localhost:9092. gRPC каталога: inventory-service:9093 внутри сети Compose; порт наружу не опубликован. `/health` доступен без JWT.

Регистрация: `POST /api/v1/auth/register`, тело `{"email":"student@example.com","password":"password123"}`. Полученный `accessToken` передаётся в заголовке `Authorization: Bearer TOKEN`. Зарегистрированный пользователь может наполнить учебный кошелёк, создать товар и оформить заказ. Платёж здесь — списание внутреннего баланса; внешнего банка нет.

## Сохранность данных

У каждого сервиса отдельная база: auth_db, order_db, inventory_db. Сервер PostgreSQL и Redis общие для локального стенда. Том postgres_data сохраняет бизнес-данные, redis_data — кеш. `docker compose down` останавливает стенд без удаления томов. Скрипт init-multiple-dbs.sh создаёт базы только при первоначальном запуске пустого PostgreSQL.

Сага выполняется асинхронно: POST заказа возвращает NEW, итоговый GET — PAID или CANCELLED. Цена клиента не используется: стоимость запрашивается из каталога по gRPC. Для оплаты пополните `/api/v1/wallet/deposit`. При недостатке денег резерв автоматически возвращается.

Для сервера подготовлены production Compose и pipeline. Фактическое развёртывание требует адреса сервера, SSH-доступа и настройки переменных и секретов GitHub; процедура приведена в deployment.md.
