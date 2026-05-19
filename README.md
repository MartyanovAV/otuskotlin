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

MVP фокусируется на критическом пути первого коммерческого релиза:

1. Тренер регистрируется и открывает рабочий кабинет.
2. Тренер приглашает первого клиента по ссылке или коду.
3. Клиент принимает приглашение, создает профиль и явно выдает доступ.
4. Тренер создает или выбирает простую тренировочную программу.
5. Тренер назначает программу клиенту.
6. Клиент ведет дневник, вносит тренировки, замеры и отметки выполнения.
7. Тренер видит базовый отчет по тренировкам, adherence и замерам.
8. Тренер видит тариф, лимиты, статус оплаты и подсказки по апгрейду.

В MVP не входят полноценный multi-specialist production-сценарий, командный тариф студии, AI-генерация программ, маркетплейс специалистов и продвинутая медицинская аналитика.

Подробнее: [docs/01-business/MVP_SCOPE_SUMMARY.md](docs/01-business/MVP_SCOPE_SUMMARY.md).

## Эскиз фронтенда

Фронтенд-представление подготовлено как интерактивные HTML-прототипы:

- [ux-prototype/index.html](ux-prototype/index.html) - мобильный клиентский сценарий дневника, прогресса и доступа.
- [ux-prototype/desktop.html](ux-prototype/desktop.html) - desktop-сценарий тренера: клиенты, программы, отчеты и биллинг.

Ключевые экраны MVP:

- клиентский дневник тренировок;
- экран текущих доступов клиента;
- карточка клиента для тренера;
- конструктор простой программы;
- базовый отчет по прогрессу;
- экран тарифа и лимитов.

## Сущности и методы

Проект использует подход **POST Full API**: бизнес-операции выполняются через `POST` с JSON-запросом и JSON-ответом. Это упрощает авторизацию, аудит и валидацию команд.

| Сущность | Назначение | Основные методы MVP |
|---|---|---|
| `User` | Учетная запись в Keycloak, базовая идентификация пользователя | `registerClient`, `registerTrainer`, `login`, `logout`, `restoreAccess` |
| `ClientProfile` | Профиль клиента и принадлежащая ему тренировочная история | `createClientProfile`, `updateClientProfile`, `getClientProfile`, `requestProfileDeletion` |
| `TrainerProfile` | Профессиональный профиль тренера и рабочий кабинет | `createTrainerProfile`, `updateTrainerProfile`, `getTrainerDashboard` |
| `AccessGrant` | Разрешение тренеру работать с данными клиента | `createInvite`, `acceptInvite`, `grantAccess`, `revokeAccess`, `listAccesses`, `checkAccess` |
| `TrainingEntry` | Запись дневника клиента: тренировка, упражнения, комментарии | `createTrainingEntry`, `updateTrainingEntry`, `listTrainingEntries`, `linkEntryToProgram` |
| `Measurement` | Замеры и показатели прогресса клиента | `createMeasurement`, `updateMeasurement`, `listMeasurements` |
| `Program` | Тренировочная программа или шаблон тренера | `createProgram`, `updateProgram`, `createProgramTemplate`, `listPrograms` |
| `ProgramAssignment` | Назначение программы клиенту на период | `assignProgram`, `updateAssignment`, `markWorkoutDone`, `getCurrentProgram` |
| `Report` | Базовая аналитика по тренировкам, adherence и замерам | `buildProgressReport`, `getAdherenceSummary`, `getMeasurementTrends` |
| `Subscription` | Тариф, лимиты и статус оплаты тренера | `getSubscriptionStatus`, `checkLimits`, `upgradePlan`, `registerPaymentStatus` |
| `Notification` | Уведомления о приглашениях, программах, лимитах и оплате | `sendInviteNotification`, `sendProgramNotification`, `sendBillingNotification` |
| `AuditEvent` | Журнал действий с чувствительными данными | `writeAuditEvent`, `listAuditEventsForInvestigation` |

Функциональные требования описаны в [docs/02-analysis/01-functional-requiremens.md](docs/02-analysis/01-functional-requiremens.md), нефункциональные - в [docs/02-analysis/02-nonfunctional-requirements.md](docs/02-analysis/02-nonfunctional-requirements.md).

## Архитектурное видение

Архитектура описана через C4 и draw.io-диаграммы:

- [диаграмма контекста](docs/03-architecture/c4/arch-C4-Context.drawio.svg);
- [диаграмма контейнеров](docs/03-architecture/c4/arch-C4-containers.drawio.svg);
- [диаграмма компонентов](docs/03-architecture/c4/arch-C4-components.drawio.svg);
- [упрощенная слоистая диаграмма](docs/03-architecture/c4/fitbridge-arch.drawio.svg).

Индекс архитектурной документации: [docs/03-architecture/architecture-overview.md](docs/03-architecture/architecture-overview.md).

### Архитектурные решения

- Kotlin как основной язык backend-кода.
- Ktor как backend framework.
- Keycloak как внешний Identity Server.
- POST Full API для бизнес-операций.
- OpenSearch и OpenSearch Dashboards для мониторинга и анализа логов.
- Fluent Bit для доставки логов приложения в OpenSearch.

ADR:

- [ADR-001: использовать Keycloak](docs/03-architecture/ADR/ADR-001-use-keycloak.md)
- [ADR-002: использовать POST Full API](docs/03-architecture/ADR/ADR-002-post-full-api.md)
- [ADR-003: использовать Ktor](docs/03-architecture/ADR/ADR-003-ktor.md)
- [ADR-004: использовать Kotlin](docs/03-architecture/ADR/ADR-004-kotlin.md)

## Инфраструктура

Инфраструктура запуска находится в [deploy](deploy):

- [deploy/Dockerfile](deploy/Dockerfile) - образ приложения на базе Nginx для статического прототипа;
- [deploy/docker-compose.yml](deploy/docker-compose.yml) - локальный запуск приложения и инфраструктуры;
- `app` - локальный контейнер приложения;
- `keycloak` - авторизация и импорт realm `fit-bridge`;
- `opensearch` - хранение и поиск логов;
- `dashboards` - веб-интерфейс OpenSearch Dashboards;
- `fluent-bit` - сбор и доставка логов;
- `envoy` - входной прокси.

Запуск:

```bash
cd deploy
docker compose up --build
```

Полезные адреса после запуска:

- приложение: `http://localhost:8080`;
- OpenSearch: `https://localhost:9200`;
- OpenSearch Dashboards: `http://localhost:5601`;
- Keycloak доступен через инфраструктурный контур compose и Envoy.
