# C4 Container / Контейнеры - FitBridge

Диаграмма показывает контейнеры MVP и границы ответственности. Политики безопасности, API-контракты и ERD описаны в канонических документах: [Security](../SECURITY_ARCHITECTURE.md), [API](../02-api.md), [ERD](../ERD.md).

```mermaid
C4Container
  title Диаграмма контейнеров - FitBridge MVP

  Person(client, "Клиент", "Mobile-first пользователь")
  Person(trainer, "Тренер", "Desktop-first пользователь")
  Person_Ext(supportOps, "Оператор поддержки пилота", "Вне product UI/domain API; controlled operations без чтения чувствительных клиентских данных")

  System_Boundary(fitbridge, "FitBridge application boundary / граница приложения") {
    Container(webUi, "Web UI", "UI framework TBD", "Клиентский и тренерский интерфейс")
    Container(envoy, "Envoy Gateway", "Envoy", "Входной proxy, маршрутизация и JWT validation для всех MVP /v1/* endpoints")
    Container(api, "FitBridge Backend API", "Kotlin/JVM, Ktor", "POST Full API, backend JWT/user-context validation, domain policy")
    ContainerDb(appDb, "Application DB", "PostgreSQL", "Профили, доступы, дневник, программы, назначения")
  }

  Boundary(observability, "Platform / Observability boundary", "Platform") {
    Container_Ext(logAgent, "Fluent Bit", "Fluent Bit", "Сбор и доставка логов контейнеров вне application boundary")
    Container_Ext(logStore, "OpenSearch", "OpenSearch", "Внешнее хранилище masked logs")
    Container_Ext(dashboards, "OpenSearch Dashboards", "OpenSearch Dashboards", "Просмотр логов и диагностика")
  }

  System_Ext(keycloak, "Keycloak", "OIDC/OAuth2 Identity Server")
  System_Ext(runbook, "Controlled operational runbook", "Процедуры пилота вне domain API")
  System_Ext(manualBilling, "Manual billing validation", "Внешний ручной процесс проверки willingness-to-pay")

  Rel(client, webUi, "Использует", "HTTPS")
  Rel(trainer, webUi, "Использует", "HTTPS")
  Rel(supportOps, dashboards, "Смотрит только masked technical/audit-oriented logs", "HTTPS")
  Rel(supportOps, keycloak, "Выполняет provisioning/block/revoke по runbook", "Keycloak admin console")
  Rel(supportOps, runbook, "Исполняет утверждённые операции пилота", "Manual controlled process")

  Rel(webUi, envoy, "Вызывает API и открывает страницы", "HTTPS")
  Rel(envoy, api, "Проксирует только запросы, прошедшие edge JWT validation", "HTTP/JSON")
  Rel(envoy, keycloak, "Проверяет JWT/JWKS для пользовательских MVP endpoints", "OIDC/JWKS")
  Rel(api, keycloak, "Независимо валидирует JWT/user context и использует subject, roles и claims", "JWT/OIDC")
  Rel(api, appDb, "Читает и сохраняет доменные данные", "DB protocol TBD")
  Rel(api, logAgent, "Пишет структурированные логи", "stdout/fluent logging")
  Rel(logAgent, logStore, "Доставляет логи", "HTTP bulk")
  Rel(dashboards, logStore, "Читает индексы логов", "HTTPS")
  Rel(trainer, manualBilling, "Проходит qualitative price validation", "Interview/manual")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Ответственность контейнеров

| Контейнер | Целевая ответственность MVP |
|---|---|
| Web UI | Пользовательский интерфейс клиента и тренера; ADMIN/support UI отсутствует |
| Envoy Gateway | Маршрутизация всех MVP `/v1/*` endpoints и обязательная JWT validation на edge/proxy layer |
| FitBridge Backend API | Ktor backend с Profile, Access, Diary, Program, Progress, Auth и Audit Logging; независимо проверяет JWT/user context и owner/grant/scope policy |
| Application DB | PostgreSQL storage для доменных данных MVP |
| Keycloak | Identity provider для пользователей, ролей и JWT |
| Controlled operational runbook | Внешний процесс пилота без доступа к domain API и sensitive payload |
| Fluent Bit / OpenSearch / Dashboards | Platform/observability-контур вне application boundary; принимает только masked structured logs |

## Канонические ссылки

| Тема | Источник |
|---|---|
| JWT, roles, support boundary, threat model | [SECURITY_ARCHITECTURE.md](../SECURITY_ARCHITECTURE.md) |
| Модель данных и ограничения | [ERD.md](../ERD.md) |
| API-поверхность и лимиты | [02-api.md](../02-api.md), [api/06](../api/06-metrics-and-limits.md) |
| Observability-стек | [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md) |

## Открытые решения

- UI framework.
- API contract format beyond markdown: OpenAPI/JSON Schema/error model.
- Production deployment platform.
