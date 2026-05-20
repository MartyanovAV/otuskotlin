# API FitBridge B2B2C SaaS

Документ описывает бизнес-API платформы FitBridge: цифровой среды, где клиент владеет своей тренировочной историей, а независимые тренеры и студии получают доступ только после явного разрешения клиента.

Область документа после сокращения MVP: обязательны только критический путь BR-001, BR-002, BR-004, BR-005 и BR-006 в минимальном составе.

#### 0. Разделение scope

| Scope | Что означает | Что реализуется |
|-------|--------------|-----------------|
| **MVP / Gate 1** | Обязательный API сокращённого MVP | Регистрация профилей, приглашение клиента, один активный тренер, базовый дневник, простой план, назначение, отметка выполнения, история клиента, обязательный аудит |

**Правило чтения документа:** если сущность, поле, метод, правило или лимит помечены как. Для MVP допускается хранить future-ready поля в модели, но публичный API и UI не обязаны их поддерживать.

**MVP API surface:**

- `profile.createClientProfile`, `profile.readClientProfile`, `profile.updateClientProfile`, `profile.deleteClientProfile`
- `profile.createTrainerProfile`, `profile.readTrainerProfile`, `profile.updateTrainerProfile`, `profile.deleteTrainerProfile`
- `onboarding.startTrainerWizard`, `onboarding.completeTrainerWizard`, `dashboard.getTrainerSummary`
- `access.createInvite`, `access.readInvite`, `access.deleteInvite`, `access.acceptInvite`, `access.declineInvite`, `access.readGrant`, `access.revoke`, `access.listGrants`, `access.validateScope`
- `diary.createEntry`, `diary.readEntry`, `diary.updateEntry`, `diary.deleteEntry`, `diary.searchEntries`
- `program.create`, `program.read`, `program.update`, `program.delete`, `program.assign`, `program.readAssignment`, `program.updateAssignment`, `program.deleteAssignment`, `program.completeWorkout`, `program.trackCompletion`
