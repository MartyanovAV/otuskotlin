# ERD - FitBridge MVP

Документ фиксирует целевую модель данных MVP FitBridge для двух полноценных путей: `Trainer-led` и `Solo-client (PLG)`. ERD основан на MVP scope, API entities и traceability matrix.

## Статус

| Параметр | Значение |
|---|---|
| Статус | Draft |
| Область | MVP / Gate 1 |
| Источники | `docs/01-business/MVP_SCOPE_SUMMARY.md`, `docs/03-architecture/api/02-mvp-entities.md`, `docs/02-analysis/REQUIREMENTS_TRACEABILITY_MATRIX.md` |
| DBMS | PostgreSQL |

## ERD

```mermaid
erDiagram
    FITBRIDGE_USER ||--o| CLIENT_PROFILE : owns
    FITBRIDGE_USER ||--o| TRAINER_PROFILE : owns
    FITBRIDGE_USER ||--o{ TRAINING_ENTRY : authors
    FITBRIDGE_USER ||--o{ PROGRAM : authors

    CLIENT_PROFILE ||--o{ ACCESS_GRANT : grants
    TRAINER_PROFILE ||--o{ ACCESS_GRANT : receives

    FITBRIDGE_USER ||--o{ INVITE : sends
    FITBRIDGE_USER ||--o{ INVITE : receives
    CLIENT_PROFILE ||--o{ INVITE : targets_client
    TRAINER_PROFILE ||--o{ INVITE : targets_trainer

    CLIENT_PROFILE ||--o{ TRAINING_ENTRY : owns
    CLIENT_PROFILE ||--o{ PROGRAM_ASSIGNMENT : receives
    PROGRAM ||--o{ PROGRAM_ASSIGNMENT : assigned_as
    ACCESS_GRANT o|--o{ PROGRAM_ASSIGNMENT : authorizes_trainer_assignment
    PROGRAM_ASSIGNMENT o|--o{ TRAINING_ENTRY : links_completion

    FITBRIDGE_USER {
        string id PK
        string keycloakSubject UK
        string phone
        string displayName
        string roles
        string status
        string locale
        string timezone
        datetime createdAt
        datetime lastLoginAt
    }

    CLIENT_PROFILE {
        string id PK
        string userId FK
        string fullName
        string gender
        int heightCm
        string goals
        string visibility
        datetime createdAt
        datetime updatedAt
        datetime archivedAt
    }

    TRAINER_PROFILE {
        string id PK
        string userId FK
        string publicName
        string specialization
        string bio
        string certificates
        string contactPolicy
        string onboardingStatus
        datetime createdAt
        datetime archivedAt
    }

    ACCESS_GRANT {
        string id PK
        string clientProfileId FK
        string trainerProfileId FK
        string grantedByUserId FK
        string status
        string scopes
        datetime grantedAt
        datetime revokedAt
        datetime expiresAt
    }

    INVITE {
        string id PK
        string type
        string senderUserId FK
        string recipientUserId FK
        string recipientEmail
        string targetClientProfileId FK
        string targetTrainerProfileId FK
        string proposedScopes
        string tokenHash
        string status
        datetime createdAt
        datetime expiresAt
    }

    TRAINING_ENTRY {
        string id PK
        string clientProfileId FK
        string authorUserId FK
        string linkedProgramAssignmentId FK
        string type
        datetime occurredAt
        string title
        string exercisesJson
        int durationMinutes
        int intensity
        string mood
        string notes
        string source
        string visibility
        datetime createdAt
        datetime updatedAt
        datetime deletedAt
    }

    PROGRAM {
        string id PK
        string authorUserId FK
        string type
        string title
        string goal
        string difficulty
        int durationWeeks
        string workoutsJson
        string status
        int version
        datetime createdAt
        datetime archivedAt
    }

    PROGRAM_ASSIGNMENT {
        string id PK
        string programId FK
        string clientProfileId FK
        string assignedByUserId FK
        string accessGrantId FK
        date startDate
        date endDate
        string status
        int completionPercent
        int currentWeek
        string clientFeedback
        datetime createdAt
        datetime updatedAt
        datetime cancelledAt
    }
```

## Entity Responsibilities

| Entity | Назначение | Ownership |
|---|---|---|
| `FITBRIDGE_USER` | Локальная проекция пользователя Keycloak и базовых доменных ролей | Связана с `keycloakSubject`; аутентификация остаётся в Keycloak |
| `CLIENT_PROFILE` | Клиентский профиль, владелец дневника, истории, доступов и назначений | Принадлежит клиенту через `userId` |
| `TRAINER_PROFILE` | Профессиональный профиль тренера и состояние минимального онбординга | Принадлежит пользователю-тренеру через `userId` |
| `ACCESS_GRANT` | Разрешение тренеру работать с данными клиента | Создаётся только после явного подтверждения клиента |
| `INVITE` | Одноразовое приглашение клиента тренером | Не является доступом до `acceptInvite` |
| `TRAINING_ENTRY` | Запись дневника клиента или факт выполнения тренировки | Всегда принадлежит `CLIENT_PROFILE`; автором может быть клиент или тренер со scope |
| `PROGRAM` | Простой тренировочный план | Автором может быть тренер или solo-клиент |
| `PROGRAM_ASSIGNMENT` | Назначение программы клиенту на период | Для trainer-led связано с `ACCESS_GRANT`; для solo-client `accessGrantId = null` |

## Key Constraints

| Constraint | Rule |
|---|---|
| Один клиентский профиль MVP | `FITBRIDGE_USER` имеет не более одного основного `CLIENT_PROFILE` |
| Один тренерский профиль MVP | `FITBRIDGE_USER` имеет не более одного основного `TRAINER_PROFILE` |
| Один активный тренер MVP | У `CLIENT_PROFILE` не более одного `ACCESS_GRANT` в статусе `ACTIVE` |
| Приглашение не равно доступу | `INVITE.status = ACCEPTED` создаёт или активирует `ACCESS_GRANT`; pending invite не даёт доступа |
| Deny by default | Все операции с клиентскими данными требуют владельца или активный `ACCESS_GRANT` с нужным scope |
| Solo-client assignment | `PROGRAM_ASSIGNMENT.accessGrantId` может быть `null`, если клиент назначил свою программу сам себе |
| Версионирование программ | Активное назначение должно ссылаться на фиксированную версию программы или snapshot структуры `workoutsJson` |
| Soft delete | Профили, дневниковые записи, программы и назначения архивируются/помечаются, а не удаляются физически в пользовательском сценарии |

## Sensitive Data Notes

| Area | Notes |
|---|---|
| Health-adjacent fields | `heightCm`, `goals`, `intensity`, `mood`, `notes`, упражнения/вес/RPE требуют отдельной классификации данных |
| Audit | В MVP `AuditEvent` API и отдельная audit entity/table не моделируются; критичные события логируются инфраструктурно через ADR-006 |
| Logs | Персональные и health-adjacent данные не должны попадать в логи без маскирования |
| Deletion | Полноценная модель consent/privacy/deletion требует отдельного `BR-009-consent-privacy-deletion.md` |

## Scope аудита и уведомлений MVP

| Контур | MVP модель данных | Phase 2 |
|---|---|---|
| Аудит | Нет таблицы `AuditEvent`; обязательные события пишутся как masked structured logs: accept/decline invite, grant/revoke access, profile delete/archive request, diary create/update/delete, program assign/update/cancel, complete workout, access denied for scope | Отдельная audit entity/API, продуктовый audit trail, расширенная retention/legal модель |
| Уведомления | Нет таблицы `Notification`; UI читает статусы приглашений, доступов, назначений и выполнения через существующие read endpoints/dashboard | Отдельный `Notification` API/provider, push/email/in-app notification center и lifecycle communications |

## Phase 2 / Out Of Scope Entities

| Entity | Reason |
|---|---|
| `Measurement` | Замеры и графики прогресса вынесены после MVP |
| `Report` | Отдельный отчётный модуль и экспорт отчётов не входят в MVP |
| `Subscription` | Встроенный биллинг, лимиты и автоплатежи не входят в MVP |
| `Notification` | Отдельный push/email `Notification` API/provider не входит в MVP; используется pull-model UI |
| `AuditEvent` | Продуктовый audit API и отдельная audit entity перенесены в Phase 2; MVP покрыт infrastructure audit-oriented logging |
| `Team`, `Studio`, `SpecialistRole` | Командный и multi-specialist production-сценарии вынесены за MVP |

## Open Decisions

| Decision | Impact |
|---|---|
| Snapshot vs version link for assigned programs | Нужно решить, хранить ли полную копию программы в назначении или ссылку на версию |
| Data classification matrix | Нужна для полей, связанных с самочувствием, целями, интенсивностью и тренировочной нагрузкой |
| Retention policy | Нужны сроки хранения для профилей, soft-deleted записей, invites, access grants и инфраструктурных логов |
