# ADR-006: Использовать OpenSearch, OpenSearch UI и Fluent Bit для observability MVP

**Статус:** Accepted  
**Дата:** 2026-05-21

## Контекст

Trainer Diary MVP FitBridge должен быть проверяемым в пилоте: команда должна видеть доступность MVP-контура, ошибки API, критичные пользовательские события и нарушения performance targets. Нефункциональные требования фиксируют необходимость мониторинга доступности и производительности, логирования событий профиля тренера, клиентских карточек и тренировочных планов.

Целевой observability-контур MVP включает:

- `Fluent Bit` как агент доставки структурированных логов;
- `OpenSearch` как хранилище и поисковый движок для логов;
- `OpenSearch UI` как интерфейс для support/debugging сценариев;
- masked JSON-события от backend API и инфраструктурных контейнеров;
- индексы логов с retention и lifecycle policy, определёнными для окружения.

## Сравнение

| Criteria | OpenSearch + UI + Fluent Bit | Loki + Grafana + Promtail | ELK / Elastic Cloud | Docker logs only |
|---|:---:|:---:|:---:|:---:|
| Контур MVP без внешнего SaaS | ✅ | ✅ | ⚠️ | ✅ |
| Полнотекстовый поиск и фильтрация | ✅ | ⚠️ | ✅ | ❌ |
| Приём структурированных JSON-логов | ✅ | ✅ | ✅ | ❌ |
| UI для support/debugging | ✅ | ✅ | ✅ | ❌ |
| Операционная простота для MVP | ✅ | ⚠️ | ❌ | ✅ |
| Достаточность для MVP audit-oriented logs | ✅ | ⚠️ | ✅ | ❌ |

## Решение

Использовать **OpenSearch + OpenSearch UI + Fluent Bit** как observability-стек MVP.

Для MVP observability включает:

- structured application logs в stdout/stderr;
- доставку логов через Fluent Bit в OpenSearch;
- просмотр и поиск логов через OpenSearch UI;
- health endpoint Fluent Bit на `:2020`;
- инфраструктурное логирование критичных событий тренерского онбординга, клиентских карточек и планов;
- correlation/request id в логах backend API.

Расширенный продуктовый `AuditEvent` API не входит в MVP. Для MVP audit-oriented события фиксируются как masked structured logs без раскрытия лишних персональных и health-adjacent данных.

## Scope аудита MVP

MVP-аудит в рамках этого ADR — это infrastructure audit-oriented logging, а не продуктовая audit-сущность. Обязательные события MVP:

| Event action | Когда логируется | Минимальный результат |
|---|---|---|
| `clientCard.create` | Тренер создал клиентскую карточку | `SUCCESS` или ожидаемый `ERROR` |
| `clientCard.update` | Тренер изменил карточку | `SUCCESS` или `VALIDATION_ERROR` |
| `clientCard.archive` | Тренер архивировал карточку | `SUCCESS` |
| `trainingPlan.create` | Тренер создал план | `SUCCESS` или `VALIDATION_ERROR` |
| `trainingPlan.update` | Тренер изменил план | `SUCCESS` или `VALIDATION_ERROR` |
| `trainingPlan.archive` | План архивирован | `SUCCESS` |
| `access.denied` | Доступ к приватному ресурсу отказан | `DENIED` + reason code без sensitive payload |

Phase 2 может добавить продуктовый `AuditEvent` API, отдельную audit entity/table, пользовательский или админский просмотр audit trail и расширенную retention/legal-модель. Эти элементы не входят в MVP.

## Обоснование

- OpenSearch подходит для поиска по событиям доступа, ошибкам API и техническим диагностическим данным.
- Fluent Bit лёгкий и достаточный для MVP-контура доставки логов в OpenSearch.
- OpenSearch UI даёт быстрый интерфейс для поддержки пилотов без необходимости разрабатывать отдельную админку.
- Отделение infrastructure logs от будущего `AuditEvent` API сохраняет MVP scope и не смешивает технические логи с продуктовой аудиторской моделью.

## Обязательные поля логов

Application logs должны быть структурированными JSON-событиями с минимальным набором полей:

| Field | Required | Description |
|---|:---:|---|
| `timestamp` | ✅ | Время события в UTC |
| `level` | ✅ | `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `service` | ✅ | Имя сервиса/контейнера |
| `environment` | ✅ | Имя окружения: `dev`, `stage`, `prod` или иной согласованный код окружения |
| `requestId` | ✅ | Correlation id одного HTTP-запроса |
| `userId` | ⚠️ | Только внутренний id; не `phone`, не email |
| `keycloakSubject` | ⚠️ | Допустимо для auth-debug, если не раскрывает PII |
| `action` | ✅ | `clientCard.create`, `trainingPlan.search`, `access.denied` и т.п. |
| `entityType` | ⚠️ | `ClientCard`, `TrainingPlan`, `TrainerProfile` |
| `entityId` | ⚠️ | Внутренний id сущности без payload данных |
| `result` | ✅ | `SUCCESS`, `DENIED`, `VALIDATION_ERROR`, `ERROR` |
| `durationMs` | ✅ | Длительность операции |
| `errorCode` | ⚠️ | Стабильный код ошибки без stacktrace для expected ошибок |

## Правила для чувствительных данных

| Data | Rule |
|---|---|
| Телефон, email, ФИО | Не писать в логи; использовать internal ids |
| Заметки тренера, содержимое плана, комментарии клиента | Не писать raw payload в логи |
| JWT/access token/refresh token | Никогда не писать в логи |
| Ошибки валидации | Писать field name и error code, не исходное значение чувствительного поля |

## Последствия

**Positive:**

- MVP получает единый путь для сбора и просмотра логов.
- Поддержка может расследовать ошибки карточек и планов без прямого доступа к БД и без sensitive payload.
- Решение не требует внешних SaaS для базового observability MVP.
- Structured logs создают основу для будущих метрик и алертов.

**Negative:**

- OpenSearch тяжелее, чем простой Docker logs или Loki-only стек.
- Retention, index lifecycle и alerting ещё нужно детализировать отдельно.
- Секреты Fluent Bit/OpenSearch должны управляться отдельно для каждого окружения и не храниться в документации.
- Metrics/traces вне минимального MVP; MVP начинается с logs-first observability.

**Risks:**

| Risk | Likelihood | Impact | Mitigation |
|---|---:|---:|---|
| В логи попадут персональные или health-adjacent данные | Medium | High | Ввести deny-list/маскирование, ревью log statements, негативные тесты на sensitive payloads |
| OpenSearch переполнится из-за отсутствия retention | Medium | Medium | Настроить index lifecycle/retention перед длительным пилотом |
| Нет requestId, сложно связывать события одного запроса | Medium | Medium | Добавить requestId middleware в Ktor backend первым observability task |
| Небезопасные test/dev credentials попадут в production | Low | High | Разделить secrets по окружениям и ввести production secret management до production deploy |

## Архитектурные требования к логированию

- Для Trainer Diary MVP backend должен логировать critical events: создание/чтение/изменение/архивация/поиск карточки, создание/чтение/изменение/архивация/поиск плана и отказы доступа.
- Performance targets из NFR должны попадать в логи через `durationMs`; отдельная metrics-система может быть добавлена позже.
- Alerting не входит в MVP, но нарушение порогов latency/error rate должно быть возможно найти через OpenSearch queries.
- Для production потребуется отдельное решение по retention, index lifecycle, secrets и resource limits.
