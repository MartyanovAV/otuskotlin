# FitBridge

**FitBridge** - вертикальная CRM-платформа для независимых фитнес-тренеров, онлайн-коучей и будущих малых фитнес-команд.

Утверждённый MVP-кандидат проекта — trainer-first MVP (Дневник тренера): тренер как единственный зарегистрированный пользователь создаёт клиентскую карточку и тренировочный план. Будущая client-owned модель остаётся стратегическим направлением после проверки trainer-first B2B-ценности.

## Целевая аудитория

### Независимый тренер

Ведет 15-25 клиентов в гибридном формате: часть работы проходит офлайн, часть - в мессенджерах и таблицах. Ему важно сократить ручную админку, выглядеть профессиональнее и удерживать клиента за счет видимого прогресса.

### Онлайн-коуч

Работает удаленно с 35-50 клиентами. Его главные боли - большое количество check-in, переключение между сервисами и отсутствие единой картины по adherence, программам и статусам клиентов.

### Клиент

В текущем MVP клиентский контур полностью исключен. В будущей модели клиент сможет получить собственный профиль, историю и управление доступами, а также получать тренировочные планы по ссылке.

### Владелец микро-студии

Управляет небольшой командой специалистов и хочет стандартизировать клиентский сервис без тяжелой club-CRM. Для него важны роли, общая клиентская база и прозрачный контроль качества.

Подробнее: [docs/01-business/CUSTOMER_PERSONAS.md](docs/01-business/CUSTOMER_PERSONAS.md).

## MVP

MVP фокусируется на одном сокращённом критическом пути пилотного релиза:

**Trainer-first сценарий (Дневник тренера)**
1. Тренер регистрируется и открывает минимальный рабочий контур.
2. Тренер создаёт минимальную клиентскую карточку.
3. Тренер создаёт простой тренировочный план.

В MVP **не входят**: регистрация клиента, клиентский кабинет, публичные ссылки на план, отметки выполнения, `AccessGrant`, полноценная модель прав, solo-client PLG, multi-specialist сценарий, командный тариф студии, шаблоны программ, биллинг, отчётный модуль, замеры, AI-генерация программ, чат, фото/видео, медданные, выделенные API уведомлений (Notification) и аудита (AuditEvent).

Подробнее: [docs/01-business/MVP_SCOPE_SUMMARY.md](docs/01-business/MVP_SCOPE_SUMMARY.md).

## Целевой пользовательский опыт MVP

Ключевые продуктовые поверхности MVP:

- карточка клиента для тренера;
- создание простого плана;
- поиск и список клиентов;
- поиск и список планов.

Подробнее о визуальном стиле, цветах и компонентах интерфейса: [docs/04-ui-ux/DESIGN_STYLE_GUIDE.md](docs/04-ui-ux/DESIGN_STYLE_GUIDE.md).

## Продуктовые сущности MVP

Канонический business scope trainer-only MVP фиксирует минимальные сущности без выбора будущих client-owned и access-механизмов:

| Сущность | Назначение в MVP | Статус |
|---|---|---|
| `Trainer` | Единственный зарегистрированный пользователь MVP | Входит |
| `ClientCard` | Минимальная карточка клиента, создаётся тренером | Входит |
| `TrainingPlan` | Простой тренировочный план, создаётся тренером | Входит |
| `ClientProfile` | Полноценный клиентский профиль и история | Phase 2 |
| `AccessGrant` | Полноценная модель прав и подтверждений доступа | Phase 2 |
| `Subscription` | Тарифы и биллинг | Phase 2 |
| `Notification` / `AuditEvent` | Отдельные продуктовые контуры уведомлений и аудита | Phase 2 |

Функциональные требования описаны в [docs/02-analysis/01-functional-requirements.md](docs/02-analysis/01-functional-requirements.md), нефункциональные - в [docs/02-analysis/02-nonfunctional-requirements.md](docs/02-analysis/02-nonfunctional-requirements.md). Матрица трассировки BR → MVP → FR/NFR → API → AC → тесты находится в [docs/02-analysis/REQUIREMENTS_TRACEABILITY_MATRIX.md](docs/02-analysis/REQUIREMENTS_TRACEABILITY_MATRIX.md), а классификация данных, согласия, доступы, логирование и заметки по хранению/удалению — в [docs/02-analysis/DATA_CLASSIFICATION_MATRIX.md](docs/02-analysis/DATA_CLASSIFICATION_MATRIX.md).

## Архитектурное видение

Архитектура описана в обзорном документе и C4-диаграммах Draw.io:

- [Обзор архитектуры](docs/03-architecture/03-arch.md);
- [C4 Context source](docs/03-architecture/c4/C4_CONTEXT.drawio);
- [C4 Container source](docs/03-architecture/c4/C4_CONTAINER.drawio);
- [C4 Component source](docs/03-architecture/c4/C4_COMPONENT.drawio);
- [ERD MVP](docs/03-architecture/ERD.md);
- [Security Architecture / Threat Model](docs/03-architecture/SECURITY_ARCHITECTURE.md).

Индекс архитектурной документации: [docs/03-architecture/03-arch.md](docs/03-architecture/03-arch.md).

### Архитектурные решения

- Kotlin как основной язык backend-кода.
- Ktor как backend framework.
- Keycloak как внешний Identity Server.
- POST Full API для бизнес-операций.
- PostgreSQL как основное хранилище приложения.
- GreptimeDB для мониторинга и анализа логов и метрик через встроенный GreptimeDB Dashboard.
- Fluent Bit для доставки логов приложения в GreptimeDB.

ADR:

- [ADR-001: использовать Keycloak](docs/03-architecture/ADR/ADR-001-use-keycloak.md)
- [ADR-002: использовать POST Full API](docs/03-architecture/ADR/ADR-002-post-full-api.md)
- [ADR-003: использовать Ktor](docs/03-architecture/ADR/ADR-003-ktor.md)
- [ADR-004: использовать Kotlin](docs/03-architecture/ADR/ADR-004-kotlin.md)
- [ADR-005: использовать PostgreSQL](docs/03-architecture/ADR/ADR-005-use-postgresql.md)
- [ADR-006: использовать GreptimeDB и Fluent Bit для observability MVP](docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md)

## Целевая инфраструктура MVP

Целевая инфраструктура MVP описана в архитектурной документации и включает:

- планируемый Web UI приватного кабинета тренера; клиентский и in-product support UI относятся к Phase 2;
- Envoy Gateway как входной proxy и boundary для проверки JWT;
- FitBridge Backend API на Kotlin/Ktor для POST Full бизнес-операций;
- PostgreSQL как прикладное хранилище;
- Keycloak как внешний Identity Server;
- Fluent Bit и GreptimeDB для observability.

Подробности контейнеров и границ ответственности зафиксированы в [C4 Container source](docs/03-architecture/c4/C4_CONTAINER.drawio), [C4 Component source](docs/03-architecture/c4/C4_COMPONENT.drawio) и [Security Architecture / Threat Model](docs/03-architecture/SECURITY_ARCHITECTURE.md).

### Транспортные модели, API, Спецификации

1. `profile-service/specs`, `training-service/specs` - описание API в форме OpenAPI-спецификаций
2. `profile-service/api-v1-jackson`, `training-service/api-v1-jackson` - Генерация первой версии транспортных моделей (Jackson)
3. `profile-service/api-v2-kmp`, `training-service/api-v2-kmp` - Генерация второй версии транспортных моделей (KMP)
4. `profile-service/swagger`, `training-service/swagger` - Сборка Docker-образов Swagger UI для визуализации API

