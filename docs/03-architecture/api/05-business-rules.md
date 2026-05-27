# Бизнес-правила API trainer-first MVP с публичной ссылкой

Канонические бизнес-правила API MVP. Security-алгоритм и threat model — в [Security Architecture](../SECURITY_ARCHITECTURE.md), модель данных — в [ERD](../ERD.md), audit logging — в [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

| Область | Правило MVP |
|---|---|
| Trainer-only registration | Единственный зарегистрированный пользователь MVP — тренер; клиент не регистрируется и не получает кабинет. |
| ClientCard ownership | `ClientCard` создаётся и управляется тренером; это не client-owned `ClientProfile`. |
| TrainingPlan ownership | План принадлежит тренеру и связан с `ClientCard`; приватные операции требуют trainer JWT + ownership check. |
| Public link as capability | Публичная ссылка не product entity; это public-access state конкретного `TrainingPlan`. |
| Token storage | Raw token не хранится и не логируется; в БД только hash, TTL, status, revoke timestamps. |
| Public endpoint | Публичный API принимает только token; `clientId`, `planId`, `trainerId` не передаются как параметры доступа. |
| Public payload | Только минимальный план и форма отметки; внутренние id, заметки тренера, sensitive payload и health-adjacent данные запрещены. |
| CompletionMark | Минимальная отметка выполнения по ссылке; допустима как value object внутри `TrainingPlan`; не равна дневниковой записи клиента. |
| Revoke/close | Тренер может закрыть ссылку; после закрытия публичный просмотр и новые отметки запрещены. |
| TTL | Каждая ссылка имеет срок жизни; бессрочные публичные ссылки не допускаются как baseline. |
| Rate limiting | Public open/mark и generate-link операции лимитируются для защиты от перебора/abuse. |
| ADMIN/support | In-product `ADMIN`/support роль, support console и broad bypass отсутствуют; операции пилота выполняются вне domain API без чтения sensitive payload. |
| Audit | MVP-аудит — infrastructure audit-oriented masked logs; продуктовый `AuditEvent` API/entity не входит в MVP. |
| Notifications | MVP использует pull-model статусы в read endpoints/dashboard; отдельный `Notification` API/provider — Phase 2. |
| Future AccessGrant | `AccessGrant`, `Invite`, клиентское подтверждение и отзыв доступа — Phase 2; не добавлять как скрытый MVP requirement. |
