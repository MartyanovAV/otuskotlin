# Обзор архитектуры FitBridge

Документ является краткой картой архитектуры trainer-first MVP с публичной ссылкой на тренировочный план. Детали не дублируются: источником истины являются документы из таблицы ниже.

## Канонические источники

| Область | Источник |
|---|---|
| Business scope MVP | [BR-010 Trainer-first MVP с публичной ссылкой](../01-business/BR/BR-010-public-plan-link-mvp.md), [MVP_SCOPE_SUMMARY](../01-business/MVP_SCOPE_SUMMARY.md), [PRODUCT_ROADMAP](../01-business/PRODUCT_ROADMAP.md) |
| Архитектурные решения | [ADR index](./01-adrs.md) и файлы каталога `ADR/` |
| ADR публичного доступа к плану | [ADR-007](./ADR/ADR-007-public-plan-link-mvp.md) |
| C4-диаграммы | Исходники Draw.io: [Context](./c4/C4_CONTEXT.drawio), [Container](./c4/C4_CONTAINER.drawio), [Component](./c4/C4_COMPONENT.drawio); preview SVG перечислены ниже |
| Модель данных | [ERD.md](./ERD.md) |
| Security/access/privacy | [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md) |
| API index | [02-api.md](./02-api.md) |
| API entities/contracts/rules/limits | Файлы каталога `api/`, перечислены в [02-api.md](./02-api.md) |
| OpenAPI спецификации | [specs-fitbridge-v1.yaml](../../fit-bridge-other/fit-bridge-specs/specs/specs-fitbridge-v1.yaml), [specs-fitbridge-v2.yaml](../../fit-bridge-other/fit-bridge-specs/specs/specs-fitbridge-v2.yaml) |
| Deployment/run guide | [../../deploy/README.md](../../deploy/README.md) |

## Карта MVP

FitBridge — trainer-first SaaS для независимого тренера. В Gate 1 проверяется один критический путь с публичной ссылкой на тренировочный план без регистрации клиента и без полноценной client-owned модели.

| Путь | Кратко | Канонический контракт |
|---|---|---|
| Trainer-first сценарий с публичной ссылкой | Тренер регистрируется, создаёт `ClientCard`, создаёт `TrainingPlan`, получает публичную ссылку, клиент без регистрации открывает план и оставляет `CompletionMark`, тренер видит статус и может закрыть ссылку | [api/01](./api/01-scope.md), [api/03](./api/03-mvp-profile-access-methods.md), [api/04](./api/04-mvp-diary-plan-methods.md) |

### Доменные концепты MVP

| Концепт | Назначение | Статус в MVP |
|---|---|---|
| `ClientCard` | Минимальная карточка клиента, созданная и управляемая тренером | Product entity MVP |
| `TrainingPlan` | Простой тренировочный план, связанный с `ClientCard`; содержит technical public-access state | Product entity MVP |
| `CompletionMark` | Минимальная отметка выполнения по публичной ссылке | Дочерний объект/value object внутри `TrainingPlan` на MVP |
| `PublicLink` | Публичный доступ по capability-token к конкретному плану | Не product entity; техническая capability/public-access функция |

## Архитектурные принципы

- **Trainer-first scope:** единственный зарегистрированный пользователь MVP — тренер; клиент открывает публичную ссылку без регистрации и без личного кабинета.
- **Минимальная доменная модель:** MVP опирается на `ClientCard`, `TrainingPlan`, `CompletionMark`; `CompletionMark` допустимо хранить как дочерний value object плана.
- **Capability-token вместо `AccessGrant`:** публичная ссылка — технический доступ к конкретному `TrainingPlan`, а не самостоятельная продуктовая сущность и не полноценная модель прав.
- **Public endpoint by token only:** публичные методы получают только raw token; `clientId`, `planId` и другие прямые идентификаторы в публичном URL/API не передаются.
- **Token safety:** raw token не хранится и не логируется; в БД хранится только hash, обязательны TTL, revoke/close, rate limiting и минимальный публичный payload.
- **Deny by default для приватного API:** все тренерские `/v1/*` и `/v2/*` методы проходят JWT validation и ownership-проверку тренера над `ClientCard`/`TrainingPlan`.
- **Per-entity repository rule:** repository ports/implementations находятся внутри соответствующей сущности, общего repo-layer на уровне сервиса нет.
- **Evolution-ready:** модель не блокирует будущий переход к `ClientProfile`, `Invite`, `AccessGrant` и client-owned истории после проверки метрик публичной ссылки.

## Технологический контур

| Область | Решение | ADR/источник |
|---|---|---|
| Backend | Kotlin + Ktor | [ADR-004](./ADR/ADR-004-kotlin.md), [ADR-003](./ADR/ADR-003-ktor.md) |
| Identity | Keycloak/OIDC/JWT для тренера; публичный клиентский доступ без JWT по capability-token | [ADR-001](./ADR/ADR-001-use-keycloak.md), [ADR-007](./ADR/ADR-007-public-plan-link-mvp.md), [SECURITY](./SECURITY_ARCHITECTURE.md) |
| API | POST Full, HTTPS/JSON; приватный `/v1/*` + `/v2/*` и публичный token-only контур `/public/v1/*` + `/public/v2/*` | [ADR-002](./ADR/ADR-002-post-full-api.md), [02-api](./02-api.md), OpenAPI specs |
| Data | PostgreSQL; hash публичного token внутри `TrainingPlan`/public-access state | [ADR-005](./ADR/ADR-005-use-postgresql.md), [ERD](./ERD.md) |
| Observability | OpenSearch + Dashboards + Fluent Bit как внешний/platform-контур, masked logs без raw token/payload | [ADR-006](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md) |

## Высокоуровневая C4 / компонентная логика

### C4 Context

- `Trainer` — единственный зарегистрированный пользователь; работает через Web UI и Keycloak.
- `Client by public link` — незарегистрированный пользователь; открывает публичную страницу по ссылке и оставляет отметку выполнения.
- `FitBridge` — система в фокусе: хранит карточки, планы, completion marks и public-access state.
- `Keycloak` — внешний Identity Server только для тренерского входа в MVP.
- `OpenSearch Platform` — внешний observability-контур, получает только masked structured logs.

### C4 Container

- `Web UI` содержит две поверхности: приватный кабинет тренера и публичная страница плана.
- `Envoy Gateway` разделяет приватные `/v1/*` и `/v2/*` контуры с JWT validation и публичный token-only контур с rate limiting.
- `FitBridge Backend API` реализует POST Full handlers, ownership checks, public token validation, lifecycle закрытия ссылки и запись `CompletionMark`.
- `Application DB` хранит `FitBridgeUser/TrainerProfile`, `ClientCard`, `TrainingPlan` и `CompletionMark`/value-object данные.

### C4 Component

- `Trainer Auth Guard` валидирует JWT тренера и ownership ресурсов.
- `Public Link Guard` принимает raw token, вычисляет hash, проверяет TTL/revoked/status/rate limit и не раскрывает внутренние id.
- `ClientCard Component` управляет карточками внутри границы тренера.
- `TrainingPlan Component` управляет планом, public-access state и закрытием ссылки.
- `Completion Component` записывает отметку выполнения как дочерний объект плана и обновляет статус для тренера.
- `Audit Logging` пишет только masked события без raw token, ФИО, содержимого плана и чувствительных payload.

## C4-диаграммы

Раздел содержит обзорные C4-представления. Они не переопределяют канонические документы выше: при расхождениях приоритет имеют ADR, API index, ERD и Security Architecture.

`.drawio` файлы являются каноническим редактируемым источником C4-диаграмм. `.svg` файлы — производные preview/export для чтения в Markdown и должны регенерироваться из соответствующего `.drawio` после правок диаграмм.

| Уровень | Назначение | Редактируемый источник | Preview/export |
|---|---|---|---|
| C4 Context | Границы FitBridge, тренер, клиент по ссылке и внешние системы | [C4_CONTEXT.drawio](./c4/C4_CONTEXT.drawio) | [SVG](./c4/C4_CONTEXT.drawio.svg) |
| C4 Container | Контейнеры trainer-first MVP с публичной ссылкой и внешние инфраструктурные зависимости | [C4_CONTAINER.drawio](./c4/C4_CONTAINER.drawio) | [SVG](./c4/C4_CONTAINER.drawio.svg) |
| C4 Component | Ключевые компоненты `FitBridge Backend API` для public-link и trainer-private контуров | [C4_COMPONENT.drawio](./c4/C4_COMPONENT.drawio) | [SVG](./c4/C4_COMPONENT.drawio.svg) |

### C4 Context

![C4 Context — FitBridge trainer-first MVP с публичной ссылкой](./c4/C4_CONTEXT.drawio.svg)

### C4 Container

![C4 Container — FitBridge trainer-first MVP с публичной ссылкой](./c4/C4_CONTAINER.drawio.svg)

### C4 Component

![C4 Component — FitBridge Backend API публичной ссылки](./c4/C4_COMPONENT.drawio.svg)

## Lifecycle публичной ссылки

1. Тренер создаёт `ClientCard`.
2. Тренер создаёт `TrainingPlan`, связанный с карточкой.
3. Backend генерирует cryptographically strong raw token, возвращает его тренеру один раз как public URL и сохраняет только `tokenHash`, `publicAccessStatus=ACTIVE`, `publicAccessExpiresAt`.
4. Клиент открывает публичный endpoint только с token; backend ищет план по hash, проверяет TTL/status/revoked/rate limit и возвращает минимальный payload плана.
5. Клиент оставляет `CompletionMark`; backend повторно валидирует token, записывает отметку и обновляет статус выполнения для тренера.
6. Тренер закрывает ссылку; backend переводит public-access state в `REVOKED`, после чего публичный endpoint не возвращает план и не принимает новые отметки.

## Решения MVP, которые не переопределяются в обзорных документах

- Нет регистрации клиента, клиентского кабинета, client-owned `ClientProfile`, `Invite` и `AccessGrant` в MVP.
- `PublicLink` не является самостоятельной продуктовой сущностью; это technical capability-token/public access к конкретному `TrainingPlan`.
- Публичная ссылка не должна раскрывать `clientId`, `planId`, внутренние id и sensitive payload.
- Нет in-product `ADMIN`/support роли, support console и broad bypass.
- OpenSearch остаётся external/platform observability, не частью application boundary; raw token и содержимое планов/отметок в логи не пишутся.

## Путь эволюции

| Будущий концепт | Как подготовлен MVP | Когда включать |
|---|---|---|
| `ClientProfile` | `ClientCard` содержит минимум полей и может быть мигрирован/связан с будущим профилем | После подтверждения метрик публичной ссылки и решения о клиентской регистрации |
| `Invite` | Public-link lifecycle отделён от product invite lifecycle | При появлении клиентского аккаунта и явного принятия доступа |
| `AccessGrant` | Ownership trainer→card не смешан с будущими client-owned правами | Phase 2 granular permissions/client-owned model |
| Client-owned history | `CompletionMark` хранится с датой/статусом и может стать источником миграции | После запуска клиентского кабинета и consent/deletion модели |

## Открытые решения

- UI framework.
- Production deployment platform.
- Конкретный TTL публичной ссылки для пилота и policy продления/ротации token.
- Retention/index lifecycle/secrets для observability и данных.
- Notification provider — только Phase 2, MVP использует pull-model статусы.
