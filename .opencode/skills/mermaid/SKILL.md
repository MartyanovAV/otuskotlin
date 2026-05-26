---
name: mermaid
description: Standard Mermaid diagram templates for architecture documentation — use flowchart for component relationships, sequence for request flows, C4 for system context, deployment for infrastructure
---

# Mermaid Diagrams

Стандартные диаграммы для архитектурной документации.

## Выбор типа диаграммы

| Ситуация                              | Тип диаграммы     |
|---------------------------------------|-------------------|
| Компоненты и связи между ними         | flowchart         |
| Последовательность запросов/операций  | sequenceDiagram   |
| Система в контексте внешнего мира     | C4Context         |
| Инфраструктура и деплой               | flowchart (subgraph) |
| Слои архитектуры (API, Core, Storage) | flowchart (subgraph) / C4Component |

## Flowchart — компоненты и связи

```mermaid
flowchart LR
    A[Client] --> B[API Gateway]
    B --> C[Service]
    C --> D[(Database)]
```

## Sequence — последовательность операций

```mermaid
sequenceDiagram
    Client->>API: POST /order
    API->>Service: Process
    Service->>DB: Save
    DB-->>Service: OK
    Service-->>API: 201 Created
    API-->>Client: Response
```

## C4 Context — система в целом

```mermaid
C4Context
    Person(user, "Пользователь")
    System(app, "Приложение")
    SystemDb(db, "База данных")
    
    Rel(user, app, "Использует")
    Rel(app, db, "Хранит данные")
```

## Deployment — инфраструктура

Для отображения инфраструктуры используйте `flowchart` с `subgraph` для группировки по узлам/контейнерам:

```mermaid
flowchart LR
    subgraph Docker Network
        subgraph api-node["API Node"]
            api["API Service"]
        end
        subgraph worker-node["Worker Node"]
            worker["Worker"]
        end
        subgraph db-node["Database Node"]
            db[(PostgreSQL)]
        end
    end

    api --> worker
    worker --> db
```

## Architecture — компоненты

Для отображения слоёв архитектуры используйте `flowchart` с `subgraph` или `C4Component`:

```mermaid
flowchart TB
    subgraph API["API Layer"]
        apiService["API Service"]
        gateway["Gateway"]
    end

    subgraph Core["Core"]
        business["Business Logic"]
        repository["Repository"]
    end

    subgraph Storage["Storage"]
        db[(Database)]
    end

    apiService --> business
    gateway --> apiService
    business --> repository
    repository --> db
```

## Чеклист

- [ ] Все связи подписаны
- [ ] Нет пересечений линий
- [ ] Компоненты одного уровня на одном уровне
- [ ] Цвета однотипных компонентов одинаковые
