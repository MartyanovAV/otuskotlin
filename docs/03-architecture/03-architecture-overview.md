# Обзор архитектуры FitBridge

Документ является краткой картой архитектуры MVP. Детали не дублируются: источником истины являются документы из таблицы ниже.

## Канонические источники

| Область | Источник |
|---|---|
| Архитектурные решения | [ADR index](./01-adrs.md) и файлы [ADR/](./ADR/) |
| C4-диаграммы | [Context](./c4/C4_CONTEXT.md), [Container](./c4/C4_CONTAINER.md), [Component](./c4/C4_COMPONENT.md) |
| Модель данных | [ERD.md](./ERD.md) |
| Security/access/privacy | [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md) |
| API index | [02-api.md](./02-api.md) |
| API entities/contracts/rules/limits | [api/](./api/) |
| Deployment/run guide | [../../deploy/README.md](../../deploy/README.md) |

## Карта MVP

FitBridge — B2B2C SaaS для клиентской тренировочной истории, доступов тренера и простых программ. MVP покрывает два пути:

| Путь | Кратко | Канонический контракт |
|---|---|---|
| Trainer-led | Тренер приглашает клиента, клиент принимает доступ, тренер назначает план, клиент отмечает выполнение | [api/03](./api/03-mvp-profile-access-methods.md), [api/04](./api/04-mvp-diary-plan-methods.md) |
| Solo-client | Клиент ведёт дневник и назначает личную программу без тренера | [api/04](./api/04-mvp-diary-plan-methods.md) |

## Архитектурные принципы

- **Client-owned data:** клиент владеет профилем, дневником, историей и доступами.
- **Deny by default:** каждый `/v1/*` запрос проходит edge JWT validation и независимую backend-проверку owner/grant/scope.
- **No support bypass in MVP:** in-product `ADMIN`/support роль и support console отсутствуют; операции пилота идут через Keycloak/runbook без чтения sensitive payload.
- **POST Full API:** бизнес-операции оформлены как `domain.action` через `POST` + JSON.
- **Per-entity repository rule:** repository ports/implementations находятся внутри соответствующей сущности, общего repo-layer на уровне сервиса нет.

## Технологический контур

| Область | Решение | ADR/источник |
|---|---|---|
| Backend | Kotlin + Ktor | [ADR-004](./ADR/ADR-004-kotlin.md), [ADR-003](./ADR/ADR-003-ktor.md) |
| Identity | Keycloak/OIDC/JWT | [ADR-001](./ADR/ADR-001-use-keycloak.md), [SECURITY](./SECURITY_ARCHITECTURE.md) |
| API | POST Full, HTTPS/JSON | [ADR-002](./ADR/ADR-002-post-full-api.md), [02-api](./02-api.md) |
| Data | PostgreSQL | [ADR-005](./ADR/ADR-005-use-postgresql.md), [ERD](./ERD.md) |
| Observability | OpenSearch + Dashboards + Fluent Bit как внешний/platform-контур, не application boundary | [ADR-006](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md) |

## Решения MVP, которые не переопределяются в обзорных документах

- Нет in-product `ADMIN`/support роли, support console и broad bypass.
- Trainer `PROFILE_WRITE` для `ClientProfile` отсутствует.
- `gender`/`goals` — optional MVP-поля; `heightCm` и body metrics — Phase 2.
- OpenSearch остаётся external/platform observability, не частью application boundary.
- `Invite.status` и `AccessGrant.status` разделены; pending invite не даёт доступа.

## Открытые решения

- UI framework.
- Production deployment platform.
- Retention/index lifecycle/secrets для observability и данных.
- Notification provider — только Phase 2, MVP использует pull-model статусы.
