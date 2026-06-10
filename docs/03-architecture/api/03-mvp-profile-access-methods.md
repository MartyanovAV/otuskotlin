# Методы Trainer и ClientCard API MVP Trainer Diary

Канонические методы текущего MVP для тренера и клиентских карточек. Все методы используют стиль `domain.action` через `POST` + JSON.

## Общие правила

- Авторизация приватного API: см. [Security Architecture](../SECURITY_ARCHITECTURE.md). Domain API MVP — `TRAINER` only, без in-product `ADMIN`/support bypass.
- Клиент не регистрируется и не получает JWT.
- Share/access методы, клиентский контур, сводные экраны и статусы выполнения не входят в MVP.
- `AccessGrant`, `Invite`, клиентское подтверждение доступа и отзыв доступа клиентом не входят в MVP Trainer Diary.

## Приватные бизнес-функции тренера

1. **`trainerProfile.createOrUpdate`** — создать или обновить минимальный профиль тренера.
   - *Бизнес-правило*: доступно только зарегистрированному тренеру с валидным JWT.
   - *Валидация*: `publicName` 2-80 символов; не хранить лишние персональные/медицинские данные клиента в профиле тренера.

2. **`trainerProfile.readOwn`** — получить собственный профиль тренера.
   - *Бизнес-правило*: возвращает только профиль caller'а.

3. **`clientCard.create`** — создать минимальную клиентскую карточку.
   - *Бизнес-правило*: карточка принадлежит тренеру и не создаёт клиентский аккаунт.
   - *Валидация*: `displayName` 1-120 символов; медданные, фото/видео, body metrics и расширенные profile fields запрещены.

4. **`clientCard.read`** — прочитать карточку клиента.
   - *Бизнес-правило*: доступна только тренеру-владельцу.

5. **`clientCard.update`** — изменить карточку клиента.
   - *Бизнес-правило*: изменения доступны только тренеру-владельцу.

6. **`clientCard.archive`** — архивировать карточку клиента.
   - *Бизнес-правило*: новые планы для архивной карточки не создаются.

7. **`clientCard.search`** — поиск карточек тренера.
   - *HTTP endpoints*: `POST /v1/clientCard/search` и `POST /v2/clientCard/search`.
   - *Бизнес-правило*: список фильтруется по `trainerUserId`; поддерживает `searchString`, `status`, `pageSize`, `pageNumber`.

> В OpenAPI v1/v2 могут сохраняться `read/update/archive` как методы управления ресурсом, но строго минимальный пользовательский сценарий текущего MVP — `create` и `search`.

## Ссылки без дублирования

- Статусы и ограничения данных: [ERD](../ERD.md).
- Ownership и threat model: [Security Architecture](../SECURITY_ARCHITECTURE.md).
- Rate limits и SLO: [06-metrics-and-limits.md](./06-metrics-and-limits.md).
