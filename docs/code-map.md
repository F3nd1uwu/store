# Основные операции в исходном коде

Номера строк соответствуют текущим исходникам. Для обновления карты: `python scripts/generate-code-map.py`.

## auth-service

### AuthUseCase

Файл: [auth-service/src/main/java/ru/web/store/auth/application/AuthUseCase.java](../auth-service/src/main/java/ru/web/store/auth/application/AuthUseCase.java).

- Строка 16: `public AuthUseCase(UserRepository users, PasswordEncoder encoder, TokenIssuer jwt)`.
- Строка 23: `public String register(String email, String password)`.
- Строка 34: `public String login(String email, String password)`.

### DocumentStore

Файл: [auth-service/src/main/java/ru/web/store/auth/application/DocumentStore.java](../auth-service/src/main/java/ru/web/store/auth/application/DocumentStore.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### ProfileService

Файл: [auth-service/src/main/java/ru/web/store/auth/application/ProfileService.java](../auth-service/src/main/java/ru/web/store/auth/application/ProfileService.java).

- Строка 12: `public ProfileService(DocumentStore store)`.
- Строка 16: `public CustomerProfile save(CustomerProfile profile)`.
- Строка 21: `public Optional<CustomerProfile> get(UUID id)`.

### TokenIssuer

Файл: [auth-service/src/main/java/ru/web/store/auth/application/TokenIssuer.java](../auth-service/src/main/java/ru/web/store/auth/application/TokenIssuer.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### AuthServiceApplication

Файл: [auth-service/src/main/java/ru/web/store/auth/AuthServiceApplication.java](../auth-service/src/main/java/ru/web/store/auth/AuthServiceApplication.java).

- Строка 8: `public static void main(String[] args)`.

### CustomerProfile

Файл: [auth-service/src/main/java/ru/web/store/auth/domain/CustomerProfile.java](../auth-service/src/main/java/ru/web/store/auth/domain/CustomerProfile.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### User

Файл: [auth-service/src/main/java/ru/web/store/auth/domain/User.java](../auth-service/src/main/java/ru/web/store/auth/domain/User.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### UserRepository

Файл: [auth-service/src/main/java/ru/web/store/auth/domain/UserRepository.java](../auth-service/src/main/java/ru/web/store/auth/domain/UserRepository.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Documents

Файл: [auth-service/src/main/java/ru/web/store/auth/infrastructure/Documents.java](../auth-service/src/main/java/ru/web/store/auth/infrastructure/Documents.java).

- Строка 12: `public Documents(JdbcTemplate jdbc)`.
- Строка 16: `public void save(String kind, UUID id, UUID owner, String payload)`.
- Строка 29: `public Optional<String> find(String kind, UUID id, UUID owner)`.

### UserEntity

Файл: [auth-service/src/main/java/ru/web/store/auth/infrastructure/persistence/UserEntity.java](../auth-service/src/main/java/ru/web/store/auth/infrastructure/persistence/UserEntity.java).

- Строка 25: `public UserEntity(UUID id, String email, String passwordHash, String role, boolean active)`.
- Строка 33: `public UUID getId()`.
- Строка 37: `public String getRole()`.
- Строка 41: `public String getPasswordHash()`.
- Строка 45: `public boolean isActive()`.

### UserJpaRepository

Файл: [auth-service/src/main/java/ru/web/store/auth/infrastructure/persistence/UserJpaRepository.java](../auth-service/src/main/java/ru/web/store/auth/infrastructure/persistence/UserJpaRepository.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### UserRepositoryAdapter

Файл: [auth-service/src/main/java/ru/web/store/auth/infrastructure/persistence/UserRepositoryAdapter.java](../auth-service/src/main/java/ru/web/store/auth/infrastructure/persistence/UserRepositoryAdapter.java).

- Строка 11: `public UserRepositoryAdapter(UserJpaRepository repository)`.
- Строка 15: `public boolean existsByEmail(String email)`.
- Строка 19: `public User save(User user)`.
- Строка 25: `public Optional<User> findByEmail(String email)`.

### JwtService

Файл: [auth-service/src/main/java/ru/web/store/auth/infrastructure/security/JwtService.java](../auth-service/src/main/java/ru/web/store/auth/infrastructure/security/JwtService.java).

- Строка 18: `public JwtService(`.
- Строка 25: `public String issue(UUID userId, String role)`.

### SecurityConfig

Файл: [auth-service/src/main/java/ru/web/store/auth/infrastructure/security/SecurityConfig.java](../auth-service/src/main/java/ru/web/store/auth/infrastructure/security/SecurityConfig.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### ApiErrors

Файл: [auth-service/src/main/java/ru/web/store/auth/presentation/ApiErrors.java](../auth-service/src/main/java/ru/web/store/auth/presentation/ApiErrors.java).

- Строка 10: `public ResponseEntity<?> unauthorized()`.
- Строка 15: `public ResponseEntity<?> invalid(IllegalArgumentException e)`.
- Строка 20: `public ResponseEntity<?> missing()`.
- Строка 25: `public ResponseEntity<?> conflict()`.

### AuthController

Файл: [auth-service/src/main/java/ru/web/store/auth/presentation/AuthController.java](../auth-service/src/main/java/ru/web/store/auth/presentation/AuthController.java).

- Строка 16: `public AuthController(AuthUseCase auth)`.
- Строка 27: `public TokenResponse register(@Valid @RequestBody Credentials request)`.
- Строка 32: `public TokenResponse login(@Valid @RequestBody Credentials request)`.
- Строка 38: `public String invalid(IllegalArgumentException e)`.

### HealthController

Файл: [auth-service/src/main/java/ru/web/store/auth/presentation/HealthController.java](../auth-service/src/main/java/ru/web/store/auth/presentation/HealthController.java).

- Строка 9: `public Map<String, String> health()`.

### ProfileController

Файл: [auth-service/src/main/java/ru/web/store/auth/presentation/ProfileController.java](../auth-service/src/main/java/ru/web/store/auth/presentation/ProfileController.java).

- Строка 16: `public ProfileController(ProfileService service)`.
- Строка 23: `public CustomerProfile save(@AuthenticationPrincipal Jwt jwt, @RequestBody Request r)`.
- Строка 29: `public ResponseEntity<CustomerProfile> get(@AuthenticationPrincipal Jwt jwt)`.

## order-service

### DocumentStore

Файл: [order-service/src/main/java/ru/web/store/order/application/DocumentStore.java](../order-service/src/main/java/ru/web/store/order/application/DocumentStore.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### CreateOrderCommand

Файл: [order-service/src/main/java/ru/web/store/order/application/dto/CreateOrderCommand.java](../order-service/src/main/java/ru/web/store/order/application/dto/CreateOrderCommand.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### CatalogPort

Файл: [order-service/src/main/java/ru/web/store/order/application/port/CatalogPort.java](../order-service/src/main/java/ru/web/store/order/application/port/CatalogPort.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### OrderEvents

Файл: [order-service/src/main/java/ru/web/store/order/application/port/OrderEvents.java](../order-service/src/main/java/ru/web/store/order/application/port/OrderEvents.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### SagaResultStore

Файл: [order-service/src/main/java/ru/web/store/order/application/port/SagaResultStore.java](../order-service/src/main/java/ru/web/store/order/application/port/SagaResultStore.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### ShoppingService

Файл: [order-service/src/main/java/ru/web/store/order/application/ShoppingService.java](../order-service/src/main/java/ru/web/store/order/application/ShoppingService.java).

- Строка 12: `public ShoppingService(DocumentStore store)`.
- Строка 16: `public Cart cart(UUID user, List<Cart.Line> lines)`.
- Строка 22: `public Optional<Cart> cart(UUID user)`.
- Строка 26: `public Coupon coupon(UUID user, Coupon coupon)`.
- Строка 31: `public Optional<Coupon> coupon(UUID user, UUID id)`.

### ApplySagaResult

Файл: [order-service/src/main/java/ru/web/store/order/application/usecase/ApplySagaResult.java](../order-service/src/main/java/ru/web/store/order/application/usecase/ApplySagaResult.java).

- Строка 13: `public ApplySagaResult(SagaResultStore store)`.
- Строка 19: `public void execute(StoreEvent event)`.

### CreateOrderUseCase

Файл: [order-service/src/main/java/ru/web/store/order/application/usecase/CreateOrderUseCase.java](../order-service/src/main/java/ru/web/store/order/application/usecase/CreateOrderUseCase.java).

- Строка 18: `public CreateOrderUseCase(OrderRepository orders, OrderEvents events, CatalogPort catalog)`.
- Строка 25: `public Order execute(CreateOrderCommand command)`.

### GetOrderUseCase

Файл: [order-service/src/main/java/ru/web/store/order/application/usecase/GetOrderUseCase.java](../order-service/src/main/java/ru/web/store/order/application/usecase/GetOrderUseCase.java).

- Строка 17: `public GetOrderUseCase(OrderRepository orderRepository)`.
- Строка 22: `public Order execute(UUID id)`.

### Cart

Файл: [order-service/src/main/java/ru/web/store/order/domain/model/Cart.java](../order-service/src/main/java/ru/web/store/order/domain/model/Cart.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Coupon

Файл: [order-service/src/main/java/ru/web/store/order/domain/model/Coupon.java](../order-service/src/main/java/ru/web/store/order/domain/model/Coupon.java).

- Строка 12: `public BigDecimal apply(BigDecimal amount)`.

### Money

Файл: [order-service/src/main/java/ru/web/store/order/domain/model/Money.java](../order-service/src/main/java/ru/web/store/order/domain/model/Money.java).

- Строка 15: `public static Money of(BigDecimal amount, String currency)`.
- Строка 19: `public Money add(Money other)`.

### Order

Файл: [order-service/src/main/java/ru/web/store/order/domain/model/Order.java](../order-service/src/main/java/ru/web/store/order/domain/model/Order.java).

- Строка 19: `public static Order restore(`.
- Строка 32: `public Order(UUID id, UUID customerId, List<OrderItem> items, String currency)`.
- Строка 52: `public void cancel()`.
- Строка 59: `public UUID getId()`.
- Строка 63: `public UUID getCustomerId()`.
- Строка 67: `public List<OrderItem> getItems()`.
- Строка 71: `public OrderStatus getStatus()`.
- Строка 75: `public Money getTotalAmount()`.
- Строка 79: `public Instant getCreatedAt()`.

### OrderItem

Файл: [order-service/src/main/java/ru/web/store/order/domain/model/OrderItem.java](../order-service/src/main/java/ru/web/store/order/domain/model/OrderItem.java).

- Строка 13: `public OrderItem(UUID id, UUID productId, int quantity, Money unitPrice)`.
- Строка 23: `public Money calculateSubtotal()`.
- Строка 28: `public UUID getId()`.
- Строка 32: `public UUID getProductId()`.
- Строка 36: `public int getQuantity()`.
- Строка 40: `public Money getUnitPrice()`.

### OrderStatus

Файл: [order-service/src/main/java/ru/web/store/order/domain/model/OrderStatus.java](../order-service/src/main/java/ru/web/store/order/domain/model/OrderStatus.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### OrderRepository

Файл: [order-service/src/main/java/ru/web/store/order/domain/repository/OrderRepository.java](../order-service/src/main/java/ru/web/store/order/domain/repository/OrderRepository.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Documents

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/Documents.java](../order-service/src/main/java/ru/web/store/order/infrastructure/Documents.java).

- Строка 12: `public Documents(JdbcTemplate jdbc)`.
- Строка 16: `public void save(String kind, UUID id, UUID owner, String payload)`.
- Строка 29: `public Optional<String> find(String kind, UUID id, UUID owner)`.

### CatalogGrpcClient

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/grpc/CatalogGrpcClient.java](../order-service/src/main/java/ru/web/store/order/infrastructure/grpc/CatalogGrpcClient.java).

- Строка 19: `public CatalogGrpcClient(@Value("$`.
- Строка 25: `public Quote quote(UUID id)`.
- Строка 37: `public void close()`.

### OrderSagaListener

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/kafka/OrderSagaListener.java](../order-service/src/main/java/ru/web/store/order/infrastructure/kafka/OrderSagaListener.java).

- Строка 12: `public OrderSagaListener(ApplySagaResult result)`.
- Строка 17: `public void receive(String payload)`.

### OutboxPublisherScheduler

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/kafka/OutboxPublisherScheduler.java](../order-service/src/main/java/ru/web/store/order/infrastructure/kafka/OutboxPublisherScheduler.java).

- Строка 22: `public OutboxPublisherScheduler(`.
- Строка 30: `public void publishPendingEvents()`.

### KafkaConfig

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/KafkaConfig.java](../order-service/src/main/java/ru/web/store/order/infrastructure/KafkaConfig.java).

- Строка 17: `public KafkaConfig(@Value("$`.

### KafkaReliability

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/KafkaReliability.java](../order-service/src/main/java/ru/web/store/order/infrastructure/KafkaReliability.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### KafkaTopics

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/KafkaTopics.java](../order-service/src/main/java/ru/web/store/order/infrastructure/KafkaTopics.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### OrderItemJpaEntity

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/entity/OrderItemJpaEntity.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/entity/OrderItemJpaEntity.java).

- Строка 29: `public OrderItemJpaEntity()`.
- Строка 31: `public OrderItemJpaEntity(`.
- Строка 46: `public UUID getId()`.
- Строка 50: `public OrderJpaEntity getOrder()`.
- Строка 54: `public UUID getProductId()`.
- Строка 58: `public int getQuantity()`.
- Строка 62: `public BigDecimal getUnitPrice()`.
- Строка 66: `public String getCurrency()`.

### OrderJpaEntity

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/entity/OrderJpaEntity.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/entity/OrderJpaEntity.java).

- Строка 38: `public OrderJpaEntity()`.
- Строка 40: `public OrderJpaEntity(`.
- Строка 55: `public void addItem(OrderItemJpaEntity item)`.
- Строка 59: `public UUID getId()`.
- Строка 63: `public UUID getCustomerId()`.
- Строка 67: `public String getStatus()`.
- Строка 71: `public BigDecimal getTotalAmount()`.
- Строка 75: `public String getCurrency()`.
- Строка 79: `public Instant getCreatedAt()`.
- Строка 83: `public List<OrderItemJpaEntity> getItems()`.

### OutboxEventJpaEntity

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/entity/OutboxEventJpaEntity.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/entity/OutboxEventJpaEntity.java).

- Строка 31: `public OutboxEventJpaEntity()`.
- Строка 33: `public OutboxEventJpaEntity(`.
- Строка 50: `public UUID getId()`.
- Строка 54: `public String getAggregateType()`.
- Строка 58: `public UUID getAggregateId()`.
- Строка 62: `public String getEventType()`.
- Строка 66: `public String getPayload()`.
- Строка 70: `public String getStatus()`.
- Строка 74: `public void setStatus(String status)`.
- Строка 78: `public Instant getCreatedAt()`.

### OrderEventsAdapter

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/OrderEventsAdapter.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/OrderEventsAdapter.java).

- Строка 13: `public OrderEventsAdapter(SpringDataOutboxRepository events)`.
- Строка 17: `public void append(StoreEvent event)`.

### PostgresOrderRepository

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/PostgresOrderRepository.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/PostgresOrderRepository.java).

- Строка 23: `public PostgresOrderRepository(SpringDataOrderRepository springDataRepository)`.
- Строка 29: `public Order save(Order order)`.
- Строка 57: `public Optional<Order> findById(UUID id)`.

### SagaResultAdapter

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/SagaResultAdapter.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/SagaResultAdapter.java).

- Строка 12: `public SagaResultAdapter(JdbcTemplate jdbc)`.
- Строка 16: `public void apply(StoreEvent e)`.

### SpringDataOrderRepository

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/SpringDataOrderRepository.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/SpringDataOrderRepository.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### SpringDataOutboxRepository

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/persistence/SpringDataOutboxRepository.java](../order-service/src/main/java/ru/web/store/order/infrastructure/persistence/SpringDataOutboxRepository.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### RedisConfig

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/redis/RedisConfig.java](../order-service/src/main/java/ru/web/store/order/infrastructure/redis/RedisConfig.java).

- Строка 13: `public CacheManager cacheManager(RedisConnectionFactory connectionFactory)`.

### JwtSecurityConfig

Файл: [order-service/src/main/java/ru/web/store/order/infrastructure/security/JwtSecurityConfig.java](../order-service/src/main/java/ru/web/store/order/infrastructure/security/JwtSecurityConfig.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### OrderServiceApplication

Файл: [order-service/src/main/java/ru/web/store/order/OrderServiceApplication.java](../order-service/src/main/java/ru/web/store/order/OrderServiceApplication.java).

- Строка 12: `public static void main(String[] args)`.

### ApiErrors

Файл: [order-service/src/main/java/ru/web/store/order/presentation/ApiErrors.java](../order-service/src/main/java/ru/web/store/order/presentation/ApiErrors.java).

- Строка 10: `public ResponseEntity<?> invalid(IllegalArgumentException e)`.
- Строка 15: `public ResponseEntity<?> missing()`.
- Строка 20: `public ResponseEntity<?> conflict()`.

### OrderController

Файл: [order-service/src/main/java/ru/web/store/order/presentation/controller/OrderController.java](../order-service/src/main/java/ru/web/store/order/presentation/controller/OrderController.java).

- Строка 24: `public OrderController(CreateOrderUseCase createOrderUseCase, GetOrderUseCase getOrderUseCase)`.
- Строка 30: `public ResponseEntity<OrderResponse> createOrder(`.
- Строка 46: `public ResponseEntity<OrderResponse> getOrderById(`.

### ShoppingController

Файл: [order-service/src/main/java/ru/web/store/order/presentation/controller/ShoppingController.java](../order-service/src/main/java/ru/web/store/order/presentation/controller/ShoppingController.java).

- Строка 17: `public ShoppingController(ShoppingService service)`.
- Строка 22: `public Cart cart(@AuthenticationPrincipal Jwt jwt, @RequestBody List<Cart.Line> lines)`.
- Строка 27: `public ResponseEntity<Cart> cart(@AuthenticationPrincipal Jwt jwt)`.
- Строка 32: `public Coupon coupon(@AuthenticationPrincipal Jwt jwt, @RequestBody Coupon coupon)`.
- Строка 37: `public BigDecimal quote(`.

### HealthController

Файл: [order-service/src/main/java/ru/web/store/order/presentation/HealthController.java](../order-service/src/main/java/ru/web/store/order/presentation/HealthController.java).

- Строка 9: `public Map<String, String> health()`.

### CreateOrderRequest

Файл: [order-service/src/main/java/ru/web/store/order/presentation/request/CreateOrderRequest.java](../order-service/src/main/java/ru/web/store/order/presentation/request/CreateOrderRequest.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### OrderResponse

Файл: [order-service/src/main/java/ru/web/store/order/presentation/response/OrderResponse.java](../order-service/src/main/java/ru/web/store/order/presentation/response/OrderResponse.java).

- Строка 15: `public static OrderResponse fromDomain(Order order)`.

## inventory-service

### CatalogService

Файл: [inventory-service/src/main/java/ru/web/store/inventory/application/CatalogService.java](../inventory-service/src/main/java/ru/web/store/inventory/application/CatalogService.java).

- Строка 13: `public CatalogService(ProductRepository products)`.
- Строка 18: `public Product create(Product product)`.
- Строка 23: `public Product get(UUID id)`.

### CategoryService

Файл: [inventory-service/src/main/java/ru/web/store/inventory/application/CategoryService.java](../inventory-service/src/main/java/ru/web/store/inventory/application/CategoryService.java).

- Строка 12: `public CategoryService(DocumentStore store)`.
- Строка 16: `public Category save(UUID owner, Category category)`.
- Строка 21: `public Optional<Category> get(UUID owner, UUID id)`.

### DocumentStore

Файл: [inventory-service/src/main/java/ru/web/store/inventory/application/DocumentStore.java](../inventory-service/src/main/java/ru/web/store/inventory/application/DocumentStore.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### InventorySaga

Файл: [inventory-service/src/main/java/ru/web/store/inventory/application/InventorySaga.java](../inventory-service/src/main/java/ru/web/store/inventory/application/InventorySaga.java).

- Строка 12: `public InventorySaga(SagaStore store)`.
- Строка 18: `public void handle(StoreEvent event)`.

### SagaStore

Файл: [inventory-service/src/main/java/ru/web/store/inventory/application/SagaStore.java](../inventory-service/src/main/java/ru/web/store/inventory/application/SagaStore.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### WalletStore

Файл: [inventory-service/src/main/java/ru/web/store/inventory/application/WalletStore.java](../inventory-service/src/main/java/ru/web/store/inventory/application/WalletStore.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Category

Файл: [inventory-service/src/main/java/ru/web/store/inventory/domain/Category.java](../inventory-service/src/main/java/ru/web/store/inventory/domain/Category.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Payment

Файл: [inventory-service/src/main/java/ru/web/store/inventory/domain/Payment.java](../inventory-service/src/main/java/ru/web/store/inventory/domain/Payment.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Product

Файл: [inventory-service/src/main/java/ru/web/store/inventory/domain/Product.java](../inventory-service/src/main/java/ru/web/store/inventory/domain/Product.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### ProductRepository

Файл: [inventory-service/src/main/java/ru/web/store/inventory/domain/ProductRepository.java](../inventory-service/src/main/java/ru/web/store/inventory/domain/ProductRepository.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Reservation

Файл: [inventory-service/src/main/java/ru/web/store/inventory/domain/Reservation.java](../inventory-service/src/main/java/ru/web/store/inventory/domain/Reservation.java).

- Строка 12: `public Reservation finish(boolean paid)`.

### Wallet

Файл: [inventory-service/src/main/java/ru/web/store/inventory/domain/Wallet.java](../inventory-service/src/main/java/ru/web/store/inventory/domain/Wallet.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### CatalogGrpcServer

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/CatalogGrpcServer.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/CatalogGrpcServer.java).

- Строка 17: `public CatalogGrpcServer(ProductRepository products)`.
- Строка 22: `public void start() throws Exception`.
- Строка 50: `public void stop()`.

### Documents

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/Documents.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/Documents.java).

- Строка 12: `public Documents(JdbcTemplate jdbc)`.
- Строка 16: `public void save(String kind, UUID id, UUID owner, String payload)`.
- Строка 29: `public Optional<String> find(String kind, UUID id, UUID owner)`.

### KafkaConfig

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/KafkaConfig.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/KafkaConfig.java).

- Строка 17: `public KafkaConfig(@Value("$`.

### KafkaReliability

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/KafkaReliability.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/KafkaReliability.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### KafkaTopics

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/KafkaTopics.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/KafkaTopics.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### PostgresProducts

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/PostgresProducts.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/PostgresProducts.java).

- Строка 12: `public PostgresProducts(JdbcTemplate jdbc)`.
- Строка 16: `public Product save(Product p)`.
- Строка 27: `public Optional<Product> find(UUID id)`.

### PostgresSagaStore

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/PostgresSagaStore.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/PostgresSagaStore.java).

- Строка 13: `public PostgresSagaStore(JdbcTemplate jdbc)`.
- Строка 17: `public boolean begin(StoreEvent e)`.
- Строка 23: `public void emit(StoreEvent e)`.
- Строка 31: `public boolean reserve(StoreEvent e)`.
- Строка 57: `public boolean charge(StoreEvent e)`.
- Строка 81: `public void finish(StoreEvent e, boolean success)`.

### PostgresWallets

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/PostgresWallets.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/PostgresWallets.java).

- Строка 14: `public PostgresWallets(JdbcTemplate jdbc)`.
- Строка 19: `public Wallet deposit(Wallet w)`.
- Строка 32: `public Wallet get(UUID id)`.

### RedisConfig

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/redis/RedisConfig.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/redis/RedisConfig.java).

- Строка 13: `public CacheManager cacheManager(RedisConnectionFactory connectionFactory)`.

### SagaMessaging

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/SagaMessaging.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/SagaMessaging.java).

- Строка 19: `public SagaMessaging(InventorySaga saga, JdbcTemplate jdbc, KafkaTemplate<String, String> kafka)`.
- Строка 26: `public void receive(String payload)`.
- Строка 32: `public void publish() throws Exception`.

### JwtSecurityConfig

Файл: [inventory-service/src/main/java/ru/web/store/inventory/infrastructure/security/JwtSecurityConfig.java](../inventory-service/src/main/java/ru/web/store/inventory/infrastructure/security/JwtSecurityConfig.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### InventoryServiceApplication

Файл: [inventory-service/src/main/java/ru/web/store/inventory/InventoryServiceApplication.java](../inventory-service/src/main/java/ru/web/store/inventory/InventoryServiceApplication.java).

- Строка 11: `public static void main(String[] args)`.

### AccountsController

Файл: [inventory-service/src/main/java/ru/web/store/inventory/presentation/AccountsController.java](../inventory-service/src/main/java/ru/web/store/inventory/presentation/AccountsController.java).

- Строка 18: `public AccountsController(WalletStore wallets, CategoryService categories)`.
- Строка 26: `public Wallet deposit(@AuthenticationPrincipal Jwt jwt, @RequestBody Deposit request)`.
- Строка 32: `public Wallet wallet(@AuthenticationPrincipal Jwt jwt)`.
- Строка 37: `public Category category(@AuthenticationPrincipal Jwt jwt, @RequestBody Category category)`.
- Строка 42: `public ResponseEntity<Category> category(`.

### ApiErrors

Файл: [inventory-service/src/main/java/ru/web/store/inventory/presentation/ApiErrors.java](../inventory-service/src/main/java/ru/web/store/inventory/presentation/ApiErrors.java).

- Строка 10: `public ResponseEntity<?> invalid(IllegalArgumentException e)`.
- Строка 15: `public ResponseEntity<?> missing()`.
- Строка 20: `public ResponseEntity<?> conflict()`.

### HealthController

Файл: [inventory-service/src/main/java/ru/web/store/inventory/presentation/HealthController.java](../inventory-service/src/main/java/ru/web/store/inventory/presentation/HealthController.java).

- Строка 9: `public Map<String, String> health()`.

### ProductController

Файл: [inventory-service/src/main/java/ru/web/store/inventory/presentation/ProductController.java](../inventory-service/src/main/java/ru/web/store/inventory/presentation/ProductController.java).

- Строка 17: `public ProductController(CatalogService catalog)`.
- Строка 29: `public Product create(@Valid @RequestBody CreateProduct p)`.
- Строка 35: `public ResponseEntity<Product> get(@PathVariable UUID id)`.

## contracts

### CatalogRpc

Файл: [contracts/src/main/java/ru/web/store/contracts/CatalogRpc.java](../contracts/src/main/java/ru/web/store/contracts/CatalogRpc.java).

Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.

### Json

Файл: [contracts/src/main/java/ru/web/store/contracts/Json.java](../contracts/src/main/java/ru/web/store/contracts/Json.java).

- Строка 10: `public static String write(Object value)`.
- Строка 18: `public static <T> T read(String value, Class<T> type)`.

### StoreEvent

Файл: [contracts/src/main/java/ru/web/store/contracts/StoreEvent.java](../contracts/src/main/java/ru/web/store/contracts/StoreEvent.java).

- Строка 17: `public StoreEvent next(String type, String reason)`.
