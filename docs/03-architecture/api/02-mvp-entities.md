# API-сущности trainer-first MVP с публичной ссылкой

Сущности Gate 1: тренер, клиентская карточка, простой план, публичный доступ как technical state плана и отметка выполнения. Полная модель данных — [ERD](../ERD.md).

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

> Роль/аккаунт `CLIENT` не требуется в MVP публичного доступа к плану. Клиент открывает публичную ссылку без регистрации.

## [MVP] TrainerProfile

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор профиля тренера | `trp_01HX7M4C2L` |
| userId | Пользователь-тренер | `usr_01HX7M2A9Q` |
| publicName | Имя, безопасное для показа клиенту по ссылке | `Иван Петров` |
| specialization | Короткая специализация | `силовой тренинг` |
| onboardingStatus | `NEW`, `PROFILE_READY`, `FIRST_LINK_CREATED`, `COMPLETED` | `FIRST_LINK_CREATED` |

## [MVP] ClientCard

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор карточки | `ccd_01HX7M3B1K` |
| trainerUserId | Тренер-владелец карточки | `usr_01HX7M2A9Q` |
| displayName | Имя/псевдоним клиента для тренера | `Мария` |
| note | Внутренняя заметка тренера; не попадает в public payload | `после отпуска начать мягко` |
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
| planBody | Структура заданий; публично отдаётся только whitelisted subset | `{ "days": [...] }` |
| status | `DRAFT`, `ACTIVE`, `ARCHIVED` | `ACTIVE` |
| version | Версия плана | `1` |
| publicAccessTokenHash | Hash public token, raw token не хранится | `sha256:...` |
| publicAccessStatus | `NONE`, `ACTIVE`, `REVOKED`, `EXPIRED` | `ACTIVE` |
| publicAccessExpiresAt | TTL публичной ссылки | `2026-06-10T10:00:00Z` |
| publicAccessRevokedAt | Момент закрытия ссылки | `null` |

> Публичная ссылка не является отдельной API/product entity. Public-link lifecycle хранится как technical state плана.

## [MVP] CompletionMark

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор отметки или вложенного элемента | `cmk_01HX7MAJ8S` |
| trainingPlanId | План, по которому оставлена отметка | `tpl_01HX7MCLAV` |
| itemRef | Ссылка на день/упражнение/элемент плана | `week1.day1` |
| status | `DONE`, `SKIPPED` | `DONE` |
| completedAt | Когда клиент отметил выполнение | `2026-05-28T19:30:00Z` |
| clientComment | Короткий необязательный комментарий; raw value не логируется | `тяжело, но сделал` |

> На MVP `CompletionMark` допустимо хранить как value object внутри `TrainingPlan`; отдельная таблица — техническая оптимизация, а не расширение продуктовой модели.

## API-сущности вне MVP / Phase 2

| Entity | Статус |
|---|---|
| `ClientProfile` | Future client-owned profile |
| `Invite` | Future invite/consent flow |
| `AccessGrant` | Future granular permissions |
| `TrainingEntry` | Future client-owned diary/history |
| `ProgramAssignment` | Future assignment model после отделения plan/program/history |
| `Notification`, `AuditEvent`, `Subscription` | Phase 2+ продуктовые контуры |
