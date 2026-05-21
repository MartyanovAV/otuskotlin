# API FitBridge B2B2C SaaS

Документ описывает бизнес-API платформы FitBridge: цифровой среды, где клиент владеет своей тренировочной историей, а независимые тренеры и студии получают доступ только после явного разрешения клиента.

Область документа после сокращения MVP: обязательны только критический путь BR-001, BR-002, BR-004, BR-005, BR-006 и минимальный privacy/deletion-контур BR-009. Audit и notification scope трактуются строго по этому документу и ADR-006.

#### 0. Разделение scope

| Scope | Что означает | Что реализуется |
|-------|--------------|-----------------|
| **MVP / Gate 1** | Обязательный API сокращённого MVP | Регистрация профилей, приглашение клиента, один активный тренер, базовый дневник, простой план, назначение, отметка выполнения, история клиента, pull-model UI статусы и инфраструктурный audit-oriented logging через ADR-006 |
| **Phase 2 / Out of MVP** | Отдельные продуктовые контуры | Продуктовый `AuditEvent` API/сущность, отдельный `Notification` API/provider, push/email/lifecycle communications, расширенная юридическая audit-модель |

**Правило чтения документа:** если сущность, поле, метод, правило или лимит помечены как Phase 2 / Out of MVP, они не входят в обязательный MVP API. Для MVP допускается хранить future-ready поля в модели, но публичный API и UI не обязаны их поддерживать.

#### 0.1. Scope аудита: MVP vs Phase 2

**MVP:** аудит реализуется как infrastructure audit-oriented logging: masked structured logs приложения отправляются через Fluent Bit в OpenSearch согласно [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md). В MVP нет публичного или внутреннего продуктового `AuditEvent` API и нет отдельной audit entity/table в бизнес-модели.

Обязательные MVP audit events:

- `access.acceptInvite` — клиент принимает приглашение;
- `access.declineInvite` — клиент отклоняет приглашение;
- `access.grant` — доступ тренеру выдан или активирован;
- `access.revoke` — доступ тренеру отозван;
- `profile.deleteOrArchiveRequested` — пользователь запросил удаление или архивацию профиля;
- `diary.createEntry` — создана запись дневника;
- `diary.updateEntry` — изменена запись дневника;
- `diary.deleteEntry` — запись дневника удалена/помечена удалённой;
- `program.assign` — программа назначена клиенту;
- `program.updateAssignment` — назначение программы изменено;
- `program.cancelAssignment` — назначение программы отменено;
- `program.completeWorkout` — тренировка по программе отмечена выполненной;
- `access.validateScope` с результатом `DENIED` — отказ доступа из-за отсутствующего scope или неактивного grant.

**Phase 2:** продуктовый `AuditEvent` API, отдельная audit entity/API, расширенный юридический audit trail, retention и self-service/админские сценарии анализа аудита.

#### 0.2. Scope уведомлений: MVP vs Phase 2

**MVP:** используется pull-model: UI получает статусы приглашений, доступов, назначений программ и выполнения тренировок из MVP read endpoints и dashboard. В MVP нет отдельного `Notification` API, push/email provider, очереди коммуникаций или lifecycle communications.

**Phase 2:** отдельный `Notification` API/provider, push/email/in-app notification center, lifecycle communications, напоминания по оплате, лимитам и вовлечению.

**Поверхность API MVP:**

- `profile.createClientProfile`, `profile.readClientProfile`, `profile.updateClientProfile`, `profile.deleteClientProfile`
- `profile.createTrainerProfile`, `profile.readTrainerProfile`, `profile.updateTrainerProfile`, `profile.deleteTrainerProfile`
- `onboarding.startTrainerWizard`, `onboarding.completeTrainerWizard`, `dashboard.getTrainerSummary`
- `access.createInvite`, `access.readInvite`, `access.deleteInvite`, `access.acceptInvite`, `access.declineInvite`, `access.readGrant`, `access.revoke`, `access.listGrants`, `access.validateScope`
- `diary.createEntry`, `diary.readEntry`, `diary.updateEntry`, `diary.deleteEntry`, `diary.searchEntries`
- `program.create`, `program.read`, `program.update`, `program.delete`, `program.assign`, `program.readAssignment`, `program.updateAssignment`, `program.deleteAssignment`, `program.completeWorkout`, `program.trackCompletion`
