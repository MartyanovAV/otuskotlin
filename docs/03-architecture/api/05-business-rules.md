# API Business Rules

Бизнес-правила API для MVP.

#### 3. Бизнес-правила

1. **[MVP] Клиент владеет данными**:
   - `ClientProfile`, `TrainingEntry` и `ProgramAssignment` принадлежат клиенту.
   - `ClientProfile.gender` и `ClientProfile.goals` входят в MVP как optional/nullable поля: клиент может читать, заполнять, изменять и очищать их самостоятельно; отсутствие значений не блокирует onboarding или базовое использование.
   - `ClientProfile.heightCm` не является supported MVP полем и относится к Phase 2 / later measurement and body metrics scope.
   - Тренер получает доступ только через активный `AccessGrant`.
   - Отзыв доступа не требует согласия тренера и применяется немедленно.

2. **[MVP] Модель разрешений AccessGrant**:
    - Минимальные scopes MVP: `PROFILE_READ`, `DIARY_READ`, `DIARY_WRITE`, `PROGRAM_READ`, `PROGRAM_WRITE`.
    - `PROFILE_READ` разрешает тренеру читать только разрешённые поля `ClientProfile` через активный `AccessGrant`, включая optional `gender` и `goals` при подтверждённом доступе.
    - Trainer `PROFILE_WRITE` для клиентского профиля запрещён в MVP, не входит в минимальные scopes и может рассматриваться только как Phase 2 / out of MVP.
    - Write-права тренера в MVP ограничены тренировочным процессом: дневником, программами, назначениями и статусами выполнения в рамках `DIARY_WRITE` и `PROGRAM_WRITE`.
    - Статусы `AccessGrant`: `ACTIVE`, `REVOKED`, `EXPIRED`; только `ACTIVE` разрешает доступ при выполнении owner/grant/scope policy.
    - По умолчанию доступ запрещён.
    - Расширение scopes всегда требует действия клиента.

3. **[MVP] Приглашения**:
    - Приглашение не является доступом.
    - Статусы `Invite`: `PENDING`, `ACCEPTED`, `DECLINED`, `EXPIRED`, `CANCELLED`.
    - MVP поддерживает один активный тренерский доступ для клиента (массив `activeTrainerIds` ограничен 1 элементом на уровне бизнес-логики).
   - Токен приглашения одноразовый и хранится только в виде хэша.
   - Истёкшие приглашения нельзя принять; нужно создать новое приглашение.

4. **[MVP] Дневник тренировок**:
   - Ручная запись клиента считается источником истины, если не связана с программой.
   - Удаление реализуется мягко, чтобы сохранить аудит.

5. **[MVP] Простые планы**:
   - Активные назначения используют зафиксированную версию программы.
   - MVP поддерживает только индивидуальные планы без шаблонов и библиотеки упражнений.
   - Тренер не может назначить программу клиенту без доступа `PROGRAM_WRITE`.

6. **[MVP] Отчёты и видимость**:
   - Отчёты не должны раскрывать данные клиента после отзыва доступа.
   - Агрегаты строятся только по видимым пользователю данным.

7. **[MVP] Онбординг тренера**:
    - Минимальный путь: регистрация, заполнение публичного профиля, приглашение первого клиента.
    - Тренерский dashboard не показывает демо-данные как реальные клиентские данные.

8. **[MVP] Граница ADMIN/support**:
    - В MVP нет in-product `ADMIN`/support роли, личного кабинета администратора, support console и broad bypass для domain API.
    - Пользовательский domain API остаётся `CLIENT`/`TRAINER` only: `profile.readClientProfile`, `access.readGrant`, дневник, история и программы всегда проходят owner/grant/scope policy.
    - Операционные действия пилота выполняются вне product UI/domain API через Keycloak + controlled operational runbook/provisioning: допуск/отзыв пилотного тренера, блокировка пользователя, отмена invite edge case, отзыв доступа по privacy/deletion процессу.
    - Support/operator не получает права читать `ClientProfile`, дневник, тренировочную историю, health-adjacent payload или raw token; в логах фиксируются только internal ids, action, result, requestId и время.

9. **[MVP] Инфраструктурный Аудит**:
    - MVP-аудит — это infrastructure audit-oriented logging через structured logs, Fluent Bit и OpenSearch согласно `ADR-006`; продуктовый `AuditEvent` API и отдельная audit entity не входят в MVP.
    - Обязательные события MVP: принятие/отклонение приглашения, выдача доступа, отзыв доступа, запрос удаления/архивации профиля, создание/изменение/удаление записи дневника, назначение/изменение/отмена программы, отметка выполнения тренировки, отказ доступа из-за scope, а также controlled support-operation без sensitive payload.
    - В audit-oriented logs фиксируются internal ids, action, result, requestId и время события; персональные данные, health-adjacent payload и raw invite token маскируются или не логируются.

10. **[MVP] Pull-model статусы вместо Notification API**:
   - Статусы приглашений, доступов, назначений и выполнения пользователь видит через MVP read endpoints и `dashboard.getTrainerSummary`.
   - Отдельный `Notification` API, push/email provider и lifecycle communications не входят в MVP и относятся к Phase 2.
