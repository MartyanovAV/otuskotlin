# TARGET DIRECTORY: docs/03-architecture/c4/
# TARGET FILENAME: C4_CONTAINER.md (single file, edit if exists)

# Диаграмма контейнеров C4 - уровень 2

## Система: {{system_name}}

Диаграмма контейнеров описывает границы приложения, внешние системы и платформенные контуры.

```mermaid
C4Container
  title Диаграмма контейнеров - {{system_name}}

  Person(user, "Пользователь", "Основной пользователь системы")
  System_Ext(externalSystem, "Внешняя система", "Система за пределами application boundary")

  System_Boundary(systemBoundary, "{{system_name}} граница приложения") {
    Container(webUi, "Web UI", "{{ui_technology}}", "Пользовательский интерфейс")
    Container(api, "Backend API", "{{api_technology}}", "Бизнес-операции и доменная авторизация")
    ContainerDb(appDb, "Application DB", "{{db_technology}}", "Прикладные данные системы")
  }

  Boundary(platformBoundary, "Граница платформы / observability", "Platform") {
    Container_Ext(logAgent, "Log Agent", "{{log_agent_technology}}", "Доставка структурированных логов")
    Container_Ext(logStore, "Log Store", "{{log_store_technology}}", "Хранилище технических логов вне приложения")
  }

  Rel(user, webUi, "Использует", "HTTPS")
  Rel(webUi, api, "Вызывает API", "HTTPS/JSON")
  Rel(api, appDb, "Читает и записывает", "DB protocol")
  Rel(api, externalSystem, "Интегрируется", "Protocol")
  Rel(api, logAgent, "Пишет структурированные логи", "stdout/fluent logging")
  Rel(logAgent, logStore, "Доставляет логи", "HTTP bulk")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

{{additional_containers}}
