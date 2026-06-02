# Методы TrainingPlan API MVP Trainer Diary

Канонические методы текущего MVP для простых тренировочных планов: создать план и найти планы тренера. Клиентский просмотр, дневник выполнения, сводные экраны и `ProgramAssignment` не входят в MVP Trainer Diary.

## Общие правила

- Методы плана доступны только тренеру-владельцу через JWT + ownership check.
- Поиск планов выполняется через `trainingPlan.search`, а не через отдельную сводку статусов.
- Отметки выполнения и public endpoints отсутствуют в MVP.
- SLO и rate limits: [06-metrics-and-limits.md](./06-metrics-and-limits.md).

## Методы плана (versioned `/v1/*` и `/v2/*`)

1. **`trainingPlan.create`** — создать простой план для `ClientCard`.
    - *Бизнес-правило*: план создаёт только тренер-владелец карточки.
    - *Валидация*: название 3-120 символов; план содержит минимум одно задание; медданные/фото/видео/rich-media запрещены.

2. **`trainingPlan.search`** — найти/вывести планы тренера.
   - *HTTP endpoints*: `POST /v1/trainingPlan/search` и `POST /v2/trainingPlan/search`.
   - *Бизнес-правило*: возвращает только планы текущего тренера; фильтр `clientCardId` допустим только для карточки этого же тренера.
   - *Фильтры*: `clientCardId`, `searchString` по названию плана, `status`, `pageSize`, `pageNumber`.
   - *Ответ*: список `TrainingPlanResponseObject`, `totalSize`, `pageNumber`, `pageSize`.

> В OpenAPI v1/v2 могут сохраняться методы `read/update/archive` как методы управления ресурсом, но строго минимальный MVP-сценарий в текущем scope — `create` и `search`.

Для v2 в базовых request/response-схемах присутствует `apiVersion` как поле контракта; оно не помечено как обязательное (`required`) в OpenAPI.

## Поля ответа плана в MVP

| Поле | Возвращается тренеру | Комментарий |
|---|---|---|
| `id` | ✅ | Идентификатор плана в приватном API |
| `clientCardId` | ✅ | Связь с клиентской карточкой тренера |
| `title` | ✅ | Название плана |
| `planItems` | ✅ | Список заданий плана |
| `status` | ✅ | `DRAFT`, `ACTIVE`, `ARCHIVED` |
| `version` | ✅ | Версия плана |
| `createdAt` / `updatedAt` | ✅ | Технические timestamps |
| `publicAccess*` | ❌ | Не входит в MVP |
| `completionSummary` и отметки выполнения | ❌ | Не входит в MVP |

## Ссылки без дублирования

- Сущности и связи планов: [ERD](../ERD.md).
- Security/ownership guardrails: [Security Architecture](../SECURITY_ARCHITECTURE.md).
- Общие бизнес-правила API: [05-business-rules.md](./05-business-rules.md).
