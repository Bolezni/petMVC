# Деплой petMVP

Приложение: Spring Boot + PostgreSQL + Caddy (HTTPS) в Docker Compose.
Всё, что нужно на сервере — Docker и этот репозиторий.

## 1. Что подготовить

- VPS: 2 vCPU / 4 ГБ RAM / 40 ГБ SSD (Ubuntu 24.04), ~600–900 ₽/мес
- Домен или поддомен, A-запись которого указывает на IP сервера
- Docker + плагин compose на сервере

## 2. Первый запуск

```bash
# на сервере
git clone <repo-url> petMVP && cd petMVP

cp .env.example .env
openssl rand -base64 32     # -> JWT_SECRET
openssl rand -hex 24        # -> ADMIN_KEY
nano .env                   # вписать секреты, DB_PASSWORD, DOMAIN

docker compose up -d --build
docker compose ps
```

Caddy сам получит сертификат Let's Encrypt, когда DNS уже указывает на сервер.
Проверка: `curl -fsS https://<домен>/actuator/health`

## 3. Переменные окружения

| Переменная | Обязательна | Назначение |
|---|---|---|
| `DB_NAME` | нет | Имя БД, по умолчанию `petProject` |
| `DB_USERNAME` | нет | Пользователь PostgreSQL |
| `DB_PASSWORD` | да | Пароль PostgreSQL |
| `JWT_SECRET` | да | base64, минимум 32 байта |
| `ADMIN_KEY` | да | Ключ `X-Admin-Key` для создания специалистов |
| `DOMAIN` | да | Домен для Caddy и HTTPS |
| `STORAGE_PATH` | нет | Каталог медиа внутри контейнера |
| `SQL_DEBUG` | нет | `true` для логов SQL, на проде `false` |
| `SECURITY_LOG_LEVEL` | нет | Уровень логов Spring Security, на проде `INFO` |

## 4. Создание первого специалиста

```bash
curl -X POST https://<домен>/api/v1/admin/specialists \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: $ADMIN_KEY" \
  -d '{"email":"spec@example.com","password":"StrongPass123","passwordRepeat":"StrongPass123","phone":"+79990000000","name":"Имя","gender":"MALE"}'
```

## 5. Бэкапы

Медиа и база — в docker-томах, поэтому бэкап обязателен с первого дня.

```bash
# база
docker compose exec -T db pg_dump -U postgres petProject | gzip > /backup/db-$(date +%F).sql.gz

# медиа
docker run --rm -v petmvp_uploads:/data -v /backup:/backup alpine \
  tar czf /backup/uploads-$(date +%F).tar.gz -C /data .
```

Добавить оба вызова в cron на 03:00 и раз в неделю проверять восстановление на тестовой базе.

## 6. Обновление

```bash
git pull
docker compose up -d --build
```

## 7. Что не настроено сознательно

- Нет rate-limit на `/api/v1/auth/login` — добавить на этапе публичного запуска.
- Медиа хранится на диске сервера, а не в S3 — для пилота достаточно,
  при переезде меняется только `FileStorage` (интерфейс уже выделен).
- Нет горизонтального масштабирования: один инстанс приложения.