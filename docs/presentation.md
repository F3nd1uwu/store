# План демонстрации преподавателю

## Подготовка

Перед показом выполните `gradlew.bat test bootJar`, затем Compose с local override и --wait. Откройте Kafka UI на localhost:8085. Для полного автоматического прогона запустите `python scripts/integration.py`. Для ручного показа используйте сценарии ниже; UUID и токены подставляйте из ответов API.

## 1. Объяснение архитектуры

Откройте architecture.md: три контекста и десять корней. Покажите settings.gradle.kts и domain/application build.gradle.kts одного сервиса. Объясните направление зависимости: контроллер → use case → порт; adapter реализует порт. Отдельно покажите, что OrderItem и outbox не входят в число бизнес-агрегатов.

## 2. Регистрация и JWT

POST localhost:8081/api/v1/auth/register с email/password. Скопируйте accessToken в Bearer-заголовок. Повторите GET товаров без токена и получите 401. Основной код: AuthController, AuthUseCase.register, JwtService.issue и JwtSecurityConfig другого сервиса. Пароль в users хранится как BCrypt hash, токен содержит UUID в sub.

## 3. Каталог и цена по gRPC

POST localhost:8083/api/v1/products: title=Book, price=10, currency=RUB, quantity=5. Запомните id. Повторите GET. Покажите ProductController → CatalogService → PostgresProducts и RedisConfig. Для gRPC откройте CatalogGrpcClient.quote и CatalogGrpcServer.start, затем catalog.proto.

## 4. Успешная покупка

Пополните POST localhost:8083/api/v1/wallet/deposit: currency=RUB, amount=100. Создайте POST localhost:8082/api/v1/orders с двумя единицами товара. Можно передать price=0.01: итог всё равно равен 20, поскольку use case использует gRPC-цену 10. POST возвращает NEW, повторный GET через несколько секунд — PAID. Остаток товара 3, баланс кошелька 80.

Покажите CreateOrderUseCase.execute: запись заказа и события в одной @Transactional операции. Затем OutboxPublisherScheduler и PostgreSQL outbox_events. В Kafka UI видны ORDER_CREATED, STOCK_RESERVED, PAYMENT_SUCCEEDED и ORDER_CONFIRMED.

## 5. Компенсация

Зарегистрируйте второго пользователя, кошелёк не пополняйте. Создайте заказ на одну единицу того же товара. Он станет CANCELLED, а остаток останется 3 после временного резервирования и возврата. В событиях видны STOCK_RESERVED → PAYMENT_FAILED → ORDER_REJECTED. Основной код: InventorySaga.handle и PostgresSagaStore.finish(false). Объясните, что компенсация — отдельная транзакция после уже закоммиченного резерва.

## 6. Повторная доставка и конкуренция

Запустите scripts/integration.py. Скрипт извлекает исходный event payload из order_db и повторно публикует его в Kafka: баланс и остатки не меняются второй раз. Затем два пользователя одновременно оформляют последнюю единицу товара: только один заказ становится PAID. Покажите PK inbox_events.event_id, ON CONFLICT DO NOTHING, SELECT FOR UPDATE и условный UPDATE wallets.

## 7. Данные и технические проверки

```sh
docker compose exec postgres psql -U postgres -d order_db -c "SELECT id,status,total_amount FROM orders"
docker compose exec postgres psql -U postgres -d order_db -c "SELECT status,count(*) FROM outbox_events GROUP BY status"
docker compose exec postgres psql -U postgres -d inventory_db -c "SELECT order_id,state FROM reservations"
docker compose exec postgres psql -U postgres -d inventory_db -c "SELECT order_id,amount,state FROM payments"
docker compose exec redis redis-cli --scan --pattern 'products::*'
```

Значение TTL можно показать через redis-cli TTL с конкретным найденным ключом. PostgreSQL хранит истину, Redis ускоряет чтение. Для просмотра методов и строк исходников используйте code-map.md.

## 8. CI/CD

Откройте .github/workflows/ci.yml. Объясните цепочку unit-tests → assemble → integration-tests → package-images и отдельный ручной workflow .github/workflows/deploy.yml. В Actions доступны отчёты тестов и архив ручного деплоя; Docker-образы публикуются в GHCR после успешного push в main. Покажите production Compose, где образы имеют immutable tag commit SHA, а секреты поступают из .env сервера. Если внешний сервер ещё не настроен, обозначьте этот факт: конфигурация deployment подготовлена, фактический запуск требует окружения.

## Вопросы и ответы

**Почему не одна @Transactional на всё?** У сервисов разные базы и соединения; локальная транзакция не охватывает Kafka и чужую БД.

**Зачем outbox?** Чтобы изменение данных не закоммитилось без сохранённого намерения отправить событие.

**Зачем inbox, если Kafka producer идемпотентен?** Publisher может повторить уже подтверждённое сообщение после падения до фиксации SENT в БД; протокольная идемпотентность producer не охватывает этот случай.

**Есть ли exactly once?** Доставка at least once, бизнес-эффекты защищены inbox и уникальными ключами.

**Что если каталог упадёт?** gRPC завершится по deadline, новый заказ не сохранится. Если Kafka недоступна после сохранения, событие останется в outbox.

**Почему не отрицательный остаток?** Все нужные строки блокируются, сначала проверяются все позиции, затем списываются; БД дополнительно содержит CHECK quantity>=0.

**Почему не вернуть товар два раза?** Компенсация обновляет только ACTIVE резерв, и количество возвращается только при одном успешном переходе в RELEASED.

**Какие упрощения?** Учебное пополнение кошелька без банка; один broker; нет таймаута зависшей саги и автоматической очистки сообщений; categories/coupons демонстрируют самостоятельные операции и не включены автоматически в checkout.
