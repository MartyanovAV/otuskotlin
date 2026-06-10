# План развития продукта: Разделение на микросервисы (Profile & Training)

## 1. Бизнес-контекст и цели (Business Vision & Goals)
**Цель инициативы:** Обеспечить независимое масштабирование и развитие двух ключевых доменов продукта (Профили пользователей и Тренировочный процесс) для ускорения time-to-market новых фич и повышения отказоустойчивости системы.

**Ожидаемые результаты (Business Value):**
- **Изоляция сбоев:** Падение сервиса профилей не должно блокировать доступ к тренировочным планам.
- **Ускорение разработки:** Разные команды (или разработчики) смогут независимо деплоить фичи для профилей и тренировок.
- **Подготовка к масштабированию:** Закладка фундамента для будущего добавления новых сервисов (например, биллинг, аналитика, клиентский портал).

## 2. Целевая аудитория и стейкхолдеры (Target Audience & Stakeholders)
- **Тренеры (Primary Users):** Получают более стабильный доступ к своим планам и карточкам клиентов.
- **Клиенты тренеров (Secondary Users):** В будущем (вне MVP) получат доступ к своим планам без задержек.
- **Команда разработки (Stakeholders):** Снижение когнитивной нагрузки за счет разделения кодовой базы, упрощение CI/CD пайплайнов.
- **Бизнес/Продукт (Stakeholders):** Возможность приоритизировать развитие одного домена независимо от другого.

## 3. Границы MVP (Scope & Non-Scope)
**Входит в Scope (In Scope):**
- Разделение монолита на два независимых сервиса: **Profile Service** и **Training Service**.
- Разделение API-контрактов (OpenAPI) на `specs-profile` и `specs-training`.
- Настройка API Gateway (Envoy) для маршрутизации запросов.
- Логическое разделение баз данных (2 схемы в рамках одного инстанса БД для MVP). Физическое разделение выносится за рамки MVP.
- Реализация логического удаления (Soft Delete) профиля тренера без каскадного физического удаления данных в Training Service.

**Не входит в Scope (Out of Scope / Non-Scope):**
- Разработка клиентского портала (Client Portal).
- Межсервисное взаимодействие (синхронное по REST или асинхронное через Kafka) для агрегации данных. Сервисы полностью независимы по данным.
- Сложные распределенные транзакции (Saga, 2PC).
- Изменение существующего бизнес-процесса работы тренера.
- Реализация GDPR-комплаенса (право на забвение) и физического удаления данных (Hard Delete) из всех сервисов. Это требование подтверждено, но вынесено в post-MVP.

## 4. Критерии приемки (Acceptance Criteria)
1. **Маршрутизация:** Запросы `/v1/profile/*` успешно обрабатываются Profile Service, а `/v1/clientCard/*` и `/v1/trainingPlan/*` — Training Service.
2. **Независимость данных:** Данные сервисов логически изолированы (разные схемы). Cross-service JOIN/FK и прямое чтение чужой схемы отсутствуют. Требование независимой отказоустойчивости БД (остановка одной БД не влияет на другой сервис) переносится на этап post-MVP.
3. **Изоляция деплоя:** Существуют два независимых Docker-образа, которые могут быть развернуты и перезапущены независимо друг от друга.
4. **Удаление профиля:** При удалении профиля тренера (Soft Delete) его карточки клиентов и тренировочные планы остаются доступны в схеме/БД Training Service (нет потери исторических данных).
5. **Авторизация:** Оба сервиса успешно валидируют JWT-токены от Keycloak и извлекают внутренний ID пользователя.

## 5. Бизнес-риски и предположения (Risks & Assumptions)
**Риски:**
- **Увеличение операционных расходов:** Поддержка двух сервисов требует больше ресурсов инфраструктуры.
- **Сложность траблшутинга:** Поиск ошибок может усложниться из-за распределенной природы системы (требуется сквозное логирование).
- **Единая точка отказа БД (MVP):** Использование одного инстанса БД с двумя схемами означает, что падение инстанса затронет оба сервиса. Принято для MVP ради ускорения time-to-market.

**Предположения:**
- В рамках MVP нам не требуется агрегировать данные из двух сервисов на бэкенде (например, отдавать имя тренера вместе с планом). Если это потребуется клиенту (фронтенду), он сделает два отдельных запроса.
- GDPR-комплаенс (право на забвение) и Hard Delete требуются бизнесу, но их реализация вынесена за рамки MVP (post-MVP).

## 6. Открытые вопросы (Open Questions)
- Когда планируется реализация GDPR-комплаенса (право на забвение) и Hard Delete (post-MVP)? Какие точные сроки и детали реализации (retention policy, legal basis)?
- Как мы будем мониторить SLA каждого сервиса в отдельности?

---

## 7. Техническая реализация (Архитектурный план)

### 7.1. Архитектурные источники и диаграммы

Этот раздел уточняет микросервисный план и не заменяет обзорную архитектуру. Канонические C4-диаграммы редактируются в Draw.io, SVG используются как preview/export в Markdown:

| Уровень | Редактируемый источник | Preview/export |
|---|---|---|
| C4 Context | [C4_CONTEXT.drawio](./c4/C4_CONTEXT.drawio) | [C4_CONTEXT.drawio.svg](./c4/C4_CONTEXT.drawio.svg) |
| C4 Container | [C4_CONTAINER.drawio](./c4/C4_CONTAINER.drawio) | [C4_CONTAINER.drawio.svg](./c4/C4_CONTAINER.drawio.svg) |
| C4 Component | [C4_COMPONENT.drawio](./c4/C4_COMPONENT.drawio) | [C4_COMPONENT.drawio.svg](./c4/C4_COMPONENT.drawio.svg) |

При реализации разделения на микросервисы эти диаграммы должны быть обновлены так, чтобы `Profile Service`, `Training Service`, их логические persistence boundaries (две схемы в одном DB instance для MVP; отдельные DB endpoints/instances — post-MVP) и Envoy Gateway были показаны как отдельные контейнеры/границы владения данными.

### 7.2. Границы сервисов и владение данными

Микросервисные границы строятся вокруг бизнес-областей из MVP без изменения пользовательского сценария тренера:

| Сервис | Ответственность | Владеет данными | Не владеет / не делает в MVP |
|---|---|---|---|
| **Profile Service** | Профиль тренера, локальная доменная проекция аутентифицированного пользователя, soft delete профиля | `TrainerUser`/`FitBridgeUser` projection, `TrainerProfile`, статусы профиля, timestamps soft delete | Не хранит `ClientCard`/`TrainingPlan`, не инициирует каскадное удаление training-данных |
| **Training Service** | Клиентские карточки тренера и тренировочные планы | `ClientCard`, `TrainingPlan`, `trainerPrincipalId`/`trainerUserId` как owner reference, статусы карточек и планов | Не читает БД Profile Service, не требует REST/Kafka-вызова к Profile Service для create/search сценариев |
| **Envoy Gateway** | Публичная точка входа, маршрутизация по path prefix, единые gateway-политики | Не владеет доменными данными | Не агрегирует ответы сервисов и не реализует бизнес-логику |
| **Keycloak** | Identity Provider, выпуск JWT | Authentication data и claims | Не является доменной БД FitBridge |

Ключевые правила владения:

- У каждого сервиса своя persistence boundary: для MVP — отдельная схема в общем DB instance; для post-MVP — отдельный database endpoint/instance. Cross-service foreign keys, join-запросы и прямое чтение чужой схемы/чужих таблиц запрещены.
- `Training Service` хранит owner reference (`trainerPrincipalId`/`trainerUserId`) в своих агрегатах и проверяет ownership локально по JWT principal. Это сохраняет доступность training-сценариев при недоступности Profile Service.
- Soft Delete профиля тренера меняет состояние только в Profile Service. Исторические карточки и планы в Training Service остаются доступными согласно acceptance criteria.
- Future Hard Delete/GDPR требуется (post-MVP), но не реализуется в MVP и потребует отдельного архитектурного решения: retention policy, legal basis, deletion orchestration, правила удаления/анонимизации в каждом сервисе и handling audit/log данных.

### 7.3. API contracts и коммуникации

Внешний контракт для клиента остается path-based и POST Full:

| API-контур | OpenAPI source | Gateway route | Target service | Примечание |
|---|---|---|---|---|
| Profile API | `profile-service/specs/specs/specs-profile-v1.yaml`, `specs-profile-v2.yaml` | `/v1/profile/*`, `/v2/profile/*` | Profile Service | Профиль и soft delete профиля тренера |
| Client Card API | `training-service/specs/specs/specs-training-v1.yaml`, `specs-training-v2.yaml` | `/v1/clientCard/*`, `/v2/clientCard/*` | Training Service | Create/search/read/update/archive карточек в границах тренера |
| Training Plan API | `training-service/specs/specs/specs-training-v1.yaml`, `specs-training-v2.yaml` | `/v1/trainingPlan/*`, `/v2/trainingPlan/*` | Training Service | Create/search/read/update/archive планов в границах тренера |

Правила коммуникации для MVP:

- Синхронные REST-вызовы между Profile Service и Training Service не используются.
- Kafka/брокер сообщений и Saga/2PC не используются.
- Агрегация профиля тренера с планами на backend не реализуется; при будущей потребности потребуется новый endpoint/проекция и отдельное решение.
- Общие response/error/pagination DTO могут быть скопированы из OpenAPI или вынесены в маленький versioned platform-contract module. Доменные модели, репозитории и мапперы между сервисами не шарятся.
- JWT validation выполняется в каждом сервисе. Минимальный auth context: `requestId`, `principalId`, roles/scopes, token issuer/audience. Источник стабильного внутреннего `principalId` должен быть единым для обоих сервисов: либо claim, выпускаемый Keycloak, либо согласованное использование `sub` как stable owner id в MVP.

### 7.4. Структура монорепозитория с учетом манифеста проекта

Проектный манифест фиксирует правило: **сервис = сборщик сущностей**, **сущность = common/biz/api/repo-*/app**, репозитории находятся внутри сущности, а `app-*` являются точками входа. Поэтому целевая структура должна избегать общего repo-layer на уровне сервиса.

```text
fit-bridge-be/
├── build-plugin/
├── platform-contracts/                 # Только технические shared contracts: errors, pagination, requestId (без доменной логики)
├── profile-service/
│   ├── app-ktor/                   # Точка входа Profile Service, Dockerfile/health/config
│   ├── specs/                      # specs-profile-v1.yaml, specs-profile-v2.yaml
│   └── entities/
│       ├── trainer-user/
│       │   ├── common/
│       │   ├── biz/
│       │   ├── api/
│       │   ├── repo-pgjvm/
│       │   ├── repo-inmemory/
│       │   └── app/
│       └── trainer-profile/
│           ├── common/
│           ├── biz/
│           ├── api/
│           ├── repo-pgjvm/
│           ├── repo-inmemory/
│           └── app/
└── training-service/
    ├── app-ktor/                   # Точка входа Training Service, Dockerfile/health/config
    ├── specs/                      # specs-training-v1.yaml, specs-training-v2.yaml
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

Допустимые shared-модули должны быть техническими и стабильными. Нельзя выносить `ClientCard`, `TrainingPlan`, profile repositories или domain mappers в общий сервисный/root `common`, потому что это нарушит границы владения.

### 7.5. Базы данных, миграции и ограничения MVP

Целевая модель данных:

| Persistence boundary | Таблицы/агрегаты | Migration owner | Acceptance impact |
|---|---|---|---|
| **Profile schema (MVP) / Profile DB endpoint (post-MVP)** | `FITBRIDGE_USER`/`TRAINER_USER`, `TRAINER_PROFILE` | `profile-service/entities/*/repo-pgjvm/migrations` | В MVP изолирует владение данными через отдельную схему; независимая отказоустойчивость БД не гарантируется |
| **Training schema (MVP) / Training DB endpoint (post-MVP)** | `CLIENT_CARD`, `TRAINING_PLAN` | `training-service/entities/*/repo-pgjvm/migrations` | В MVP изолирует владение данными через отдельную схему; независимая отказоустойчивость БД не гарантируется |

Технические ограничения:

- Для MVP источником истины является логическая изоляция: две схемы в одном PostgreSQL instance, запрет cross-service JOIN/FK и запрет прямого чтения чужой схемы. Риск единой точки отказа БД явно принят для MVP.
- Независимая отказоустойчивость database endpoints/instances переносится на post-MVP и не является acceptance-критерием MVP.
- Миграции выполняются отдельно по сервисам и применяются только к собственной схеме сервиса. Порядок релиза не должен требовать “одной общей миграции” на весь продукт.
- В Training schema не должно быть FK на таблицы Profile schema и прямых read-запросов к ней. Связь с тренером — через immutable/stable owner id из auth context.
- В MVP backup/restore, RPO/RTO и smoke restore остаются ограничены единым DB instance; отдельные процедуры для Profile DB и Training DB проектируются на post-MVP вместе с физическим разделением.

### 7.6. Migration & deployment phases

| Фаза | Цель | Основные действия | Exit criteria |
|---|---|---|---|
| **Phase 0. Baseline audit** | Зафиксировать текущие контракты и зависимости | Инвентаризация OpenAPI, текущих модулей, таблиц, ownership checks, C4/ERD расхождений | Понятен список переносимых сущностей и нет скрытых cross-domain вызовов |
| **Phase 1. Contract split** | Разделить внешние контракты без изменения бизнес-сценариев | Поддержать `specs-profile-*` и `specs-training-*`, убрать доменные shared DTO между сервисами | Генерация API независима; маршруты соответствуют acceptance criteria |
| **Phase 2. Module/entity split** | Привести кодовую структуру к манифесту | Разнести сущности по `entities/*`, перенести repo внутрь сущностей, выделить `app-ktor` | Каждый сервис собирается независимо, доменные зависимости не пересекают границы |
| **Phase 3. Data split** | Разделить persistence boundary логически для MVP | Создать отдельные схемы Profile и Training в одном DB instance, перенести миграции, убрать cross-schema FK/JOIN/read чужой схемы, зафиксировать риск общей БД | Сервисы используют только свои схемы; прямого чтения чужой схемы нет; независимая отказоустойчивость БД перенесена на post-MVP |
| **Phase 4. Independent deploy** | Запустить два runtime-контейнера | Два Docker-образа, отдельные env/config/secrets, Envoy routes, health checks | Сервисы деплоятся/перезапускаются независимо |
| **Phase 5. Cutover & stabilization** | Перевести трафик на целевую схему | Smoke/e2e checks по `/profile`, `/clientCard`, `/trainingPlan`, наблюдение SLA/логов | Acceptance criteria выполнены, rollback path описан |

Rollback должен быть фазовым: для contract/module split — возврат gateway routes на предыдущий backend; для data split — заранее подготовленный read-only freeze/backup restore plan. Двунаправленная запись между сервисами в MVP не вводится, чтобы не усложнять scope.

### 7.7. Observability, health checks и SLA

Минимальный observability-контур опирается на [ADR-006](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md) и расширяется до двух сервисов:

| Область | Profile Service | Training Service | Gateway/Platform |
|---|---|---|---|
| Health | `/health/live`, `/health/ready`, проверка Profile schema/DB connection | `/health/live`, `/health/ready`, проверка Training schema/DB connection | Envoy upstream health, route availability |
| Logs | JSON logs: `service=profile-service`, `requestId`, `principalId`, action, result, durationMs | JSON logs: `service=training-service`, `requestId`, `principalId`, action, result, durationMs | Access logs с route, upstream, status, latency |
| SLA/SLI | availability, 5xx rate, p95 latency, DB readiness | availability, 5xx rate, p95 latency, DB readiness | Route-level uptime `/v1/profile/*`, `/v1/clientCard/*`, `/v1/trainingPlan/*` |
| Security audit | profile soft delete, auth denied, token validation errors | card/plan create/search/archive, auth denied | Correlation id propagation |

Технические требования к SLA-мониторингу:

- SLA измеряется отдельно по каждому сервису и route group, а не только по общей доступности gateway.
- `requestId` должен создаваться/пробрасываться на gateway и попадать в логи обоих сервисов.
- Readiness должен учитывать доступность собственной схемы сервиса и не зависеть от чужого сервиса/чужой схемы. В MVP падение общего DB instance может сделать оба сервиса unready; этот риск принят и не считается нарушением MVP acceptance criteria.
- Для production SLA нужны alert thresholds: 5xx rate, latency p95/p99, DB connection errors, restart loop, заполнение диска/индексов OpenSearch.
- Sensitive payload не логируется: ФИО, email, заметки, содержимое тренировочного плана и JWT исключаются из логов.

### 7.8. ADR-like trade-offs и предварительные решения

| Решение | Вариант A | Вариант B | Предварительный выбор для MVP | Последствия |
|---|---|---|---|---|
| Persistence isolation | Две схемы в одном PostgreSQL | Два независимых DB endpoints/instances | **A для MVP**, B переносится на post-MVP | A быстрее и дешевле, но оставляет единую точку отказа БД; риск принят для MVP |
| Межсервисная коммуникация | Нет runtime-вызовов между сервисами | REST/Kafka для sync/aggregation | **Нет runtime-вызовов** | Меньше связность, но frontend делает отдельные запросы при необходимости |
| Удаление профиля | Soft Delete только в Profile Service | Saga/Hard Delete across services | **Soft Delete** | Сохраняет исторические training-данные в MVP; GDPR/Hard Delete проектируется отдельно для post-MVP |
| Shared code | Общий root `common` с доменом | Только platform contracts без домена | **Только platform contracts** | Требует дублировать/версионировать DTO, но защищает границы сервисов |
| Auth principal | Локальный user id из Profile DB | Stable claim/`sub` из JWT | **Stable claim/`sub` для Training Service** | Training Service не зависит от Profile Service при авторизации |

Эти решения являются плановыми и при начале реализации должны быть оформлены отдельными ADR, если меняют уже принятые решения [ADR-001](./ADR/ADR-001-use-keycloak.md), [ADR-003](./ADR/ADR-003-ktor.md), [ADR-005](./ADR/ADR-005-use-postgresql.md), [ADR-006](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

### 7.9. Технические риски и меры снижения

| Риск | Вероятность | Влияние | Митигация |
|---|---:|---:|---|
| Рост инфраструктурной стоимости из-за двух сервисов и будущего физического разделения БД | Medium | Medium | В MVP использовать две схемы в одном DB instance; независимые DB endpoints планировать отдельным post-MVP этапом |
| Незаметное появление runtime-зависимости Training -> Profile | Medium | High | Architecture tests/dependency checks, запрет HTTP clients между сервисами в MVP, ревью модульных зависимостей |
| Неправильный owner id приведет к потере доступа к training-данным | Medium | High | Зафиксировать auth principal contract до data split; миграционный dry-run и negative tests ownership |
| Две схемы в одном DB-инстансе будут ошибочно приняты за физическую независимость отказов | Medium | High | Явно документировать MVP-компромисс и принятый риск единой точки отказа; проверку остановки отдельного DB endpoint перенести на post-MVP |
| SLA нельзя измерить по отдельным сервисам | Medium | Medium | Ввести route-level metrics/log queries и health checks до cutover |
| Future GDPR Hard Delete (post-MVP) потребует сложной каскадной модели | High (требование подтверждено) | High | Не смешивать с MVP; завести отдельный ADR/BR для проектирования механизма распределенного удаления, retention policy, legal basis и handling audit/log данных; хранить deletion markers/retention metadata при необходимости |

## 8. Сверка с PO-частью и конфликтность

Бизнес-смысл, scope/non-scope и acceptance criteria не изменялись. Технический план согласован с MVP: два независимых сервиса, отдельные API-контракты, логическая изоляция данных через две схемы в одном DB instance, отсутствие межсервисной агрегации, soft delete профиля без каскадного удаления training-данных. Риск единой точки отказа БД принят для MVP.

Проверка согласованности для Orchestrator:

- **Разрешено PO:** Конфликт по изоляции БД устранен. Для MVP утверждено логическое разделение (2 схемы), требование независимой отказоустойчивости БД перенесено в post-MVP.
- **GDPR/Hard Delete:** Требование подтверждено и зафиксировано как post-MVP. MVP явно ограничен Soft Delete профиля без распределенного Hard Delete; post-MVP требует отдельного ADR/BR по distributed deletion, retention policy, legal basis и audit/log handling.

## 9. Открытые технические вопросы

- Какой claim является каноническим `principalId`/`trainerUserId` для обоих сервисов: custom internal id в JWT или Keycloak `sub`?
- Какие целевые SLA/SLO по availability, p95 latency и 5xx rate принимаются отдельно для Profile Service и Training Service?
- Какие критерии и сроки post-MVP нужны для перехода с двух схем в одном DB instance на два независимых PostgreSQL instance/container/cluster или managed database endpoints?
- Нужен ли отдельный ADR для микросервисного разделения и data ownership перед началом реализации?
- Каковы точные требования к retention policy, legal basis, механизму распределенного удаления и audit/log handling для исторических карточек/планов в рамках реализации GDPR/Hard Delete (post-MVP)?
