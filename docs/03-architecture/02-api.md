# API FitBridge MVP

Канонический индекс API. Контракты и правила разбиты по файлам; обзор архитектуры, C4 и deploy должны ссылаться сюда, а не копировать методы.

## MVP-пути

| Путь | Состав |
|---|---|
| Trainer-led | профиль тренера, приглашение клиента, `AccessGrant`, назначение плана, выполнение и просмотр статусов |
| Solo-client | профиль клиента, личный дневник, личная программа, отметка выполнения |

## Канонические файлы API

| Документ | Что является источником истины |
|---|---|
| [01-scope.md](./api/01-scope.md) | Границы MVP/Phase 2 и поверхность API |
| [02-mvp-entities.md](./api/02-mvp-entities.md) | API-сущности и поля MVP |
| [03-mvp-profile-access-methods.md](./api/03-mvp-profile-access-methods.md) | Контракты профилей, онбординга, приглашений и доступов |
| [04-mvp-diary-plan-methods.md](./api/04-mvp-diary-plan-methods.md) | Контракты дневника, программ, назначений и выполнения |
| [05-business-rules.md](./api/05-business-rules.md) | Бизнес-правила API, включая access/support/audit/notification scope |
| [06-metrics-and-limits.md](./api/06-metrics-and-limits.md) | SLO, performance targets, rate limits, надёжность |

## Связанные источники

- Security/access/privacy: [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md)
- Data model: [ERD.md](./ERD.md)
- Decisions: [ADR index](./01-adrs.md)
