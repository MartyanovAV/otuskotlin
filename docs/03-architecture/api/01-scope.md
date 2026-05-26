# Scope API FitBridge MVP

Документ задаёт границы API MVP. Детальные методы находятся в [03](./03-mvp-profile-access-methods.md) и [04](./04-mvp-diary-plan-methods.md); security/access/privacy — в [Security Architecture](../SECURITY_ARCHITECTURE.md); audit logging — в [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

## Разделение scope

| Область | Что означает | Что реализуется |
|-------|--------------|-----------------|
| **MVP / Gate 1** | Обязательный API сокращённого MVP | Регистрация профилей, приглашение клиента, один активный тренер, базовый дневник, простой план, назначение, отметка выполнения, история клиента, pull-model UI статусы, infrastructure audit-oriented logging |
| **Phase 2 / Out of MVP** | Отдельные продуктовые контуры | Продуктовый `AuditEvent` API/сущность, отдельный `Notification` API/provider, push/email/lifecycle communications, расширенная юридическая audit-модель |

Если сущность, поле, метод, правило или лимит помечены как Phase 2 / Out of MVP, они не входят в обязательный MVP API.

## Scope аудита и уведомлений

| Контур | MVP | Phase 2 |
|---|---|---|
| Audit | Masked structured logs через Fluent Bit/OpenSearch; список событий — [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md) | Продуктовый `AuditEvent` API/entity, self-service/admin audit trail, расширенная retention/legal модель |
| Notifications | Pull-model: UI читает статусы из MVP read endpoints/dashboard | `Notification` API/provider, push/email/in-app center, lifecycle communications |

## Поверхность API MVP

- `profile.createClientProfile`, `profile.readClientProfile`, `profile.updateClientProfile`, `profile.deleteClientProfile`
- `profile.createTrainerProfile`, `profile.readTrainerProfile`, `profile.updateTrainerProfile`, `profile.deleteTrainerProfile`
- `onboarding.startTrainerWizard`, `onboarding.completeTrainerWizard`, `dashboard.getTrainerSummary`
- `access.createInvite`, `access.readInvite`, `access.deleteInvite`, `access.acceptInvite`, `access.declineInvite`, `access.readGrant`, `access.revoke`, `access.listGrants`, `access.validateScope`
- `diary.createEntry`, `diary.readEntry`, `diary.updateEntry`, `diary.deleteEntry`, `diary.searchEntries`
- `program.create`, `program.read`, `program.update`, `program.delete`, `program.assign`, `program.readAssignment`, `program.updateAssignment`, `program.deleteAssignment`, `program.completeWorkout`, `program.trackCompletion`

## Ключевые границы

- Domain API MVP — только пользовательские роли `CLIENT`/`TRAINER`; in-product `ADMIN`/support и broad bypass отсутствуют.
- Все MVP `/v1/*` endpoints требуют edge JWT validation и независимую backend-проверку user context/policy.
- Trainer `PROFILE_WRITE` для `ClientProfile`, `Notification` API и продуктовый `AuditEvent` API не входят в MVP.
