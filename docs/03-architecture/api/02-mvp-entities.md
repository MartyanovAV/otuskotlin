# API-сущности MVP Trainer Diary

Сущности текущего MVP: тренер, клиентская карточка и простой тренировочный план. Клиентский контур, сводные экраны и дневник выполнения исключены. Полная модель данных — [ERD](../ERD.md).

## [MVP] TrainerUser

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Внутренний идентификатор тренера в FitBridge | `usr_01HX7M2A9Q` |
| keycloakSubject | Идентификатор субъекта в Keycloak | `3f8d9f3a-7b2b-4f0b-9e2b-8b4d` |
| email | Email для входа тренера | `trainer@example.com` |
| displayName | Отображаемое имя тренера | `Иван Петров` |
| role | Единственная product role MVP: `TRAINER` | `TRAINER` |
| status | `ACTIVE`, `BLOCKED`, `DELETED` | `ACTIVE` |
| createdAt | Дата регистрации | `2026-05-27T10:00:00Z` |

> Роль/аккаунт `CLIENT` не требуется в MVP Trainer Diary. Клиент не входит в authenticated/public API текущего MVP.

## [MVP] TrainerProfile

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор профиля тренера | `trp_01HX7M4C2L` |
| userId | Пользователь-тренер | `usr_01HX7M2A9Q` |
| publicName | Отображаемое имя тренера в продукте | `Иван Петров` |
| specialization | Короткая специализация | `силовой тренинг` |
| onboardingStatus | `NEW`, `PROFILE_READY`, `FIRST_CLIENT_CREATED`, `FIRST_PLAN_CREATED`, `COMPLETED` | `FIRST_PLAN_CREATED` |

## [MVP] ClientCard

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор карточки | `ccd_01HX7M3B1K` |
| trainerUserId | Тренер-владелец карточки | `usr_01HX7M2A9Q` |
| displayName | Имя/псевдоним клиента для тренера | `Мария` |
| note | Внутренняя заметка тренера | `после отпуска начать мягко` |
| status | `ACTIVE`, `ARCHIVED` | `ACTIVE` |
| createdAt / updatedAt | Технические timestamps | `2026-05-27T10:05:00Z` |

> `ClientCard` не является `ClientProfile`: у неё нет `userId`, клиентского ownership и личного кабинета.

## [MVP] TrainingPlan

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор плана | `tpl_01HX7MCLAV` |
| clientCardId | Карточка клиента, для которой создан план | `ccd_01HX7M3B1K` |
| trainerUserId | Автор и владелец плана | `usr_01HX7M2A9Q` |
| title | Название плана | `Стартовая неделя` |
| planBody | Структура заданий плана | `{ "days": [...] }` |
| status | `DRAFT`, `ACTIVE`, `ARCHIVED` | `ACTIVE` |
| version | Версия плана | `1` |
| createdAt / updatedAt | Технические timestamps | `2026-05-27T11:00:00Z` |

> `TrainingPlan` в MVP не содержит share/access state и клиентского дневника. Поиск планов выполняется через `/trainingPlan/search` по `clientCardId`, `searchString`, `status` и пагинации.

## API-сущности вне MVP / Phase 2

| Entity | Статус |
|---|---|
| `ClientProfile` | Future client-owned profile |
| `Invite` | Future invite/consent flow |
| `AccessGrant` | Future granular permissions |
| Отметки выполнения / дневник | Future client-owned diary/history |
| `TrainingEntry` | Future client-owned diary/history |
| `ProgramAssignment` | Future assignment model после отделения plan/program/history |
| `Notification`, `AuditEvent`, `Subscription` | Phase 2+ продуктовые контуры |
