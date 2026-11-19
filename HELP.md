# Работа с проектом

Основное описание — [README](README.md). Подробные отчёты расположены в [docs](docs/architecture.md), последовательность показа — [presentation.md](docs/presentation.md).

Windows: `gradlew.bat test bootJar`. Linux: `./gradlew test bootJar`.

Запуск готовых JAR в Docker: `docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --build --wait`.

Интеграционные сценарии: `python scripts/integration.py`. Они создают изолированные тестовые записи в реальном стенде и не очищают бизнес-данные после завершения.

Статус контейнеров: `docker compose ps`. Логи: `docker compose logs --tail 100 order-service inventory-service auth-service`. Остановка с сохранением данных: `docker compose down`.

Если старт не завершился, сначала проверьте healthcheck PostgreSQL/Kafka и доступность портов. Существующие тома не удаляются при обычном down. Скрипт создания баз выполняется только при первоначальной инициализации PostgreSQL.
