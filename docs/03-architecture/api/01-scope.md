# Scope API FitBridge trainer-first MVP с публичной ссылкой

Документ задаёт границы API trainer-first MVP с публичной ссылкой на тренировочный план. Security/access/privacy — в [Security Architecture](../SECURITY_ARCHITECTURE.md); audit logging — в [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md); архитектурное решение — [ADR-007](../ADR/ADR-007-public-plan-link-mvp.md).

## Разделение scope

| Область | Что означает | Что реализуется |
|-------|--------------|-----------------|
| **MVP / Gate 1** | Обязательный API trainer-first сценария с публичной ссылкой | Тренерский профиль, `ClientCard`, `TrainingPlan`, генерация/закрытие публичной ссылки, public token-only просмотр плана, `CompletionMark`, статус выполнения для тренера, masked logs |
| **Phase 2 / Out of MVP** | Client-owned и расширенные продуктовые контуры | Клиентская регистрация, `ClientProfile`, `Invite`, `AccessGrant`, дневник, solo-client путь, multi-specialist, product `AuditEvent`, `Notification`, billing |

Если сущность, поле, метод, правило или лимит помечены как Phase 2 / Out of MVP, они не входят в обязательный MVP API.

## Поверхность API MVP

### Приватный trainer API (versioned `/v1/*` и `/v2/*`, требуется JWT)

- `trainerProfile.createOrUpdate`, `trainerProfile.readOwn`
- `clientCard.create`, `clientCard.read`, `clientCard.update`, `clientCard.archive`, `clientCard.list`
- `trainingPlan.create`, `trainingPlan.read`, `trainingPlan.update`, `trainingPlan.archive`
- `trainingPlan.generatePublicLink`, `trainingPlan.closePublicLink`, `trainingPlan.readCompletionStatus`
- `dashboard.getTrainerSummary`

### Публичный API по ссылке (versioned `/public/v1/*` и `/public/v2/*`, только token)

- `POST /public/v1/plan/open`, `POST /public/v1/plan/markCompletion`
- `POST /public/v2/plan/open`, `POST /public/v2/plan/markCompletion`

Публичные методы принимают только raw token и минимальный payload отметки (`itemId`, `status`, optional `clientComment`). `clientId`, `clientCardId`, `planId`, `trainerId` не передаются в публичном URL/API.

Для v2 в контракте присутствует поле `apiVersion`, но OpenAPI не помечает его как `required`.

## Ключевые границы

- Domain API MVP — trainer-private + public token-only; зарегистрированной роли `CLIENT` в MVP нет.
- Все приватные versioned endpoints (`/v1/*`, `/v2/*`) требуют edge JWT validation и независимую backend-проверку trainer ownership.
- Public endpoints требуют hash(token), TTL, revoke/status checks, rate limiting и generic errors.
- Raw token не хранится и не логируется; в БД только hash.
- `AccessGrant`, `Invite`, клиентское подтверждение доступа и отзыв доступа клиентом не входят в MVP публичного доступа к плану.
