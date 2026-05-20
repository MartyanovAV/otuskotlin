# Architecture Overview - FitBridge

## 1. Цель документа

Этот документ является индексом целевой архитектуры сокращённого MVP FitBridge: B2B2C SaaS-платформы для клиентов и независимых тренеров.

Архитектура отражает требования:

- клиент владеет историей тренировок и доступов;
- тренер работает только с явно разрешёнными клиентскими данными;
- аутентификация и базовая IAM-функция делегируются Keycloak;
- бизнес-операции API выполняются по подходу POST Full;
- проект развивается на Kotlin и Ktor без выбора дополнительных фреймворков на этом этапе.

## 2. Навигация

| Документ | Содержание |
|---|---|
| [ADR/](./ADR/) | Архитектурные решения ADR |
| [c4/C4_CONTEXT.md](./c4/C4_CONTEXT.md) | C4 Context: пользователи, FitBridge и внешние системы |
| [c4/C4_CONTAINER.md](./c4/C4_CONTAINER.md) | C4 Container: Web UI, Envoy, Ktor API, DB, Keycloak и observability |
| [c4/C4_COMPONENT.md](./c4/C4_COMPONENT.md) | C4 Component: целевая структура FitBridge Backend API |
| [ERD.md](./ERD.md) | ERD: целевая модель данных MVP, связи, ownership и ограничения |
| [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md) | Security Architecture / Threat Model: Keycloak/OIDC/JWT, access decision, 152-ФЗ controls и угрозы MVP |

## 3. Диаграммы

### 3.1. Диаграмма контекста C4

[C4 Context diagram](./c4/C4_CONTEXT.md) описывает FitBridge как систему в окружении клиентов, тренеров, Keycloak, OpenSearch и ручной проверки платёжной гипотезы.

### 3.2. Диаграмма контейнеров C4

[C4 Container diagram](./c4/C4_CONTAINER.md) описывает контейнеры target-state MVP и текущий transition-state локального стенда.

### 3.3. Диаграмма компонентов C4

[C4 Component diagram](./c4/C4_COMPONENT.md) описывает целевые компоненты Ktor backend API: Profile, Access, Diary, Program, Progress, Auth и Audit Logging.

### 3.4. Статус диаграмм

C4-диаграммы ведутся в Mermaid markdown-файлах. SVG-экспорты могут быть добавлены позднее как производные артефакты, но источником правды являются `.md` файлы в `docs/03-architecture/c4/`.

### 3.5. ERD MVP

[ERD MVP](./ERD.md) фиксирует целевые сущности `User`, `ClientProfile`, `TrainerProfile`, `AccessGrant`, `Invite`, `TrainingEntry`, `Program`, `ProgramAssignment`, их связи, ownership и ограничения.

### 3.6. Security Architecture / Threat Model

[Security Architecture / Threat Model](./SECURITY_ARCHITECTURE.md) фиксирует scope безопасности MVP, роли `CLIENT`/`TRAINER`/future `ADMIN`, границу Keycloak и FitBridge Backend, модель owner/grant/scope access decision, controls для 152-ФЗ и health-adjacent данных, а также таблицу угроз для token leakage, IDOR, revoke, invite token abuse, logging и OpenSearch exposure.

## 4. Контекст продукта

FitBridge закрывает разрыв между таблицами, чатами и тяжёлыми club-CRM. Сокращённый MVP фокусируется на двух базовых путях:

**Путь 1: Trainer-led**
1. Тренер регистрируется.
2. Приглашает первого клиента.
3. Клиент принимает приглашение и подтверждает доступ.
4. Тренер назначает план.
5. Клиент отмечает выполнение, тренер видит статус.

**Путь 2: Solo-client (PLG)**
1. Клиент самостоятельно регистрируется.
2. Создаёт себе индивидуальный план.
3. Отмечает выполнение без подключения специалиста.

## 5. Архитектурные принципы

- **Client-owned data:** история клиента не зависит от текущего тренера.
- **Privacy by design:** доступ к чувствительным данным проверяется на каждый запрос.
- **POST Full API:** все бизнес-операции проходят через `POST` с JSON-контрактами.
- **Identity externalization:** вход и токены делегируются Keycloak.
- **Модульность домена:** дневник, доступы, планы и история выполнения разделены внутри backend.
- **TBD без преждевременного выбора:** UI-фреймворк, платформа деплоя и notification-провайдеры остаются открытыми решениями.

## 6. Технологический контур

| Область | Решение |
|---|---|
| Язык backend | Kotlin |
| Backend framework | Ktor |
| Identity Server | Keycloak |
| API style | POST Full, HTTPS/JSON |
| UI | Web UI, конкретный фреймворк TBD |
| Хранилище приложения | PostgreSQL |
| Observability | OpenSearch, OpenSearch Dashboards, Fluent Bit |
| Уведомления | MVP: статус приглашения в продукте; внешние провайдеры TBD |

## 7. Доменные контуры MVP

- **Profile Context:** клиентские и тренерские профили.
- **Access Context:** приглашение, подтверждение и отзыв доступа одного активного тренера.
- **Diary Context:** тренировки и комментарии к тренировкам.
- **Program Context:** простые планы и назначения.
- **Progress Context:** история тренировок и статусы выполнения в карточке клиента.
- **Audit Component:** журналирование действий с чувствительными данными.

## 8. Безопасность и данные

- Keycloak используется для аутентификации, выдачи токенов и базовых claims.
- FitBridge хранит доменные роли, связи клиент-тренер и статус доступов.
- Тренер не получает доступ к данным без активного разрешения клиента.
- Операции с доступами, дневником и планами журналируются.
- Данные о здоровье и замерах рассматриваются как чувствительные данные и не входят в обязательный MVP.
- Детальная модель безопасности и угроз ведётся в [SECURITY_ARCHITECTURE.md](./SECURITY_ARCHITECTURE.md).

## 9. Нефункциональные ориентиры

- Дневник: сохранение записи менее 800 мс.
- Операции доступа: менее 1000 мс.
- Назначение простого плана: менее 1200 мс.
- Открытие истории клиента: менее 1000 мс.
- Доступность основных функций: не ниже 99,5%.
- Доступность контроля доступа: не ниже 99,9%.
- RPO клиентской истории: не более 24 часов.

## 10. ADR

- [ADR-001: Использовать Keycloak как Identity Server](./ADR/ADR-001-use-keycloak.md)
- [ADR-002: Использовать POST Full API](./ADR/ADR-002-post-full-api.md)
- [ADR-003: Использовать Ktor для backend](./ADR/ADR-003-ktor.md)
- [ADR-004: Использовать Kotlin как основной язык](./ADR/ADR-004-kotlin.md)
- [ADR-005: Использовать PostgreSQL как основное хранилище приложения](./ADR/ADR-005-use-postgresql.md)
- [ADR-006: Использовать OpenSearch, OpenSearch Dashboards и Fluent Bit для observability MVP](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md)

## 11. Открытые решения

- Выбор UI-фреймворка.
- Детализация схемы хранения и retention для инфраструктурного аудита.
- Выбор платформы деплоя.
- Выбор notification-провайдера.
- Детализация API-контрактов POST Full.
