# HTTP gateway

Gateway — инфраструктурный Nginx, не отдельный бизнес-контекст. Конфигурация: deploy/nginx.conf. Данные в PostgreSQL/Redis не хранит. Слушает внутренний порт 80, в production доступен с сервера на 127.0.0.1:18080 (настраивается HTTP_PORT).

Маршруты:

- /api/v1/auth/* и /api/v1/profile → auth-service:8081.
- /api/v1/orders*, /api/v1/cart*, /api/v1/coupons* → order-service:8082.
- /api/v1/products*, /api/v1/categories*, /api/v1/wallet* → inventory-service:8083.
- / → JSON-описание приложения.
- /health → готовность самого gateway; это не агрегированная проверка зависимостей.

JWT проверяют backend-сервисы. Gateway передаёт исходный URI и Authorization через proxy_pass. Клиенту достаточно одного домена yasakovd.9593647-gu32875.twc1.net. Внешний Nginx Proxy Manager направляет домен на сетевой alias store-yasakovd-api:80.

Compose proxy override подключает только gateway к существующей сети NPM, сохраняя остальные сервисы в собственной сети проекта. Имена контейнеров и томов production формируются с префиксом store-yasakovd. Backend, PostgreSQL, Kafka и Redis не публикуют host-порты.

Проверены nginx -t и HTTP-маршрутизация на локальном стенде. Подключение к сети реального NPM выполняется пользователем по deployment.md.
