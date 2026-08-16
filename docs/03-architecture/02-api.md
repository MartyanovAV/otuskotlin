# API FitBridge MVP Trainer Diary

Канонический индекс API для MVP **Trainer Diary**: клиентская база и тренировочные планы тренера. Контракты и правила разбиты по файлам; обзор архитектуры, C4 и deploy должны ссылаться сюда, а не копировать методы.

Для текущего MVP актуальны приватные версии Training OpenAPI `/v1/*` и `/v2/*`. Identity profile предоставляется Keycloak через стандартные OIDC endpoints и не дублируется в domain API.

## MVP-путь

| Путь | Состав |
|---|---|
| Trainer Diary | Keycloak registration/login, `ClientCard.create/search`, `TrainingPlan.create/search` |

Не входят в MVP API: регистрация клиента, клиентский кабинет, `ClientProfile`, `Invite`, `AccessGrant`, share/access-сценарии, отдельный клиентский контур, дневник выполнения, сводные экраны, solo-client путь, granular permissions.

## Канонические файлы API

| Документ | Что является источником истины |
|---|---|
| [01-scope.md](./api/01-scope.md) | Границы MVP Trainer Diary/Phase 2 и поверхность API |
| [02-mvp-entities.md](./api/02-mvp-entities.md) | API-сущности и поля MVP |
| [03-mvp-client-card-methods.md](./api/03-mvp-client-card-methods.md) | Контракты карточек клиентов |
| [04-mvp-diary-plan-methods.md](./api/04-mvp-diary-plan-methods.md) | Контракты создания и поиска планов |
| [05-business-rules.md](./api/05-business-rules.md) | Бизнес-правила API, ownership/search guardrails, support/audit boundaries |
| [06-metrics-and-limits.md](./api/06-metrics-and-limits.md) | SLO, performance targets, rate limits, надёжность |

## Связанные источники

- Security/access/privacy: [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md)
- Data model: [ERD.md](./ERD.md)
- Decisions: [ADR index](./01-adrs.md)
