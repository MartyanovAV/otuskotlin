# API FitBridge MVP Trainer Diary

Канонический индекс API для MVP **Trainer Diary**: клиентская база и тренировочные планы тренера. Контракты и правила разбиты по файлам; обзор архитектуры, C4 и deploy должны ссылаться сюда, а не копировать методы.

Для текущего MVP актуальны приватные версии OpenAPI `/v1/*` и `/v2/*` (см. `specs-profile-v1.yaml`, `specs-training-v1.yaml` и их v2 аналоги). Публичный контур исключён.

## MVP-путь

| Путь | Состав |
|---|---|
| Trainer Diary | профиль тренера, `ClientCard.create/search`, `TrainingPlan.create/search` |

Не входят в MVP API: регистрация клиента, клиентский кабинет, `ClientProfile`, `Invite`, `AccessGrant`, share/access-сценарии, отдельный клиентский контур, дневник выполнения, сводные экраны, solo-client путь, granular permissions.

## Канонические файлы API

| Документ | Что является источником истины |
|---|---|
| [01-scope.md](./api/01-scope.md) | Границы MVP Trainer Diary/Phase 2 и поверхность API |
| [02-mvp-entities.md](./api/02-mvp-entities.md) | API-сущности и поля MVP |
| [03-mvp-profile-access-methods.md](./api/03-mvp-profile-access-methods.md) | Контракты тренера и карточек клиентов |
| [04-mvp-diary-plan-methods.md](./api/04-mvp-diary-plan-methods.md) | Контракты создания и поиска планов |
| [05-business-rules.md](./api/05-business-rules.md) | Бизнес-правила API, ownership/search guardrails, support/audit boundaries |
| [06-metrics-and-limits.md](./api/06-metrics-and-limits.md) | SLO, performance targets, rate limits, надёжность |

## Связанные источники

- Security/access/privacy: [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md)
- Data model: [ERD.md](./ERD.md)
- Архивное решение по публичному доступу к плану: [ADR-007](./ADR/ADR-007-public-plan-link-mvp.md) — Rejected/Archived для текущего MVP
- Decisions: [ADR index](./01-adrs.md)
