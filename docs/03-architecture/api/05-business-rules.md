# Бизнес-правила API

Канонические бизнес-правила API MVP. Security-алгоритм и threat model — в [Security Architecture](../SECURITY_ARCHITECTURE.md), модель данных — в [ERD](../ERD.md), audit logging — в [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

| Область | Правило MVP |
|---|---|
| Client-owned data | `ClientProfile`, `TrainingEntry` и `ProgramAssignment` принадлежат клиенту; тренер получает доступ только через active `AccessGrant`; отзыв применяется немедленно. |
| Profile fields | `gender` и `goals` — optional/nullable MVP-поля; клиент может читать/заполнять/изменять/очищать их. `heightCm` и body metrics — Phase 2. |
| Access scopes | Минимальные scopes: `PROFILE_READ`, `DIARY_READ`, `DIARY_WRITE`, `PROGRAM_READ`, `PROGRAM_WRITE`. Trainer `PROFILE_WRITE` для `ClientProfile` запрещён в MVP. |
| Deny by default | Доступ разрешён только owner или active `AccessGrant` + нужный scope; расширение scopes требует действия клиента. |
| Invite vs grant | `Invite` не является доступом. `Invite.status`: `PENDING`, `ACCEPTED`, `DECLINED`, `EXPIRED`, `CANCELLED`; `AccessGrant.status`: `ACTIVE`, `REVOKED`, `EXPIRED`. |
| Один активный тренер | MVP поддерживает не более одного active trainer access для клиента. |
| Invite token | Токен одноразовый, хранится только как hash; истёкшее приглашение нельзя принять. |
| Дневник | Ручная запись клиента — источник истины, если не связана с программой; удаление мягкое. |
| Простые планы | Активное назначение использует зафиксированную версию программы; индивидуальные планы без шаблонов и библиотеки упражнений. |
| Видимость | Отчёты и агрегаты строятся только по данным, видимым пользователю после owner/grant/scope проверки. |
| Онбординг тренера | Минимальный путь: регистрация, публичный профиль, первое приглашение; dashboard не показывает демо-данные как реальные. |
| ADMIN/support | In-product `ADMIN`/support роль, support console и broad bypass отсутствуют; операции пилота выполняются вне domain API через Keycloak/runbook без чтения sensitive payload. |
| Audit | MVP-аудит — infrastructure audit-oriented logging; продуктовый `AuditEvent` API/entity не входит в MVP. |
| Notifications | MVP использует pull-model статусы в read endpoints/dashboard; отдельный `Notification` API/provider — Phase 2. |
