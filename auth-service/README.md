# Auth service

[Полный отчёт по сервису](../docs/auth-service.md): ответственность, агрегаты, REST API, таблицы PostgreSQL, кеширование, безопасность, транзакции и основные методы.

[Карта исходников и методов](../docs/code-map.md), [общая архитектура](../docs/architecture.md), [сценарий демонстрации](../docs/presentation.md).

Слои собираются отдельно: `auth-service:domain`, `auth-service:application`, исполняемый `auth-service`. Запуск и тесты выполняются из корня репозитория; инструкции в общем README.
