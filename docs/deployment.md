# Сборка и деплой

Текущий стенд развёрнут 25 ноября 2026 года. [Адрес и результаты серверной проверки](deployment-result.md). Сеть NPM на данном сервере — nginx-proxy.

## Локальная сборка

JDK 21. `gradlew.bat test bootJar` на Windows или `./gradlew test bootJar` на Linux. Gradle Wrapper скачивает нужную версию Gradle. Boot JAR каждого сервиса содержит зависимости внутренних domain/application модулей и contracts.

`docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --build --wait` собирает runtime-образы из готовых JAR. Обычный `docker compose up -d --build --wait` собирает JAR внутри builder-контейнера и требует доступа к Maven Central/Gradle distribution.

## Ручной деплой на сервер преподавателя

Ваш домен: **yasakovd.9593647-gu32875.twc1.net**. Сервер: **136.234.238.30**. Каталог проекта: **/home/yasakovd**. Имя Compose-проекта: **store-yasakovd**. Оно изолирует контейнеры, сети и volumes от других учебных проектов. Production Compose не задаёт глобальные container_name или имена томов.

### 1. Подготовить архив на Windows

В PowerShell из D:\Code\Java\store:

```powershell
.\gradlew.bat test bootJar
python scripts/package-release.py
```

Получится build/store-yasakovd-release.zip: три JAR, Dockerfile.runtime, production Compose, настройка gateway, init SQL script и deployment script. JDK и Gradle на сервере не понадобятся. Закрытые ключи и .env в архив не входят.

Сохраните полученный от преподавателя SSH-ключ на своём компьютере, например в `$env:USERPROFILE\.ssh\store_server.key`. Ниже используется этот пример пути. Сам ключ в репозиторий не копируйте.

```powershell
ssh -i "$env:USERPROFILE\.ssh\store_server.key" root@136.234.238.30 "mkdir -p /home/yasakovd"
scp -i "$env:USERPROFILE\.ssh\store_server.key" .\build\store-yasakovd-release.zip root@136.234.238.30:/home/yasakovd/
ssh -i "$env:USERPROFILE\.ssh\store_server.key" root@136.234.238.30
```

### 2. Распаковать и настроить окружение на Linux

Дальнейшие команды выполняются в SSH-сессии сервера:

```sh
cd /home/yasakovd
unzip -o store-yasakovd-release.zip
cp -n .env.example .env
chmod 600 .env
nano .env
```

Если unzip недоступен, при установленном Python 3: `python3 -m zipfile -e store-yasakovd-release.zip .`.

Заполните COMPOSE_PROJECT_NAME=store-yasakovd, REGISTRY_IMAGE=store-yasakovd, RELEASE_TAG=manual. Для JWT_SECRET и DB_PASSWORD создайте собственные значения. Например, `openssl rand -hex 32` генерирует подходящее случайное значение. Для двух секретов выполните команду отдельно. HTTP_PORT=18080 предназначен только для диагностики через loopback сервера. Если порт занят (`ss -ltn | rg ':18080'` либо обычный `ss -ltn`), выберите другой свободный в .env.

### 3. Узнать существующую сеть Nginx Proxy Manager

```sh
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
```

Найдите контейнер с образом jc21/nginx-proxy-manager. Подставьте его реальное имя:

```sh
NPM_CONTAINER=имя_контейнера_из_списка
docker inspect "$NPM_CONTAINER" --format '{{range $name, $config := .NetworkSettings.Networks}}{{println $name}}{{end}}'
```

Выберите его bridge-сеть и впишите её имя в NPM_NETWORK в .env. Существующую сеть и контейнер преподавателя пересоздавать не нужно. Схема с общей Docker-сетью и проксированием по имени контейнера описана в [официальной документации Nginx Proxy Manager](https://nginxproxymanager.com/advanced-config/).

### 4. Собрать и запустить свой проект

```sh
cd /home/yasakovd
sh scripts/deploy-manual.sh --proxy
```

Скрипт строит три runtime-образа из загруженных JAR и запускает production Compose вместе с proxy override. На первом запуске скачиваются базовые Docker-образы. Проверка `--wait` ожидает готовности инфраструктуры и приложений. Gateway публикует только loopback-порт и получает уникальное сетевое имя store-yasakovd-api в сети NPM.

```sh
curl http://127.0.0.1:18080/health
docker compose -f docker-compose.prod.yml -f docker-compose.proxy.yml ps
```

Ожидается `{"status":"UP"}`. Если контейнер не готов, смотрите логи только своего проекта:

```sh
docker compose -f docker-compose.prod.yml -f docker-compose.proxy.yml logs --tail 100 auth-service order-service inventory-service gateway
```

### 5. Настроить домен в Nginx Proxy Manager

Откройте `http://136.234.238.30:81` и войдите с данными преподавателя. В Hosts → Proxy Hosts → Add Proxy Host укажите:

- Domain Names: `yasakovd.9593647-gu32875.twc1.net`.
- Scheme: `http`.
- Forward Hostname / IP: `store-yasakovd-api`.
- Forward Port: `80`.
- Access List: Publicly Accessible.

Сохраните. Откройте `http://yasakovd.9593647-gu32875.twc1.net/health`. Далее на вкладке SSL можно запросить сертификат для этого домена и включить Force SSL после успешной выдачи. На момент выдачи DNS домена должен указывать на сервер, а 80/443 должны обслуживаться существующим NPM.

Gateway маршрутизирует auth/profile в auth-service, orders/cart/coupons в order-service, products/categories/wallet в inventory-service. Заголовок Authorization передаётся стандартным proxy_pass. Поэтому API работает с одним доменом:

```sh
curl -X POST http://yasakovd.9593647-gu32875.twc1.net/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"password123"}'
```

### 6. Обновление и остановка

Для обновления заново соберите архив на Windows, загрузите и распакуйте его в тот же каталог. Существующий .env сохраняйте. Повторите `sh scripts/deploy-manual.sh --proxy`. Бизнес-данные остаются в именованных томах проекта.

Остановка только вашего приложения с сохранением данных:

```sh
cd /home/yasakovd
docker compose -f docker-compose.prod.yml -f docker-compose.proxy.yml down
```

В Portainer ваш стек будет различаться по префиксу store-yasakovd. Управлять другими стеками общего сервера для этого деплоя не требуется.


## GitHub Actions: что запускается автоматически

Репозиторий: https://github.com/F3nd1uwu/store. Конфигурация — .github/workflows/ci.yml. Во вкладке Actions workflow называется CI.

1. unit-tests устанавливает Temurin JDK 21, запускает ./gradlew test и загружает XML/HTML отчёты. Они сохраняются даже при падении тестов.
2. assemble после успешных unit-тестов собирает три bootJar и создаёт ZIP через scripts/package-release.py. Артефакты application-jars и manual-deployment-release сохраняются на 14 дней.
3. integration-tests получает application-jars, запускает реальный Compose с PostgreSQL/Redis/Kafka и scripts/integration.py. Сервисные логи доступны в integration-service-logs даже при ошибке. В конце стенд останавливается.
4. package-images запускается после интеграционных проверок только при push в основную ветку репозитория. Образы публикуются в ghcr.io/f3nd1uwu/store/auth-service, /order-service и /inventory-service; тег — полный commit SHA.

На push в feature-ветку и pull request выполняются проверки и сборка без публикации production-образов. Коммит в main проходит ту же проверку и дополнительно публикует образы. Для CI используется стандартный GitHub-hosted Ubuntu 24.04 runner с Docker; свой privileged runner и Docker-in-Docker не требуются.

Actions закреплены по commit SHA. permissions по умолчанию contents: read; packages: write выдаётся только заданию публикации. Вход в GHCR выполняется встроенным GITHUB_TOKEN, отдельный секрет для публикации из workflow не требуется. См. [официальную документацию GitHub о публикации образов](https://docs.github.com/en/actions/tutorials/publish-packages/publish-docker-images).

## Как получить архив через GitHub

1. Откройте репозиторий → Actions → CI.
2. Выберите успешный запуск нужного commit.
3. В Artifacts скачайте manual-deployment-release.
4. Из скачанного архива извлеките store-yasakovd-release.zip и перенесите на сервер по инструкции выше.

Для полностью ручного деплоя не нужны SSH secrets GitHub и токен GHCR: архив содержит JAR, а runtime-образы собираются на сервере. Локальная команда python scripts/package-release.py остаётся доступна.

## Необязательный ручной деплой через Actions

Workflow .github/workflows/deploy.yml называется Deploy production. Сам по push он не выполняется. Сначала перенесите workflow в основную ветку и дождитесь успешной публикации образов CI.

В Settings → Environments создайте production. В этом environment задайте Variables:

- DEPLOY_HOST = 136.234.238.30.
- DEPLOY_USER = root либо выданный SSH-пользователь.

В Environment secrets добавьте:

- SSH_PRIVATE_KEY — полный текст SSH-ключа с сохранёнными переводами строк.
- SSH_KNOWN_HOSTS — проверенная запись host key сервера в формате known_hosts.

Это обычные GitHub secrets со значениями; загружать переменные типа File не требуется. Пароли DB_PASSWORD и JWT_SECRET остаются в /home/yasakovd/.env на сервере. При необходимости включите подтверждение развёртывания в настройках environment production.

## Подготовка сервера для образов GHCR

Docker Engine и Compose v2 должны быть установлены. Каталог /home/yasakovd уже должен существовать с правами SSH-пользователя. Серверный .env создаётся на основе .env.example, в том числе COMPOSE_PROJECT_NAME=store-yasakovd, NPM_NETWORK и настоящие DB_PASSWORD/JWT_SECRET.

Если GHCR packages закрытые, один раз выполните на сервере docker login ghcr.io, используя GitHub-логин и token classic с read:packages, имеющий доступ к пакетам. Для публичных образов авторизация скачивания не нужна. Пароль GitHub-аккаунта вместо registry token не используется. См. [документацию GHCR](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry).

В режиме Actions REGISTRY_IMAGE автоматически устанавливается в ghcr.io/f3nd1uwu/store, RELEASE_TAG — в переданный commit SHA. Это переопределяет локальные значения .env только для команды деплоя. Ручной режим scripts/deploy-manual.sh продолжает использовать локально собранные store-yasakovd/<service>.

## Запуск и откат

Actions → Deploy production → Run workflow. Выберите основную ветку и в release_sha вставьте полный 40-символьный SHA успешного main-коммита, чьи образы уже опубликованы. Workflow проверяет формат SHA, получает конфигурацию выбранного commit, подключается по SSH, копирует Compose и gateway, скачивает образы и выполняет up -d --wait. concurrency не позволяет двум таким деплоям идти одновременно.

Для отката повторите ручной workflow с SHA предыдущего опубликованного commit. Это откат образов и конфигурации; данные PostgreSQL сохраняются. Обратимость изменений схемы проверяется отдельно.

## Границы эксплуатации

Общий сервер и стек преподавателя не пересоздаются. Production Compose изолирует приложение префиксом store-yasakovd. PostgreSQL/Redis/Kafka и backend REST-порты не публикуются наружу. Gateway присоединяется к существующей сети NPM; домен направляется на store-yasakovd-api:80.

Один Kafka broker и один PostgreSQL — учебная топология. Реальные secrets не попадают в release archive. Результаты локальных проверок приведены в verification.md; фактический запуск Actions виден во вкладке Actions после отправки кода на GitHub.

## Проверка действующего стенда

Интеграционный скрипт поддерживает API_BASE_URL для единого публичного адреса и COMPOSE_FILES для production/proxy. Полная команда и фактические результаты приведены в deployment-result.md. Production Compose содержит ограничения JVM/контейнеров для общего сервера с 4 ГБ RAM и отдельный постоянный том Kafka. Ручной deployment script берёт имя проекта, registry и release tag из .env, если они не переданы через окружение.
