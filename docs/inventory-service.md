# Inventory service: устройство и основные операции

## Назначение

HTTP 8083, gRPC 9093 внутри сети Docker, PostgreSQL inventory_db. Корни: Product, Category, Reservation, Wallet, Payment. Доступное количество хранится в Product.quantity; отдельного агрегата Stock в этой модели нет. Биллинг представляет внутренний учебный кошелёк.

## Каталог и кеш

ProductController.create валидирует title до 200 символов, цену >=0, валюту из трёх заглавных букв и quantity>=0. CatalogService.create сохраняет Product через ProductRepository, реализация PostgresProducts делает параметризованный INSERT. UUID назначается сервером. CachePut записывает созданный товар в кеш products.

ProductController.get → CatalogService.get → Redis, при промахе PostgresProducts.find выполняет SELECT по id. TTL 10 минут, null не кешируется. InventorySaga.handle очищает кеш products после изменения доступного остатка. RedisConfig явно создаёт CacheManager и учитывает транзакции.

CatalogGrpcServer.start регистрирует метод store.Catalog/Quote. Вход — StringValue с UUID, выход — StringValue с JSON товара. Контракт описан в contracts/src/main/proto/catalog.proto, MethodDescriptor — в CatalogRpc. Это настоящий unary gRPC с protobuf-маршаллингом; вложенный payload товара представлен JSON. Для отсутствующего товара возвращается NOT_FOUND. gRPC обращается к ProductRepository напрямую и читает актуальные данные PostgreSQL. Сервер закрывается через @PreDestroy.

## Резервирование: критическая операция

SagaMessaging.receive читает событие Kafka и вызывает InventorySaga.handle. На application-методе стоит @Transactional. Первым вызывается SagaStore.begin: INSERT в inbox_events по eventId. Уже обработанное сообщение не выполняет бизнес-операцию повторно.

При ORDER_CREATED вызывается PostgresSagaStore.reserve:

1. Проверяет существование резерва по orderId.
2. Объединяет одинаковые productId, суммируя quantity.
3. Сортирует UUID и захватывает строки products через SELECT FOR UPDATE в одинаковом порядке, что уменьшает вероятность deadlock.
4. Проверяет наличие товара, валюту и достаточность всех остатков до первого списания.
5. Только если все проверки прошли, уменьшает products.quantity и создаёт reservations со state=ACTIVE и JSON позиций.
6. InventorySaga сохраняет STOCK_RESERVED или ORDER_REJECTED в inventory_outbox.

Inbox, изменение количества, резерв и outbox коммитятся одной транзакцией. При нехватке любого товара не списывается ни одна позиция. Блокировки не позволяют двум конкурентным заказам продать одну последнюю единицу.

## Оплата

STOCK_RESERVED обрабатывается отдельным Kafka-вызовом и отдельной локальной транзакцией. PostgresSagaStore.charge проверяет уникальность платежа по orderId. Затем атомарно выполняет UPDATE wallets SET balance=balance-amount WHERE balance>=amount AND currency=... . Если строка не изменилась, отправляется PAYMENT_FAILED. Иначе создаётся payments со state=PAID и outbox PAYMENT_SUCCEEDED.

Запись платежа и списание баланса атомарны. Ограничение CHECK(balance>=0) дополнительно защищает данные. Уникальный order_id в payments и inbox eventId предотвращают повторное списание в штатной повторной доставке.

## Компенсация

PAYMENT_FAILED вызывает PostgresSagaStore.finish(false). UPDATE меняет только ACTIVE → RELEASED. Если обновлена ровно одна строка, количества из события возвращаются в products. Повторная компенсация не увеличивает остаток второй раз. После этого записывается ORDER_REJECTED.

PAYMENT_SUCCEEDED меняет ACTIVE → CONFIRMED без возврата товара и создаёт ORDER_CONFIRMED. Reservation.finish в домене описывает разрешённый переход из ACTIVE в финальное состояние. Все эти изменения и итоговый outbox выполняются одной транзакцией. Сбой процесса приводит к повторной доставке, а не к потере решения.

## Кошелёк и категория

AccountsController.deposit принимает currency/amount и получает владельца из JWT. Wallet проверяет валюту и неотрицательную сумму. PostgresWallets.deposit выполняет INSERT ... ON CONFLICT DO UPDATE с проверкой совпадения валюты. Это пополнение демонстрационного внутреннего баланса, без реального платёжного провайдера. GET /wallet читает собственный кошелёк.

CategoryService сохраняет Category через DocumentStore с kind=category и owner_id из JWT. Домен требует UUID и непустое название. GET доступен владельцу категории. Категория — самостоятельный справочный объект, обязательной связи с Product в данном учебном API нет.

## PostgreSQL

- products: id PK, title VARCHAR(200), price NUMERIC(19,2) CHECK>=0, currency VARCHAR(3), quantity INTEGER CHECK>=0; обязательные поля NOT NULL.
- reservations: order_id PK, customer_id, state, items TEXT.
- wallets: customer_id PK, currency, balance NUMERIC(19,2) CHECK>=0.
- payments: order_id PK, customer_id, amount CHECK>=0, currency, state.
- inbox_events: event_id PK, received_at DEFAULT now().
- inventory_outbox: event_id PK, order_id, payload TEXT, sent BOOLEAN DEFAULT false, created_at.
- documents: PK(kind,id), owner_id, payload; здесь kind=category.

Схема создаётся идемпотентным schema.sql. Данные живут в volume PostgreSQL. JDBC запросы параметризованы.

## Публикация Kafka

SagaMessaging.publish раз в секунду выбирает до 20 неотправленных событий через FOR UPDATE SKIP LOCKED. Ждёт Kafka ack до 10 секунд; после подтверждения выставляет sent=true. При исключении вся локальная транзакция откатывается, события остаются для повторной отправки. Топик store.inventory.v1, key=orderId. DefaultErrorHandler повторяет неуспешную обработку без молчаливого пропуска.

## API

POST /api/v1/products, GET /api/v1/products/{id}; POST /api/v1/wallet/deposit, GET /api/v1/wallet; POST /api/v1/categories, GET /api/v1/categories/{id}. Для всех нужен JWT. GET /health публичный. gRPC порт наружу не опубликован.

ProductTest проверяет отрицательные цену и остаток. InventorySagaTest проверяет дедупликацию, отказ резерва, компенсацию и подтверждение оплаты. Интеграционные проверки включают реальную Kafka, PostgreSQL, Redis, gRPC, конкуренцию за последний товар и повтор исходного сообщения.

## Конфигурация транспорта Kafka

`infrastructure/KafkaConfig` явно создаёт KafkaAdmin, String-serializer producer, KafkaTemplate и ConcurrentKafkaListenerContainerFactory. @EnableKafka включает обработку @KafkaListener. Consumer group отдельная для контекста, auto.offset.reset=earliest, enable.auto.commit=false, ack mode RECORD: offset фиксируется после возврата обработчика. Producer использует acks=all и enable.idempotence=true. KafkaReliability передаёт контейнеру DefaultErrorHandler с повтором через 2 секунды. Поэтому транспортная конфигурация не зависит от неявного наличия Boot autoconfiguration. KafkaTopics объявляет два versioned-топика.
