# Методы TrainingPlan и Completion API MVP

Канонические методы Gate 1 для простых планов, публичного просмотра по token и отметок выполнения. Полноценный дневник клиента и `ProgramAssignment` не входят в MVP публичного доступа к плану.

## Общие правила

- Приватные методы плана доступны только тренеру-владельцу через JWT + ownership check.
- Публичные методы доступны только по raw token; backend хранит и ищет только hash.
- `CompletionMark` — минимальный дочерний объект/value object `TrainingPlan`, а не полноценная дневниковая запись клиента.
- SLO и rate limits: [06-metrics-and-limits.md](./06-metrics-and-limits.md).

## Приватные методы плана (`/v1/*`)

1. **`trainingPlan.create`** — создать простой план для `ClientCard`.
   - *Бизнес-правило*: план создаёт только тренер-владелец карточки.
   - *Валидация*: название 3-120 символов; план содержит минимум одно задание; медданные/фото/видео/rich-media запрещены.

2. **`trainingPlan.read`** — получить план в кабинете тренера.
   - *Бизнес-правило*: возвращает полный trainer-private view только владельцу.

3. **`trainingPlan.update`** — изменить план.
   - *Бизнес-правило*: если public link активна, изменения должны увеличивать `version` или явно обновлять snapshot, чтобы клиент видел актуальную версию.

4. **`trainingPlan.archive`** — архивировать план.
   - *Бизнес-правило*: архивный план недоступен публично; активная ссылка должна быть закрыта или стать недоступной.

5. **`trainingPlan.readCompletionStatus`** — получить статус выполнения.
   - *Бизнес-правило*: тренер видит отметки выполнения по собственному плану и агрегированный статус карточки.

## Публичные методы по ссылке (`/public/v1/*`)

6. **`publicPlan.openByToken`** — открыть план по публичной ссылке.
   - *Вход*: raw token только в защищённом канале; не принимать `planId`, `clientCardId`, `trainerId`.
   - *Security*: hash(token), TTL/status/revoke checks, rate limiting, generic errors.
   - *Ответ*: минимальный public payload: название плана, задания, безопасное имя тренера/сервиса, состояние доступности ссылки.

7. **`publicPlan.markCompletion`** — оставить отметку выполнения.
   - *Вход*: raw token + минимальная отметка (`itemRef`, `status`, опциональный короткий комментарий).
   - *Бизнес-правило*: создаёт `CompletionMark`, не создаёт клиентский аккаунт, дневник, `AccessGrant` или `ClientProfile`.
   - *Валидация*: token active; `itemRef` существует в публичном snapshot; комментарий ограничен по длине и не логируется.
   - *Idempotency*: повтор submit должен быть безопасен через idempotency/fingerprint policy.

## Whitelist публичного payload

| Поле | Разрешено публично | Комментарий |
|---|---|---|
| `planTitle` | ✅ | Без внутренних id |
| `trainerPublicName` | ✅ | Только безопасное публичное имя |
| `planItems` | ✅ | Только задания, необходимые клиенту |
| `completionState` | ✅ | Текущая отметка клиента по ссылке, если нужна UX-логика |
| `clientCard.note` | ❌ | Внутренняя заметка тренера |
| Internal ids | ❌ | `clientCardId`, `trainingPlanId`, `trainerUserId` не возвращать |
| Raw token/hash | ❌ | Не возвращать и не логировать |

## Ссылки без дублирования

- Сущности и связи планов/отметок: [ERD](../ERD.md).
- Public-link guardrails: [Security Architecture](../SECURITY_ARCHITECTURE.md).
- Общие бизнес-правила API: [05-business-rules.md](./05-business-rules.md).
