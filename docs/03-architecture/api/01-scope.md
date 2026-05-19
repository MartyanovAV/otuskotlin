# API FitBridge B2B2C SaaS

Документ описывает бизнес-API платформы FitBridge: цифровой среды, где клиент владеет своей тренировочной историей, а независимые тренеры и студии получают доступ только после явного разрешения клиента.

Область документа после сокращения MVP: обязательны только критический путь BR-001, BR-002, BR-004, BR-005 и BR-006 в минимальном составе; BR-007, расширенные уведомления, отчётный модуль, замеры, шаблоны, multi-specialist и team-management являются Phase 2/design reserve. Контракты ниже могут содержать будущие поля и операции, но Gate 1 не требует их реализации.

#### 0. Разделение scope

| Scope | Что означает | Что реализуется |
|-------|--------------|-----------------|
| **MVP / Gate 1** | Обязательный API сокращённого MVP | Регистрация профилей, приглашение клиента, один активный тренер, базовый дневник, простой план, назначение, отметка выполнения, история клиента, обязательный аудит |
| **Phase 2** | Целевые контракты после проверки MVP | Замеры, check-in, шаблоны, отчёты, adherence, биллинг, расширенные уведомления, тарифные лимиты |
| **Design reserve** | Архитектурный резерв без обязательства реализации | Multi-specialist, команды студий, командные роли, shared notes |

**Правило чтения документа:** если сущность, поле, метод, правило или лимит помечены как Phase 2/design reserve, они не входят в Gate 1 даже при наличии описанного контракта. Для MVP допускается хранить future-ready поля в модели, но публичный API и UI не обязаны их поддерживать.

**MVP API surface:**

- `profile.createClientProfile`, `profile.readClientProfile`, `profile.updateClientProfile`
- `profile.createTrainerProfile`, `profile.readTrainerProfile`, `profile.updateTrainerProfile`
- `onboarding.startTrainerWizard`, `onboarding.completeTrainerWizard`, `dashboard.getTrainerSummary`
- `access.createInvite`, `access.readInvite`, `access.deleteInvite`, `access.acceptInvite`, `access.declineInvite`, `access.readGrant`, `access.revoke`, `access.listGrants`, `access.validateScope`
- `diary.createEntry`, `diary.readEntry`, `diary.updateEntry`, `diary.deleteEntry`, `diary.searchEntries`
- `program.create`, `program.read`, `program.update`, `program.delete`, `program.assign`, `program.readAssignment`, `program.updateAssignment`, `program.deleteAssignment`, `program.completeWorkout`, `program.trackCompletion`
- `audit.createEvent`

**Phase 2/design reserve API surface:**

- Замеры и check-in: `diary.createMeasurement`, `diary.readMeasurement`, `diary.updateMeasurement`, `diary.deleteMeasurement`, `diary.createCheckIn`
- Экспорт и отчёты: `diary.exportHistory`, `analytics.*`
- Шаблоны: `program.search`, `program.cloneTemplate`, режим `TEMPLATE` у `Program`
- Биллинг: `billing.*`, `Subscription`, `PaymentTransaction`
- Расширенные уведомления: `notification.*` вне простого in-app статуса приглашения/назначения
- Multi-specialist и команды: `collaboration.*`, `team.*`

