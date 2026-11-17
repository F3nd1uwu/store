# Order service: устройство и основные операции

## Назначение и агрегаты

HTTP 8082, PostgreSQL order_db. Корни Order, Cart и Coupon. OrderItem принадлежит Order, Money проверяет неотрицательную сумму и валюту. Статусы модели: NEW, PENDING_PAYMENT, PAID, CANCELLED, COMPLETED. Демонстрируемый поток использует NEW → PAID/CANCELLED.

## Создание заказа

`presentation/controller/OrderController.createOrder` извлекает customerId из Jwt.sub. Поле customerId клиента не влияет на владельца. Валидируются непустые items, productId и положительное quantity.

`application/usecase/CreateOrderUseCase.execute`:

1. Для каждой позиции вызывает CatalogPort.quote(productId).
2. `infrastructure/grpc/CatalogGrpcClient.quote` выполняет unary gRPC store.Catalog/Quote с deadline 3 секунды на inventory-service:9093. Ответ включает актуальную цену и валюту.
3. При несовпадении валют use case отказывается сохранять заказ. Цена из HTTP-запроса игнорируется. Остаток из Quote носит справочный характер: окончательная проверка выполняется с блокировкой при резервировании.
4. Создаёт OrderItem и Order, рассчитывает totalAmount как сумму quantity × unitPrice.
5. OrderRepository.save через PostgresOrderRepository сохраняет orders и order_items.
6. Через OrderEvents.append сохраняет StoreEvent ORDER_CREATED в outbox_events. В событии есть eventId, orderId, customerId, amount, currency, items.

@Тransactional на execute объединяет заказ и outbox в одну локальную транзакцию. Сетевой вызов gRPC выполняется до записи заказа; при недоступности каталога заказ не создаётся.

## Публикация и получение результата

`OutboxPublisherScheduler.publishPendingEvents` каждые 2 секунды выбирает до 10 PENDING записей. SpringDataOutboxRepository использует PESSIMISTIC_WRITE; публикация проходит внутри транзакции. Kafka key — UUID заказа, topic — store.order.v1. Статус SENT выставляется после подтверждения Kafka, ожидание ограничено 10 секундами. При ошибке запись остаётся PENDING.

`OrderSagaListener.receive` получает store.inventory.v1 и передаёт событие в `ApplySagaResult.execute`. `SagaResultAdapter.apply` записывает eventId в order_inbox через ON CONFLICT DO NOTHING. Повторная доставка заканчивается без повторного изменения данных. Для ORDER_CONFIRMED заказ NEW меняется на PAID, для ORDER_REJECTED — на CANCELLED; итог и reason сохраняются в order_sagas. Inbox и изменение заказа коммитятся вместе. Обработчик инвалидирует кеш orders.

## Чтение и Redis

OrderController.getOrderById → GetOrderUseCase.execute → кеш orders по UUID → при промахе OrderRepository.findById. PostgresOrderRepository.toDomain использует Order.restore: статус и createdAt восстанавливаются из БД, не вычисляются заново. Контроллер сравнивает customerId с sub; отсутствующий и чужой заказ возвращают 404.

RedisConfig задаёт TTL 10 минут и запрещает кешировать null. Менеджер кеша учитывает транзакции. PostgreSQL остаётся источником истины. Изменение результата саги очищает кеш заказов; чистка всего пространства выбрана для простоты небольшого стенда.

## PostgreSQL

- orders: id PK, customer_id, status, total_amount, currency, created_at.
- order_items: id PK, order_id FK, product_id, quantity, unit_price, currency. Cascade ALL и orphanRemoval задают владение позициями.
- outbox_events: id PK (eventId), aggregate_type, aggregate_id, event_type, payload TEXT, status, created_at.
- order_inbox: event_id PK, received_at; техническая дедупликация.
- order_sagas: order_id PK, state, reason, updated_at; финальное состояние процесса.
- documents: PK(kind,id), owner_id, payload. kind=cart либо coupon.

Hibernate управляет JPA-таблицами; schema.sql создаёт вспомогательные JDBC-таблицы. Межбазовых FK нет.

## Корзина и купон

ShoppingController → ShoppingService → DocumentStore → Documents. PUT /cart заменяет список позиций собственной корзины; GET читает её. Cart.Line запрещает quantity<=0 и null productId. ID корзины равен UUID пользователя.

POST /coupons сохраняет Coupon пользователя с процентом от 1 до 50. GET /coupons/{id}/quote?amount=100 рассчитывает сумму после скидки с округлением HALF_UP до двух знаков. Чужой купон не читается. Это отдельная демонстрация агрегата правил скидки; оформление заказа использует полную каталожную цену, автоматического применения купона нет.

## API

- POST /api/v1/orders: `{"currency":"RUB","items":[{"productId":"UUID","quantity":2}]}` → 201 с orderId и NEW.
- GET /api/v1/orders/{id}: собственный заказ, итоговый статус PAID/CANCELLED появляется после обработки Kafka.
- PUT /api/v1/cart: массив productId/quantity; GET /api/v1/cart.
- POST /api/v1/coupons: id/code/percent.
- GET /api/v1/coupons/{id}/quote?amount=100.

Все операции требуют Bearer JWT, кроме GET /health. ApiErrors оформляет ошибки домена как 400 и конфликт БД как 409. Сетевой отказ gRPC пока возвращается как серверная ошибка.

## Проверки

OrderTest проверяет восстановление статуса, времени и суммы и запрет отмены COMPLETED. CreateOrderTest проверяет цену из каталога, содержание события и отсутствие сохранения при несовпадении валют. Интеграционные сценарии проверяют реальные gRPC, outbox, статус заказа, доступ владельца, оплату и компенсацию.

## Конфигурация транспорта Kafka

`infrastructure/KafkaConfig` явно создаёт KafkaAdmin, String-serializer producer, KafkaTemplate и ConcurrentKafkaListenerContainerFactory. @EnableKafka включает обработку @KafkaListener. Consumer group отдельная для контекста, auto.offset.reset=earliest, enable.auto.commit=false, ack mode RECORD: offset фиксируется после возврата обработчика. Producer использует acks=all и enable.idempotence=true. KafkaReliability передаёт контейнеру DefaultErrorHandler с повтором через 2 секунды. Поэтому транспортная конфигурация не зависит от неявного наличия Boot autoconfiguration. KafkaTopics объявляет два versioned-топика.
