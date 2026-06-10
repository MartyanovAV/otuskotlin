# Scope API FitBridge MVP Trainer Diary

Документ задаёт границы API MVP **Trainer Diary**: управление клиентской базой и тренировочными планами. Security/access/privacy — в [Security Architecture](../SECURITY_ARCHITECTURE.md); audit logging — в [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md); архивное решение по публичной ссылке — [ADR-007](../ADR/ADR-007-public-plan-link-mvp.md).

## Разделение scope

| Область | Что означает | Что реализуется |
|-------|--------------|-----------------|
| **MVP / Gate 1** | Обязательный API Trainer Diary | Тренерский профиль, `ClientCard.create/search`, `TrainingPlan.create/search`, masked logs |
| **Phase 2 / Out of MVP** | Client-owned и расширенные продуктовые контуры | Клиентская регистрация, `ClientProfile`, `Invite`, `AccessGrant`, дневник, solo-client путь, multi-specialist, product `AuditEvent`, `Notification`, billing |

Если сущность, поле, метод, правило или лимит помечены как Phase 2 / Out of MVP, они не входят в обязательный MVP API.

## Поверхность API MVP

### Приватный trainer API (versioned `/v1/*` и `/v2/*`, требуется JWT)

- `trainerProfile.createOrUpdate`, `trainerProfile.readOwn`
- `clientCard.create`, `clientCard.search`
- `trainingPlan.create`, `trainingPlan.search`

### Публичный API

Публичный API по ссылке отсутствует в текущем MVP. Endpoints `/public/v1/*` и `/public/v2/*` не являются частью Trainer Diary MVP.

Для v2 в контракте присутствует поле `apiVersion`, но OpenAPI не помечает его как `required`.

## Ключевые границы

- Domain API MVP — trainer-private only; зарегистрированной роли `CLIENT` в MVP нет.
- Все приватные versioned endpoints (`/v1/*`, `/v2/*`) требуют edge JWT validation и независимую backend-проверку trainer ownership.
- Отдельный клиентский контур, share/access-сценарии, дневник выполнения, чтение статуса выполнения и сводные экраны не входят в MVP.
- `AccessGrant`, `Invite`, клиентское подтверждение доступа и отзыв доступа клиентом не входят в MVP Trainer Diary.
