# Архитектура онлайн-магазина (DDD & Microservices)

## 1. Ограниченные контексты (Bounded Contexts) и Агрегаты (Aggregates)

Всего спроектировано 14 агрегатов, распределенных по 5 независимым контекстам
(микросервисам).

### Контекст 1: Идентификация и доступ (`auth-service`)

Отвечает за регистрацию пользователей, управление ролями и выдачу JWT-токенов.

- Агрегат 1: `User` (Пользователь)
    - Сущности и Value Objects: `UserId`, `Email`, `FullName`, `UserStatus` (ACTIVE,
      BLOCKED).
    - Ответственность: Профиль пользователя и базовые контактные данные.
- Агрегат 2: `Credential` (Учетные данные)
    - Сущности и Value Objects: `CredentialId`, `PasswordHash`, `Salt`, `Set<Role>`
      (ROLE_CUSTOMER, ROLE_ADMIN).
    - Ответственность: Аутентификация, валидация пароля, управление правами.
    - Кеш Redis: Хранение черного списка токенов и Refresh-токенов.

### 2: Каталог товаров и Склад (`inventory-service`)

Отвечает за учет номенклатуры товаров и физическое наличие остатков.

- Агрегат 3: `Product` (Товар в каталоге)
    - Сущности и Value Objects: `ProductId`, `Title`, `Description`, `Money`
      (BasePrice), `Category`.
    - Ответственность: Карточка товара, цены, характеристики. Кешируется в
      Redis для быстрого чтения клиентами.
- Агрегат 4: `InventoryStock` (Складские остатки)
    - Сущности и Value Objects: `StockId`, `ProductId`, `Quantity` (общее
      количество), `ReservedQuantity`.
    - Ответственность: Контроль доступного количества товара на складе.
- Агрегат 5: `StockReservation` (Резерв товара)
    - Сущности и Value Objects: `ReservationId`, `OrderId`, `List<ReservedItem>`,
      `ReservationStatus` (PENDING, CONFIRMED, CANCELLED).
    - Ответственность: Временное бронирование товаров на время проведения
      оплаты в Saga.

### Контекст 3: Управление заказами (`order-service`) — Сервис-оркестратор

Ядро бизнес-логики. Отвечает за жизненный цикл заказа и координирует
распределенную транзакцию.

- Агрегат 6: `Order` (Заказ)
    - Сущности и Value Objects: `OrderId`, `CustomerId`, `OrderStatus` (NEW,
      PENDING_PAYMENT, PAID, CANCELLED, COMPLETED), `Money` (TotalAmount).
    - Ответственность: Корневой агрегат заказа, смена его статусов.
- Агрегат 7: `OrderItem` (Позиция заказа)
    - Сущности и Value Objects: `OrderItemId`, `ProductId`, `PriceAtPurchase`
      (зафиксированная цена на момент покупки), `Quantity`.
    - Ответственность: Снапшот купленных товаров (цена не должна меняться,
      если изменилась в каталоге).
- Агрегат 8: `OrderOutbox` (Transactional Outbox)
    - Сущности и Value Objects: `OutboxId`, `AggregateType`, `Payload` (JSON
      события), `Status` (PENDING, PROCESSED).
    - Ответственность: Гарантированная отправка событий в Kafka без потери
      данных в случае сбоя сети.
- Агрегат 9: `OrderSagaState` (Состояние распределенной транзакции)
    - Сущности и Value Objects: `SagaId`, `OrderId`, `CurrentStep`, `SagaStatus`
      (STARTED, COMPENSATING, FINISHED).
    - Ответственность: Хранение шагов саги для оркестрации.

### Контекст 4: Платежи и Биллинг (`payment-service`)

Отвечает за финансовые операции и балансы клиентов.

- Агрегат 10: `CustomerWallet` (Кошелек клиента)
    - Сущности и Value Objects: `WalletId`, `CustomerId`, `Money` (Balance),
      `Currency`.
    - Ответственность: Хранение денежного баланса пользователя.
- Агрегат 11: `PaymentTransaction` (Платежная транзакция)
    - Сущности и Value Objects: `TransactionId`, `OrderId`, `Amount`, `Type` (DEBIT,
      REFUND), `TransactionStatus` (SUCCESS, FAILED).
    - Ответственность: Фиксация попыток списания и возврата средств.
- Агрегат 12: `PaymentInbox` (Transactional Inbox)
    - Сущности и Value Objects: `MessageId`, `EventSource`, `ProcessedAt`.
    - Ответственность: Дедупликация входящих событий из Kafka (защита от
      двойного списания денег).

### Контекст 5: Доставка и Логистика (`delivery-service`)

Отвечает за доставку оплаченных заказов.

- Агрегат 13: `Shipment` (Отправление / Доставка)
    - Сущности и Value Objects: `ShipmentId`, `OrderId`, `DeliveryAddress`,
      `DeliveryStatus` (WAITING_PICKUP, IN_TRANSIT, DELIVERED).
    - Ответственность: Жизненный цикл доставки, трекинг.
- Агрегат 14: `CourierAssignment` (Назначение курьера)
    - Сущности и Value Objects: `CourierId`, `ShipmentId`, `AssignedAt`.
    - Ответственность: Назначение исполнителя на конкретную доставку.

## 2. Реализация технических требований ТЗ

1.  Синхронный gRPC вызов:
    - Перед созданием заказа `order-service` делает быстрый синхронный
      gRPC-запрос в `inventory-service` методом `CheckAvailability()`, чтобы
      убедиться, что товар вообще существует и не снят с продажи.
2.  Асинхронная шина (Kafka) + Transactional Outbox:
    - Когда создается заказ, запись о нем и запись в таблицу `OrderOutbox`
      сохраняются в Postgres в одной локальной ACID-транзакции.
    - Фоновый планировщик вычитывает Outbox и отправляет событие
      `OrderCreatedEvent` в Kafka.
3.  Распределенная транзакция (Saga Orchestration):
    - Шаг 1: `order-service` бронирует остатки на складе через событие в Kafka.
    - Шаг 2: Если склад ответил успехом (`StockReservedEvent`), отправляется
      команда на списание денег в `payment-service`.
    - Шаг 3: Если на шаге списания денег у пользователя не хватило баланса
      (`PaymentFailedEvent`), запускается компенсирующая транзакция:
      `order-service` отправляет команду на склад `CancelStockReservationCommand`,
      склад возвращает товары на баланс, а заказ получает статус `CANCELLED`.
4.  Кеширование (Redis):
    - В `inventory-service` для быстрого получения каталога товаров (`GET
      /api/v1/products/{id}`).
5.  Безопасность (JWT):
    - `auth-service` выдает Access JWT токен, который передается в заголовке
      `Authorization: Bearer <token>` во все сервисы.