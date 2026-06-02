# Бизнес-правила API MVP Trainer Diary

Канонические бизнес-правила API MVP. Security-алгоритм и threat model — в [Security Architecture](../SECURITY_ARCHITECTURE.md), модель данных — в [ERD](../ERD.md), audit logging — в [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

| Область | Правило MVP |
|---|---|
| Trainer-only registration | Единственный зарегистрированный пользователь MVP — тренер; клиент не регистрируется и не получает кабинет. |
| ClientCard ownership | `ClientCard` создаётся и управляется тренером; это не client-owned `ClientProfile`. |
| TrainingPlan ownership | План принадлежит тренеру и связан с `ClientCard`; приватные операции требуют trainer JWT + ownership check. |
| ClientCard search | `clientCard.search` возвращает только карточки текущего тренера; фильтры: `searchString`, `status`, пагинация. |
| TrainingPlan search | `trainingPlan.search` возвращает только планы текущего тренера; фильтры: `clientCardId`, `searchString`, `status`, пагинация. |
| Share/access scope | Share/access-сценарии и отдельный клиентский контур не входят в текущий MVP. |
| Дневник выполнения | Отметки выполнения и сводка статусов не входят в текущий MVP. |
| Сводные экраны | Отдельный summary endpoint не входит в текущий MVP; списочные сценарии закрываются search-методами. |
| Rate limiting | Приватные create/search операции лимитируются для защиты от abuse. |
| ADMIN/support | In-product `ADMIN`/support роль, support console и broad bypass отсутствуют; операции пилота выполняются вне domain API без чтения sensitive payload. |
| Audit | MVP-аудит — infrastructure audit-oriented masked logs; продуктовый `AuditEvent` API/entity не входит в MVP. |
| Notifications | Отдельный `Notification` API/provider — Phase 2. |
| Future AccessGrant | `AccessGrant`, `Invite`, клиентское подтверждение и отзыв доступа — Phase 2; не добавлять как скрытый MVP requirement. |
