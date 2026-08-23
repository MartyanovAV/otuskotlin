# ERD — FitBridge Trainer Diary MVP

Модель данных MVP содержит только trainer-owned рабочие сущности. Учётная запись и минимальный identity-профиль находятся в Keycloak и не дублируются в PostgreSQL.

## Статус

| Параметр | Значение |
|---|---|
| Статус | Updated after ADR-007 |
| Область | Клиентские карточки и тренировочные планы тренера |
| DBMS | PostgreSQL |
| Внешний owner | Keycloak subject (`JWT.sub`) |

## ERD

```mermaid
erDiagram
    CLIENT_CARD ||--o{ TRAINING_PLAN : receives

    CLIENT_CARD {
        string id PK
        string owner_user_id
        string created_by_user_id
        string display_name
        string note
        string status
        string lock
        datetime created_at
        datetime updated_at
        datetime archived_at
    }

    TRAINING_PLAN {
        string id PK
        string client_card_id FK
        string owner_user_id
        string created_by_user_id
        string title
        jsonb plan_items
        string status
        int version
        string lock
        datetime created_at
        datetime updated_at
        datetime archived_at
        datetime completed_at
        string difficulty
        string coach_comment
    }
```

## Правила ownership

| Правило | Реализация |
|---|---|
| Канонический owner | `owner_user_id = JWT.sub` |
| Источник owner | Только серверный Auth Guard; owner не принимается из request payload |
| Чтение и поиск | Каждый запрос фильтруется по `owner_user_id` текущего principal |
| Изменение и архивирование | Допускаются только при совпадении `entity.owner_user_id == principal.userId` |
| Создание плана | `ClientCard.owner_user_id` должен совпадать с `principal.userId`; план получает тот же owner |
| Ссылка на пользователя | `owner_user_id` не является FK: Keycloak владеет lifecycle пользователя |

## Статусы MVP

| Сущность | Значения | Правило |
|---|---|---|
| `CLIENT_CARD.status` | `ACTIVE`, `ARCHIVED` | Архивированная карточка не получает новые планы |
| `TRAINING_PLAN.status` | `DRAFT`, `ACTIVE`, `ARCHIVED`, `COMPLETED` | `DRAFT` — подготовительный черновик плана (активируется через `/trainingPlan/activate`); `ACTIVE` — активный план; `COMPLETED` — завершённая тренировка с оценкой сложности и комментарием; `ARCHIVED` — план в архиве |

## Индексы

- `CLIENT_CARD(ownerId, status, displayName)` для приватного поиска карточек.
- `TRAINING_PLAN(ownerId, clientCardId, status, title)` для приватного поиска планов.
- Индекс/constraint на `TRAINING_PLAN.clientCardId`; совпадение owner проверяется приложением в одной транзакции.

## Границы данных

- Username, email, имя, credentials, роль и IAM-статус находятся в Keycloak.
- PostgreSQL не содержит локальной user projection или профессионального профиля тренера.
- `ClientCard` не является клиентским аккаунтом или `ClientProfile`.
- Медданные, body metrics, фото/видео, `AccessGrant`, `Invite` и дневник выполнения остаются вне MVP.
