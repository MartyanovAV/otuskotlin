# C4 Container - FitBridge

Диаграмма показывает целевой контейнерный состав MVP FitBridge и явно разделяет application boundary, platform/observability boundary и операционное сопровождение пилота. OpenSearch используется FitBridge для логов, но не является частью приложения FitBridge. In-product `ADMIN`/support UI и support domain API не входят в MVP.

```mermaid
C4Container
  title Диаграмма контейнеров - FitBridge MVP

  Person(client, "Клиент", "Mobile-first пользователь")
  Person(trainer, "Тренер", "Desktop-first пользователь")
  Person_Ext(supportOps, "Оператор поддержки пилота", "Вне product UI/domain API; controlled operations без чтения чувствительных клиентских данных")

  System_Boundary(fitbridge, "FitBridge application boundary / граница приложения") {
    Container(webUi, "Web UI", "UI framework TBD", "Клиентский и тренерский интерфейс")
    Container(envoy, "Envoy Gateway", "Envoy", "Входной proxy, маршрутизация и JWT validation для всех MVP /v1/* endpoints")
    Container(api, "FitBridge Backend API", "Kotlin/JVM, Ktor", "POST Full API, backend JWT/user-context validation и доменная авторизация")
    ContainerDb(appDb, "Application DB", "PostgreSQL", "Профили, доступы, дневник, программы, назначения")
  }

  Boundary(observability, "Platform / Observability boundary / граница платформы", "Platform") {
    Container_Ext(logAgent, "Fluent Bit", "Fluent Bit", "Сбор и доставка логов контейнеров вне application boundary")
    Container_Ext(logStore, "OpenSearch", "OpenSearch", "Внешнее хранилище технических логов и audit-oriented событий")
    Container_Ext(dashboards, "OpenSearch Dashboards", "OpenSearch Dashboards", "Просмотр логов и диагностика")
  }

  System_Ext(keycloak, "Keycloak", "OIDC/OAuth2 Identity Server")
  System_Ext(runbook, "Controlled operational runbook", "Provisioning/block/revoke/cancel invite procedures для пилота")
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

## Целевые обязанности контейнеров

| Container | Целевая ответственность MVP |
|---|---|
| Web UI | Пользовательский интерфейс клиента и тренера; in-product ADMIN/support UI в MVP отсутствует |
| Envoy Gateway | Маршрутизация всех MVP `/v1/*` endpoints и обязательная JWT validation на edge/proxy layer |
| FitBridge Backend API | Ktor backend с Profile, Access, Diary, Program, Progress, Auth и Audit Logging capability; независимо проверяет JWT/user context и owner/grant/scope policy |
| Application DB | PostgreSQL storage для доменных данных MVP |
| Keycloak | Identity provider для пользователей, ролей и JWT |
| Controlled operational runbook | Внешний процесс пилота для provisioning/block/revoke/cancel invite действий без доступа к domain API и sensitive payload |
| Fluent Bit / OpenSearch / Dashboards | Platform/observability-контур вне application boundary; принимает только masked structured logs |

## Открытые решения

- UI framework.
- API contract format beyond markdown: OpenAPI/JSON Schema/error model.
- Production deployment platform.
