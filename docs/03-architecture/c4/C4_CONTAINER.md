# C4 Container - FitBridge

Диаграмма показывает целевой контейнерный состав MVP и текущий transition-state. В репозитории уже есть локальный стенд `deploy/docker-compose.yml` со статическим Nginx-прототипом, Envoy, Keycloak, OpenSearch, Dashboards и Fluent Bit. Ktor backend и прикладное хранилище пока являются целевой частью архитектуры и требуют реализации.

```mermaid
C4Container
  title Container Diagram - FitBridge MVP

  Person(client, "Клиент", "Mobile-first пользователь")
  Person(trainer, "Тренер", "Desktop-first пользователь")
  Person(admin, "Администратор / Support", "Поддержка и эксплуатация")

  System_Boundary(fitbridge, "FitBridge") {
    Container(webUi, "Web UI", "HTML prototype now; UI framework TBD", "Клиентский и тренерский интерфейс")
    Container(envoy, "Envoy Gateway", "Envoy", "Входной proxy, маршрутизация и JWT-проверка для /v1/*")
    Container(api, "FitBridge Backend API", "Kotlin/JVM, Ktor", "POST Full API, бизнес-операции MVP и доменная авторизация")
    ContainerDb(appDb, "Application DB", "PostgreSQL", "Профили, доступы, дневник, программы, назначения")
    Container(logAgent, "Fluent Bit", "Fluent Bit", "Сбор и доставка логов контейнеров")
    ContainerDb(logStore, "OpenSearch", "OpenSearch", "Хранилище технических логов и audit-oriented событий")
    Container(dashboards, "OpenSearch Dashboards", "OpenSearch Dashboards", "Просмотр логов и диагностика")
  }

  System_Ext(keycloak, "Keycloak", "OIDC/OAuth2 Identity Server")
  System_Ext(manualBilling, "Manual billing validation", "Внешний ручной процесс проверки willingness-to-pay")

  Rel(client, webUi, "Использует", "HTTPS")
  Rel(trainer, webUi, "Использует", "HTTPS")
  Rel(admin, dashboards, "Смотрит логи", "HTTPS")

  Rel(webUi, envoy, "Вызывает API и открывает страницы", "HTTPS")
  Rel(envoy, api, "Маршрутизирует POST Full API", "HTTP/JSON")
  Rel(envoy, keycloak, "Проверяет JWT/JWKS и проксирует realm/admin endpoints", "OIDC/JWKS")
  Rel(api, keycloak, "Использует subject, roles и claims", "JWT/OIDC")
  Rel(api, appDb, "Читает и сохраняет доменные данные", "DB protocol TBD")
  Rel(api, logAgent, "Пишет структурированные логи", "stdout/fluent logging")
  Rel(logAgent, logStore, "Доставляет логи", "HTTP bulk")
  Rel(dashboards, logStore, "Читает индексы логов", "HTTPS")
  Rel(trainer, manualBilling, "Проходит qualitative price validation", "Interview/manual")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Container Status

| Container | Current state | Target MVP state |
|---|---|---|
| Web UI | Static HTML prototype under `deploy/volumes/nginx/html` | Product UI, framework TBD |
| Envoy Gateway | Present in `deploy/volumes/envoy/envoy.yaml` | Routes all MVP `/v1/*` POST Full endpoints and validates JWT |
| FitBridge Backend API | Placeholder `fit-bridge-be-tmp` prints `Hello` | Ktor backend with Profile, Access, Diary, Program and Progress capabilities |
| Application DB | Target-state | PostgreSQL storage for MVP domain data |
| Keycloak | Present in local compose with imported realm | Identity provider for users, roles and JWT |
| Fluent Bit / OpenSearch / Dashboards | Present in local compose | Observability and infrastructure audit-oriented logging |

## Open Decisions

- UI framework.
- API contract format beyond markdown: OpenAPI/JSON Schema/error model.
- Production deployment platform.
