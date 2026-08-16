# План микросервисных контуров FitBridge

Документ сохраняет исходную расширяемую структуру сервисов и описывает выбранную после ADR-007 минимальную runtime-архитектуру Trainer Diary.

## Цель

Сохранить критический путь `trainer signup → ClientCard → TrainingPlan → search/reuse` без отдельного сервиса профилей и локальной проекции пользователя.

## Runtime-контуры

| Контур                  | Ответственность                                                                 | Не делает                                                        |
|-------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------|
| Keycloak                | Регистрация, username, credentials, identity profile, роль `TRAINER`, OIDC/JWT  | Не хранит карточки и планы, не решает domain ownership           |
| Envoy Gateway           | Маршрутизация, edge JWT validation, проверка issuer/audience                    | Не заменяет backend ownership checks                             |
| Training Service        | POST Full API, Auth Guard, `ClientCard`, `TrainingPlan`, ownership, persistence | Не хранит credentials и не вызывает UserInfo для каждой операции |
| PostgreSQL              | Карточки, планы, locks, archive state                                           | Не содержит локального пользователя или профиля тренера          |
| Fluent Bit / GreptimeDB | Доставка и поиск masked logs                                                    | Не получает JWT, ФИО, заметки или содержимое планов              |

## Auth context

```kotlin
AuthPrincipal(
    userId = jwt.sub,
    username = jwt.preferredUsername,
    roles = jwt.realmAccess.roles,
)
```

Для приватного запроса обязательны валидные `iss`, `aud`, `exp`, `nbf`, `sub` и роль `TRAINER`. `ownerId` доменной сущности всегда равен `principal.userId`.

## Структура backend

Проектный манифест фиксирует правило: **сервис = сборщик сущностей**, **сущность = common/biz/api/repo-*/app**. Репозитории принадлежат сущности, а `app-*` являются точками входа. Отказ от отдельного Profile Service не меняет это правило и не является основанием для объединения доменных модулей `ClientCard` и `TrainingPlan` на уровне Training Service.

```text
fit-bridge-be/
├── build-plugin/
├── platform-contracts/            # Только технические shared contracts: errors, pagination, requestId
└── training-service/
    ├── app-ktor/                   # Точка входа Training Service, JWT validation, health и config
    ├── specs/                      # specs-training-v1.yaml, specs-training-v2.yaml
    ├── swagger/
    └── entities/
        ├── client-card/
        │   ├── common/
        │   ├── biz/
        │   ├── api/
        │   ├── repo-pgjvm/
        │   ├── repo-inmemory/
        │   └── app/
        └── training-plan/
            ├── common/
            ├── biz/
            ├── api/
            ├── repo-pgjvm/
            ├── repo-inmemory/
            └── app/
```

Допустимые shared-модули должны быть техническими и стабильными. В `platform-contracts` нельзя выносить `ClientCard`, `TrainingPlan`, их бизнес-правила, repository ports/implementations или domain mappers. Общего repo-layer на уровне `training-service` быть не должно.

Текущие плоские модули `common`, `api-*` и `mappers-*` являются переходным состоянием: они компилируются, но ещё не приведены к целевым entity boundaries. Runtime-модуль `app-ktor`, `biz` и `repo-*` также ещё не входят в tracked sources. Dockerfile ориентирован на целевое имя `app-ktor`, а сборку образа следует включать в CI после добавления runtime-модуля.

## Последовательность дальнейшей реализации backend

1. Разнести текущие common-модели, контексты и entity-specific мапперы по `entities/client-card` и `entities/training-plan`; оставить в `platform-contracts` только технические контракты.
2. Добавить для каждой сущности собственные `biz`, `api`, `repo-pgjvm`, `repo-inmemory` и `app`, не создавая общий repository-layer сервиса.
3. Добавить `app-ktor` как точку сборки сущностей и Ktor JWT authentication с проверкой issuer, audience и времени жизни.
4. Преобразовать claims в `AuthPrincipal`, положить principal в domain context и реализовать authorization chain: authenticated → `TRAINER` → owner guard.
5. Реализовать начальную PostgreSQL schema; миграции и repository implementations хранить внутри соответствующих сущностей `ClientCard` и `TrainingPlan`.
6. Добавить негативные тесты no-token, wrong-role и cross-owner.
7. Включить сборку backend image и E2E в CI.

## Phase 2

Клиентская регистрация, роль `CLIENT`, расширенный профиль тренера, немедленный централизованный revoke и multi-issuer identity добавляются только отдельными решениями. Они могут оформляться отдельными сервисами в сохранённом каркасе, но не должны возвращать скрытую runtime-зависимость Training Service от identity profile API.
