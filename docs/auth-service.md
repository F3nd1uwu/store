# Auth service: устройство и основные операции

## Назначение

HTTP 8081, база auth_db. Контекст отвечает за регистрацию, проверку пароля, выпуск JWT и личный профиль. Пользователь и профиль — два самостоятельных агрегата. Auth не знает структуры заказов и остатков.

## Регистрация: путь по коду

1. `presentation/AuthController.register` принимает Credentials, выполняет @Valid: email обязателен, пароль минимум 8 символов.
2. `application/AuthUseCase.register` приводит email к нижнему регистру через Locale.ROOT и проверяет UserRepository.existsByEmail.
3. PasswordEncoder создаёт BCrypt hash с солью. Открытый пароль не сохраняется.
4. `infrastructure/persistence/UserRepositoryAdapter.save` преобразует доменный User в UserEntity и вызывает Spring Data JPA.
5. `infrastructure/security/JwtService.issue` формирует JWT с sub=UUID, role=CUSTOMER, iat и exp; явно выбирает HS256.
6. Контроллер возвращает 201 и TokenResponse. Транзакция регистрации управляется @Transactional на use case; ошибка сохранения откатывает запись.

## Вход

`AuthController.login` → `AuthUseCase.login` → UserRepository.findByEmail → BCrypt.matches → TokenIssuer.issue. Неизвестный email, неправильный пароль и active=false приводят к BadCredentialsException и HTTP 401 через ApiErrors. Повторная регистрация возвращает 409; конкурентный дубль также блокируется UNIQUE constraint БД.

## Профиль

`ProfileController` получает UUID из проверенного JWT, не из тела запроса. PUT /api/v1/profile передаёт name/address/phone в CustomerProfile. `ProfileService.save` сериализует доменную модель и сохраняет её через DocumentStore. `infrastructure/Documents.save` выполняет INSERT ... ON CONFLICT DO UPDATE, только если owner_id совпадает. GET выбирает документ по kind=profile, id=userId, owner_id=userId. Имя и адрес обязательны.

## PostgreSQL

`users`: id UUID PK; email UNIQUE NOT NULL; password_hash NOT NULL; role NOT NULL; active NOT NULL. JPA entity задаёт схему, Hibernate ddl-auto=update создаёт или обновляет её на старте.

`documents`: составной PK(kind,id), owner_id UUID NOT NULL, payload TEXT NOT NULL. Для профиля kind=profile, id=owner_id=UUID пользователя. Таблица создаётся schema.sql. Payload содержит JSON CustomerProfile.

PostgreSQL — долговременное хранилище; Redis и Kafka auth-сервису не требуются. Создание users и schema.sql принадлежит только auth_db.

## API

- POST `/api/v1/auth/register`: email/password → 201, accessToken/tokenType.
- POST `/api/v1/auth/login`: email/password → 200, accessToken/tokenType; неверные данные → 401.
- PUT `/api/v1/profile`: Bearer JWT, name/address/phone → профиль.
- GET `/api/v1/profile`: Bearer JWT → профиль либо 404.
- GET `/health`: публичная проверка готовности HTTP.

Пример профиля: `{"name":"Иван","address":"Саратов, улица Примерная, 1","phone":"+70000000000"}`.

## JWT

Секрет читается из JWT_SECRET и должен совпадать с конфигурацией order/inventory. Токен живёт один час. JwtDecoder в SecurityConfig проверяет подпись и время действия. Проверка остальных сервисов локальная: синхронный запрос в auth при каждом обращении не нужен. SecurityFilterChain не создаёт HTTP-сессию, CSRF выключен для stateless Bearer API.

Refresh token и немедленный отзыв уже выпущенного access token не входят в текущий API: токен действует до exp. Изменение active влияет на следующий вход. Общий симметричный ключ выбран для локального стенда; его значение для сервера задаётся отдельно от репозитория.

## Проверки и точки показа

`AuthTest.duplicateEmailCannotCreateAccount` проверяет запрет дублирования нормализованного email. `unknownAccountCannotLogin` проверяет отказ неизвестному аккаунту. Дополнительно wrongPasswordCannotIssueToken проверяет отсутствие выдачи JWT при неверном пароле, registrationStoresHashInsteadOfPassword — сохранение хеша вместо открытого пароля. Интеграционный сценарий регистрирует двух пользователей, сохраняет профиль и использует выданные токены в двух других сервисах. При показе кода начните с AuthController, затем AuthUseCase, UserRepositoryAdapter и JwtService.
