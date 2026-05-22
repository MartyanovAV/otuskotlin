# FitBridge

**FitBridge** - вертикальная CRM-платформа для независимых фитнес-тренеров, онлайн-коучей, малых фитнес-команд и их клиентов.

Проект заменяет разрозненные чаты, заметки и Google Sheets единой цифровой средой, где клиент владеет своей тренировочной историей, а тренер получает доступ к данным только после явного разрешения клиента.

## Целевая аудитория

### Независимый тренер

Ведет 15-25 клиентов в гибридном формате: часть работы проходит офлайн, часть - в мессенджерах и таблицах. Ему важно сократить ручную админку, выглядеть профессиональнее и удерживать клиента за счет видимого прогресса.

### Онлайн-коуч

Работает удаленно с 35-50 клиентами. Его главные боли - большое количество check-in, переключение между сервисами и отсутствие единой картины по adherence, программам и статусам клиентов.

### Клиент

Тренируется самостоятельно или с тренером, хочет видеть прогресс, не терять историю при смене специалиста и контролировать, кто имеет доступ к данным о тренировках, замерах и состоянии.

### Владелец микро-студии

Управляет небольшой командой специалистов и хочет стандартизировать клиентский сервис без тяжелой club-CRM. Для него важны роли, общая клиентская база и прозрачный контроль качества.

Подробнее: [docs/01-business/CUSTOMER_PERSONAS.md](docs/01-business/CUSTOMER_PERSONAS.md).

## MVP

MVP фокусируется на сокращённом критическом пути пилотного релиза, разделенном на два параллельных сценария:

**Путь А: Тренер-ориентированный (Trainer-led)**
1. Тренер регистрируется и открывает минимальный кабинет.
2. Тренер приглашает первого клиента по ссылке или коду.
3. Клиент принимает приглашение, создает профиль и явно выдает доступ.
4. Тренер создает простой индивидуальный план и назначает его.
5. Клиент ведет дневник и отмечает выполнение.
6. Тренер видит историю и статусы выполнения в карточке клиента.

**Путь Б: Клиент-ориентированный (PLG Solo-client)**
1. Клиент регистрируется самостоятельно.
2. Клиент сам создаёт себе программу тренировок (или добавляет разовые тренировки в дневник).
3. Клиент сам назначает программу себе и отмечает выполнение.
4. Позже клиент может пригласить тренера или передать доступ тренеру.

В MVP **не входят**: полноценный multi-specialist сценарий, командный тариф студии, шаблоны программ, биллинг, отчётный модуль, замеры, AI-генерация программ, выделенные API уведомлений (Notification) и аудита (AuditEvent).

Подробнее: [docs/01-business/MVP_SCOPE_SUMMARY.md](docs/01-business/MVP_SCOPE_SUMMARY.md).

## Целевой клиентский опыт MVP

Ключевые экраны MVP:

- клиентский дневник тренировок;
- экран текущих доступов клиента;
- карточка клиента для тренера;
- создание простого плана;
- история и статусы выполнения.

## Сущности и методы

Проект использует подход **POST Full API**: бизнес-операции выполняются через `POST` с JSON-запросом и JSON-ответом. Это упрощает авторизацию, аудит и валидацию команд.

| Сущность | Назначение | Основные методы MVP |
|---|---|---|
| `User` | Учетная запись в Keycloak, базовая идентификация пользователя | `registerClient`, `registerTrainer`, `login`, `logout`, `restoreAccess` |
| `ClientProfile` | Профиль клиента и принадлежащая ему тренировочная история | `createClientProfile`, `updateClientProfile`, `getClientProfile`, `requestProfileDeletion` |
| `TrainerProfile` | Профессиональный профиль тренера и минимальный кабинет | `createTrainerProfile`, `updateTrainerProfile`, `listClients` |
| `AccessGrant` | Разрешение одному активному тренеру работать с данными клиента | `createInvite`, `acceptInvite`, `grantAccess`, `revokeAccess`, `getCurrentAccess`, `checkAccess` |
| `TrainingEntry` | Запись дневника клиента: тренировка, упражнения, комментарии | `createTrainingEntry`, `updateTrainingEntry`, `listTrainingEntries`, `linkEntryToProgram` |
| `Measurement` | Замеры и показатели прогресса клиента | Phase 2 |
| `Program` | Простой тренировочный план (для тренера и для соло-клиента) | `createProgram`, `updateProgram`, `listPrograms` |
| `ProgramAssignment` | Назначение программы клиенту на период | `assignProgram`, `updateAssignment`, `markWorkoutDone`, `getCurrentProgram` |
| `Report` | Отдельный отчётный модуль | Phase 2 |
| `Subscription` | Тариф, лимиты и статус оплаты тренера | Phase 2 |
| `Notification` | Уведомления о приглашениях и статусах внутри продукта | Phase 2 |
| `AuditEvent` | Журнал действий с чувствительными данными | Phase 2 |

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
- OpenSearch и OpenSearch Dashboards для мониторинга и анализа логов.
- Fluent Bit для доставки логов приложения в OpenSearch.

ADR:

- [ADR-001: использовать Keycloak](docs/03-architecture/ADR/ADR-001-use-keycloak.md)
- [ADR-002: использовать POST Full API](docs/03-architecture/ADR/ADR-002-post-full-api.md)
- [ADR-003: использовать Ktor](docs/03-architecture/ADR/ADR-003-ktor.md)
- [ADR-004: использовать Kotlin](docs/03-architecture/ADR/ADR-004-kotlin.md)
- [ADR-005: использовать PostgreSQL](docs/03-architecture/ADR/ADR-005-use-postgresql.md)
- [ADR-006: использовать OpenSearch, OpenSearch Dashboards и Fluent Bit для observability](docs/03-architecture/ADR/ADR-006-use-opensearch-fluent-bit-observability.md)

## Целевая инфраструктура MVP

Целевая инфраструктура MVP описана в архитектурной документации и включает:

- Web UI для клиентских, тренерских и support-сценариев;
- Envoy Gateway как входной proxy и boundary для проверки JWT;
- FitBridge Backend API на Kotlin/Ktor для POST Full бизнес-операций;
- PostgreSQL как прикладное хранилище;
- Keycloak как внешний Identity Server;
- Fluent Bit, OpenSearch и OpenSearch Dashboards для logs-first observability.

Подробности контейнеров и границ ответственности зафиксированы в [C4 Container source](docs/03-architecture/c4/C4_CONTAINER.drawio), [C4 Component source](docs/03-architecture/c4/C4_COMPONENT.drawio) и [Security Architecture / Threat Model](docs/03-architecture/SECURITY_ARCHITECTURE.md).
