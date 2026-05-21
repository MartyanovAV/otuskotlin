# TARGET DIRECTORY: docs/03-architecture/c4/
# TARGET FILENAME: C4_COMPONENT.md (single file, edit if exists)

# Диаграмма компонентов C4 - уровень 3

## Контейнер: {{container_name}}

Диаграмма компонентов описывает внутренние элементы выбранного контейнера и их зависимости.

```mermaid
C4Component
  title Диаграмма компонентов - {{container_name}}

  Container(caller, "Внешний вызывающий контейнер", "{{caller_technology}}", "Передаёт запросы в {{container_name}}")
  ContainerDb(appDb, "Application DB", "{{db_technology}}", "Прикладные данные")

  Container_Boundary(containerBoundary, "{{container_name}}") {
    Component(router, "{{component1}}", "{{component1_technology}}", "Принимает и маршрутизирует запросы")
    Component(service, "{{component2}}", "{{component2_technology}}", "Выполняет бизнес-логику")
    Component(repository, "{{component3}}", "{{component3_technology}}", "Инкапсулирует доступ к данным")
  }

  Rel(caller, router, "Вызывает", "HTTPS/JSON")
  Rel(router, service, "Передаёт команду")
  Rel(service, repository, "Читает и сохраняет данные")
  Rel(repository, appDb, "Читает/пишет", "DB protocol")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

{{interactions}}
