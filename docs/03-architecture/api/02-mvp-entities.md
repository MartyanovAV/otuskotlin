# API-сущности MVP Trainer Diary

Domain API содержит только клиентские карточки и тренировочные планы. Identity-тренера поступает из проверенного JWT и не принимается в request body.

## AuthPrincipal — внутренняя модель

| Поле | Источник | Назначение |
|---|---|---|
| userId | `JWT.sub` | Стабильный owner id |
| username | `JWT.preferred_username` | Логин и техническое отображение |
| roles | `JWT.realm_access.roles` | Проверка `TRAINER` |

`AuthPrincipal` не является публичной API-сущностью и не сохраняется отдельной таблицей.

## ClientCard

| Поле | Бизнес-описание | Пример |
|---|---|---|
| id | Идентификатор карточки | `ccd_01HX7M3B1K` |
| ownerId | Внутренний owner; заполняется из `sub`, не входит в request | `3f8d9f3a-...` |
| displayName | Имя/псевдоним клиента для тренера | `Мария` |
| note | Внутренняя заметка тренера | `после отпуска начать мягко` |
| status | `ACTIVE`, `ARCHIVED` | `ACTIVE` |
| createdAt / updatedAt | Технические timestamps | `2026-05-27T10:05:00Z` |

`ClientCard` не является `ClientProfile`: у неё нет клиентского аккаунта или client-owned history.

## TrainingPlan

| Поле | Бизнес-описание | Пример |
|---|---|---|
| id | Идентификатор плана | `tpl_01HX7MCLAV` |
| clientCardId | Карточка клиента того же owner | `ccd_01HX7M3B1K` |
| ownerId | Автор/владелец из `JWT.sub`, не входит в request | `3f8d9f3a-...` |
| title | Название плана | `Стартовая неделя` |
| planBody | Структура заданий | `{ "days": [...] }` |
| status | `DRAFT`, `ACTIVE`, `ARCHIVED` | `ACTIVE` |
| version | Версия плана | `1` |

## Phase 2

`ClientProfile`, `Invite`, `AccessGrant`, расширенный профессиональный профиль тренера, дневник выполнения, `TrainingEntry`, `ProgramAssignment`, billing и product audit остаются вне MVP.
