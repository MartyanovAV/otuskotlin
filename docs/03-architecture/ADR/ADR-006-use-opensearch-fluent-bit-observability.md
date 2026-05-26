# ADR-006: Использовать OpenSearch, OpenSearch Dashboards и Fluent Bit для observability MVP

**Статус:** Accepted  
**Дата:** 2026-05-21

## Context

MVP FitBridge должен быть проверяемым в пилоте: команда должна видеть доступность MVP-контура, ошибки API, критичные пользовательские события и нарушения performance targets. Нефункциональные требования фиксируют необходимость мониторинга доступности и производительности, логирования событий приглашений, принятия доступа, отзыва доступа, назначения планов и выполнения тренировок.

Целевой observability-контур MVP включает:

- `Fluent Bit` как агент доставки структурированных логов;
- `OpenSearch` как хранилище и поисковый движок для логов;
- `OpenSearch Dashboards` как UI для support/debugging сценариев;
- masked JSON-события от backend API и инфраструктурных контейнеров;
- индексы логов с retention и lifecycle policy, определёнными для окружения.

## Comparison

| Criteria | OpenSearch + Dashboards + Fluent Bit | Loki + Grafana + Promtail | ELK / Elastic Cloud | Docker logs only |
|---|:---:|:---:|:---:|:---:|
| Контур MVP без внешнего SaaS | ✅ | ✅ | ⚠️ | ✅ |
| Полнотекстовый поиск и фильтрация | ✅ | ⚠️ | ✅ | ❌ |
| Приём структурированных JSON-логов | ✅ | ✅ | ✅ | ❌ |
| Dashboards для support/debugging | ✅ | ✅ | ✅ | ❌ |
| Операционная простота для MVP | ✅ | ⚠️ | ❌ | ✅ |
| Достаточность для MVP audit-oriented logs | ✅ | ⚠️ | ✅ | ❌ |

## Decision

Использовать **OpenSearch + OpenSearch Dashboards + Fluent Bit** как observability-стек MVP.

Для MVP observability включает:

- structured application logs в stdout/stderr;
- доставку логов через Fluent Bit в OpenSearch;
- просмотр и поиск логов через OpenSearch Dashboards;
- health endpoint Fluent Bit на `:2020`;
- инфраструктурное логирование критичных событий доступа, дневника, программ и онбординга;
- correlation/request id в логах backend API.

Расширенный продуктовый `AuditEvent` API не входит в MVP. Для MVP audit-oriented события фиксируются как masked structured logs без раскрытия лишних персональных и health-adjacent данных.

## Scope аудита MVP

MVP-аудит в рамках этого ADR — это infrastructure audit-oriented logging, а не продуктовая audit-сущность. Обязательные события MVP:

| Event action | Когда логируется | Минимальный результат |
|---|---|---|
| `access.acceptInvite` | Клиент принимает приглашение | `SUCCESS` или ожидаемый `ERROR` |
| `access.declineInvite` | Клиент отклоняет приглашение | `SUCCESS` или ожидаемый `ERROR` |
| `access.grant` | Доступ тренеру выдан или активирован | `SUCCESS` |
| `access.revoke` | Клиент отзывает доступ тренера | `SUCCESS` |
| `profile.deleteOrArchiveRequested` | Пользователь запрашивает удаление или архивацию профиля | `SUCCESS` или `VALIDATION_ERROR` |
| `diary.createEntry` | Создана запись дневника | `SUCCESS` |
| `diary.updateEntry` | Изменена запись дневника | `SUCCESS` или `VALIDATION_ERROR` |
| `diary.deleteEntry` | Запись дневника удалена/soft-deleted | `SUCCESS` |
| `program.assign` | Программа назначена клиенту | `SUCCESS` |
| `program.updateAssignment` | Назначение программы изменено | `SUCCESS` или `VALIDATION_ERROR` |
| `program.cancelAssignment` | Назначение программы отменено | `SUCCESS` |
| `program.completeWorkout` | Клиент отметил тренировку выполненной | `SUCCESS` |
| `access.validateScope` | Проверка scope завершилась отказом | `DENIED` |

Phase 2 может добавить продуктовый `AuditEvent` API, отдельную audit entity/table, пользовательский или админский просмотр audit trail и расширенную retention/legal-модель. Эти элементы не входят в MVP.

## Rationale

- OpenSearch подходит для поиска по событиям доступа, ошибкам API и техническим диагностическим данным.
- Fluent Bit лёгкий и достаточный для MVP-контура доставки логов в OpenSearch.
- Dashboards дают быстрый UI для поддержки пилотов без необходимости разрабатывать отдельную админку.
- Отделение infrastructure logs от будущего `AuditEvent` API сохраняет MVP scope и не смешивает технические логи с продуктовой аудиторской моделью.

## Required Log Fields

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
| `action` | ✅ | `access.acceptInvite`, `access.revoke`, `diary.createEntry` и т.п. |
| `entityType` | ⚠️ | `AccessGrant`, `TrainingEntry`, `ProgramAssignment` |
| `entityId` | ⚠️ | Внутренний id сущности без payload данных |
| `result` | ✅ | `SUCCESS`, `DENIED`, `VALIDATION_ERROR`, `ERROR` |
| `durationMs` | ✅ | Длительность операции |
| `errorCode` | ⚠️ | Стабильный код ошибки без stacktrace для expected ошибок |

## Sensitive Data Rules

| Data | Rule |
|---|---|
| Телефон, email, ФИО | Не писать в логи; использовать internal ids |
| `notes`, `goals`, `mood`, упражнения/вес/RPE | Не писать raw payload в логи |
| JWT/access token/refresh token | Никогда не писать в логи |
| Invite token | В БД и логах только hash/token id, не raw token |
| Ошибки валидации | Писать field name и error code, не исходное значение чувствительного поля |

## Consequences

**Positive:**

- MVP получает единый путь для сбора и просмотра логов.
- Поддержка может расследовать ошибки приглашений, доступов, дневника и программ без прямого доступа к БД.
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

- Для MVP backend должен логировать critical events из `RTM-013`: принятие/отклонение приглашения, выдача/отзыв доступа, запрос удаления/архивации профиля, создание/изменение/удаление дневника, назначение/изменение/отмена программы, выполнение тренировки и отказ доступа из-за scope.
- Performance targets из NFR должны попадать в логи через `durationMs`; отдельная metrics-система может быть добавлена позже.
- Alerting не входит в MVP, но нарушение порогов latency/error rate должно быть возможно найти через OpenSearch queries.
- Для production потребуется отдельное решение по retention, index lifecycle, secrets и resource limits.
