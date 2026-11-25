# Результат развёртывания — 25 ноября 2026

## Адрес и состав стенда

Приложение доступно: https://yasakovd.9593647-gu32875.twc1.net/ . Проверка gateway: https://yasakovd.9593647-gu32875.twc1.net/health . В корне возвращается JSON с перечнем сервисов; приложение предоставляет REST API, отдельный браузерный frontend не предусмотрен заданием.

Сервер 136.234.238.30, каталог /home/yasakovd, Compose project store-yasakovd. Выполнено развёртывание готовых JAR в runtime-образы с тегом manual-c76ea3f-20261125. Запущены auth-service, order-service, inventory-service, PostgreSQL, Redis, Kafka и gateway. Все семь контейнеров прошли healthchecks.

## Домен и HTTPS

Gateway подключён к существующей Docker-сети nginx-proxy с alias store-yasakovd-api. Nginx Proxy Manager направляет домен на store-yasakovd-api:80. Создан Proxy Host 9 и сертификат 10 для данного домена; остальные proxy hosts не изменялись. Включены HTTPS и перенаправление HTTP → HTTPS. Действительность сертификата и hostname проверены внешним клиентом; соединение TLS 1.3, срок сертификата до 23 февраля 2027 года.

Внутренние REST-порты, PostgreSQL, Redis и Kafka на host не опубликованы. Для локальной диагностики сервера gateway доступен на 127.0.0.1:18080. Секреты JWT и БД сгенерированы на сервере и находятся только в /home/yasakovd/.env с правами 600.

## Настройки общего сервера

Сервер имеет 4 ГБ RAM и обслуживает несколько учебных проектов. Для своего стека заданы ограничения памяти:

- auth-service: 448 MiB, heap 160 MiB, Metaspace до 192 MiB.
- order-service: 576 MiB, heap 160 MiB, Metaspace до 256 MiB.
- inventory-service: 512 MiB, heap 160 MiB, Metaspace до 256 MiB.
- Kafka: 448 MiB, heap 192 MiB; административные CLI используют отдельный маленький heap.
- PostgreSQL: 192 MiB, shared_buffers 32 MB, max_connections 40.
- Redis: 96 MiB, maxmemory 48 MB, allkeys-lru.
- Gateway: 32 MiB.

Java-приложения ограничены одним доступным процессором для расчёта внутренних пулов, Tomcat — 25 рабочими потоками, Hikari — 5 подключениями. ExitOnOutOfMemoryError обеспечивает завершение JVM при исчерпании памяти, после чего действует restart policy unless-stopped. После окончательной настройки приложения проверены нагрузкой интеграционных сценариев, а не только health endpoint. Нагрузочный тест промышленного уровня не проводился.

Измеренное потребление семи контейнеров после проверки — примерно 1.2 GiB суммарно. Это моментальный снимок, не гарантированный максимум.

## Сохранность и перезапуск

Данные находятся в отдельных именованных томах store-yasakovd_postgres_data, store-yasakovd_redis_data и store-yasakovd_kafka_data. Kafka хранит журнал в /var/lib/kafka/data, retention 72 часа. Пересоздание контейнера не удаляет том. Базы auth_db, order_db, inventory_db принадлежат соответствующим сервисам.

Gateway использует встроенный Docker DNS 127.0.0.11 с обновлением адресов backend каждые 10 секунд. Поэтому пересоздание backend-контейнеров не требует ручной замены их IP в nginx.conf.

На первом прогоне потребовалось увеличить лимит Metaspace сервиса заказов. После пересоздания контейнеров ранее сохранённые заказ и outbox были обработаны. Итоговая проверка базы: PAID — 3, CANCELLED — 3, NEW — 0. Это включает тестовые покупки; идентификаторы и количество записей будут меняться при новых запусках.

## Проверенные сценарии

На развёрнутом стенде выполнена команда:

```sh
cd /home/yasakovd
API_BASE_URL=https://yasakovd.9593647-gu32875.twc1.net \
COMPOSE_FILES=docker-compose.prod.yml,docker-compose.proxy.yml \
python3 scripts/integration.py
```

Результат PASS:

1. JWT обязателен, регистрация и профиль работают через домен.
2. Товары, категории, корзина и купон сохраняются и читаются.
3. Цена заказа поступает из каталога по gRPC, а не из пользовательского поля price.
4. Успешная покупка завершает сагу со статусом PAID, уменьшает остаток и баланс.
5. Недостаток товара приводит к CANCELLED без частичного списания.
6. Недостаток средств приводит к компенсации резерва и CANCELLED.
7. Повторная публикация исходного события с тем же eventId не повторяет списание.
8. Чужой заказ недоступен.
9. Два покупателя последней единицы получают один PAID и один CANCELLED.
10. PostgreSQL, Redis, Kafka, outbox/inbox и gRPC участвуют в проверке реально.

Тест создаёт отдельные демонстрационные записи и не удаляет их. После окончательного прогона сервисы имеют healthy и RestartCount=0.

## Управление

```sh
cd /home/yasakovd
docker compose -f docker-compose.prod.yml -f docker-compose.proxy.yml ps
docker compose -f docker-compose.prod.yml -f docker-compose.proxy.yml logs --tail 100 order-service inventory-service auth-service
```

Повторная сборка и запуск из загруженных JAR: sh scripts/deploy-manual.sh --proxy. Скрипт учитывает REGISTRY_IMAGE, RELEASE_TAG и COMPOSE_PROJECT_NAME из окружения либо .env. Действующий .env при обновлении сохраняется.

Ручной серверный деплой выполнен отдельно от workflow Deploy production. GitHub Actions CI и ранее проведённые локальные проверки описаны в verification.md. Изменения самого Docker host и чужих учебных стеков для запуска приложения не требовались.
