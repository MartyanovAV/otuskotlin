# Карта проекта FitBridge

## Назначение

Этот файл — краткая карта фактической структуры репозитория. Детали бизнес-требований,
API и архитектурных решений находятся в `docs/`; не дублируй их здесь.

## Корневая структура

```text
otuskotlin/
├── build.gradle.kts            # Агрегирующие buildInfra/buildImages/e2eTests
├── settings.gradle.kts         # Composite builds
├── build-plugin/               # Общие Gradle convention plugins
├── gradle/                     # Version catalog и wrapper
├── fit-bridge-be/              # Backend composite build
│   ├── training-service/       # Клиенты, планы и тренировки
│   └── fit-bridge-e2e-be/      # Backend E2E
├── fit-bridge-libs/            # Общие библиотеки COR и logging
├── fit-bridge-other/           # Инфраструктурные Gradle-проекты
│   └── fit-bridge-dcompose/    # Переиспользуемый Compose resource для E2E
├── fit-bridge-tests/           # Дополнительная тестовая инфраструктура
├── deploy/                     # Deployment assets
├── docs/                       # Бизнес, анализ, архитектура и UI/UX
└── ux-prototype/               # Временный статический HTML/CSS прототип
```

В проекте сейчас нет `entities/` и нет вложенных entity-level `.opencode`.
Не трать время на их поиск и не выдумывай отсутствующий контекст.

## Структура backend-сервисов

`training-service` — самостоятельный Gradle composite build. Корневой каркас
composite builds сохранён и допускает добавление новых сервисов. Типичные модули сервиса:

| Модуль | Ответственность |
|---|---|
| `common` | Доменные модели, контекст и общие контракты |
| `biz` | Application/business logic и COR-процессоры |
| `api-v1-jackson`, `api-v2-kmp` | API DTO и сериализация |
| `mappers-*` | Маппинг между API и domain |
| `specs` | Спецификации и контракты |
| `stubs` | Stub-данные и сценарии |
| `swagger` | OpenAPI/Swagger артефакты |
| `app-ktor` | Runtime, DI, transport и точки входа Ktor |

Репозиторные реализации добавляй внутрь конкретного сервиса и предметной области,
а не как общий repository на уровне всего проекта.

## Канонические источники контекста

1. Бизнес-цель и acceptance criteria: `docs/01-business/`
2. Функциональные и нефункциональные требования: `docs/02-analysis/`
3. Архитектура, ADR, ERD и API: `docs/03-architecture/`
4. Реальное поведение: существующий код и тесты соответствующего сервиса
5. Технические процессы: `.opencode/skills/`

## Правила для AI

Перед изменением кода или архитектуры:

1. Определи область: `training-service`, будущий отдельный сервис, общая библиотека или infrastructure.
2. Определи слой/модуль: `common`, `biz`, `api-*`, `mappers-*`, `app-ktor`, tests или deploy.
3. Прочитай ближайший фактический код и тесты этого модуля.
4. Подключи только релевантный skill из корневой `.opencode/skills/`.
5. При изменении поведения найди связанный BR/API/ADR в `docs/`.
6. Не создавай новые архитектурные слои и каталоги только потому, что они встречаются в шаблонах.

## Поддерживаемые варианты

- Runtime: Ktor; Spring/Kafka/RabbitMQ не считаются реализованными без фактического модуля.
- API: `api-v1-jackson`, `api-v2-kmp`.
- Хранилища: ориентируйся только на реально существующие модули и утверждённые ADR.
- Frontend: production frontend пока отсутствует; `ux-prototype/` — только статический прототип.
