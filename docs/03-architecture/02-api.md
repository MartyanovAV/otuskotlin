# API FitBridge trainer-first MVP с публичной ссылкой

Канонический индекс API для trainer-first MVP с публичной ссылкой на тренировочный план. Контракты и правила разбиты по файлам; обзор архитектуры, C4 и deploy должны ссылаться сюда, а не копировать методы.

Для публичного контура актуальны обе версии OpenAPI: `/public/v1/*` и `/public/v2/*` (см. `specs-fitbridge-v1.yaml` и `specs-fitbridge-v2.yaml`).

## MVP-путь

| Путь | Состав |
|---|---|
| Trainer-first сценарий с публичной ссылкой | регистрация/профиль тренера, `ClientCard`, `TrainingPlan`, генерация/закрытие публичной ссылки, public token-only просмотр плана, `CompletionMark`, статус выполнения для тренера |

Не входят в MVP API: регистрация клиента, клиентский кабинет, `ClientProfile`, `Invite`, `AccessGrant`, solo-client дневник/план, granular permissions.

## Канонические файлы API

| Документ | Что является источником истины |
|---|---|
| [01-scope.md](./api/01-scope.md) | Границы MVP публичного доступа к плану/Phase 2 и поверхность API |
| [02-mvp-entities.md](./api/02-mvp-entities.md) | API-сущности и поля MVP |
| [03-mvp-profile-access-methods.md](./api/03-mvp-profile-access-methods.md) | Контракты тренера, карточек клиентов и lifecycle публичной ссылки |
| [04-mvp-diary-plan-methods.md](./api/04-mvp-diary-plan-methods.md) | Контракты планов, public page и completion marks |
| [05-business-rules.md](./api/05-business-rules.md) | Бизнес-правила API, public-link guardrails, support/audit boundaries |
| [06-metrics-and-limits.md](./api/06-metrics-and-limits.md) | SLO, performance targets, rate limits, надёжность |

## Связанные источники

- Security/access/privacy: [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md)
- Data model: [ERD.md](./ERD.md)
- Решение по публичному доступу к плану: [ADR-007](./ADR/ADR-007-public-plan-link-mvp.md)
- Decisions: [ADR index](./01-adrs.md)
