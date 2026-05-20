# ADR-006: Использовать OpenSearch, OpenSearch Dashboards и Fluent Bit для observability MVP

**Статус:** Accepted  
**Дата:** 2026-05-21

## Context

MVP FitBridge должен быть проверяемым в пилоте: команда должна видеть доступность локального стенда, ошибки API, критичные пользовательские события и нарушения performance targets. Нефункциональные требования фиксируют необходимость мониторинга доступности и производительности, логирования событий приглашений, принятия доступа, отзыва доступа, назначения планов и выполнения тренировок.

Текущий deploy уже содержит локальный observability-контур:

- `Fluent Bit` принимает логи через fluent forward input `24224` и отдаёт health/metrics endpoint `2020`;
- `OpenSearch` хранит индексы логов;
- `OpenSearch Dashboards` предоставляет UI для просмотра логов;
- `docker-compose.yml` направляет логи `app` в Fluent Bit с tag `app.logs`;
- `fluent-bit.conf` парсит JSON из поля `message` и пишет daily indices `app-logs-*`, а также `cpu-load-*`.

## Comparison

| Criteria | OpenSearch + Dashboards + Fluent Bit | Loki + Grafana + Promtail | ELK / Elastic Cloud | Docker logs only |
|---|:---:|:---:|:---:|:---:|
| Already present in repo deploy | ✅ | ❌ | ❌ | ✅ |
| Full-text search and filtering | ✅ | ⚠️ | ✅ | ❌ |
| Local MVP setup without external SaaS | ✅ | ✅ | ⚠️ | ✅ |
| Structured JSON log ingestion | ✅ | ✅ | ✅ | ❌ |
| Dashboards for support/debugging | ✅ | ✅ | ✅ | ❌ |
| Operational simplicity for current project | ✅ | ⚠️ | ❌ | ✅ |
| Sufficient for MVP audit-oriented logs | ✅ | ⚠️ | ✅ | ❌ |

## Decision

Использовать **OpenSearch + OpenSearch Dashboards + Fluent Bit** как observability-стек MVP.

Для MVP observability включает:

- structured application logs в stdout/stderr;
- доставку логов через Fluent Bit в OpenSearch;
- просмотр и поиск логов через OpenSearch Dashboards;
- health endpoint Fluent Bit на `:2020`;
- инфраструктурное логирование критичных событий доступа, дневника, программ и онбординга;
- correlation/request id в логах после появления backend API.

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

- Решение уже частично реализовано в `deploy/docker-compose.yml` и `deploy/volumes/fluent-bit-etc/fluent-bit.conf`, поэтому не требует смены инфраструктурного направления.
- OpenSearch подходит для поиска по событиям доступа, ошибкам API и техническим диагностическим данным.
- Fluent Bit лёгкий и достаточный для локального MVP-стенда; он уже принимает Docker fluentd logs и отправляет их в OpenSearch.
- Dashboards дают быстрый UI для поддержки пилотов без необходимости разрабатывать отдельную админку.
- Отделение infrastructure logs от будущего `AuditEvent` API сохраняет MVP scope и не смешивает технические логи с продуктовой аудиторской моделью.

## Required Log Fields

После появления backend API application logs должны быть структурированными JSON-событиями с минимальным набором полей:

| Field | Required | Description |
|---|:---:|---|
| `timestamp` | ✅ | Время события в UTC |
| `level` | ✅ | `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `service` | ✅ | Имя сервиса/контейнера |
| `environment` | ✅ | `local`, `dev`, `stage`, `prod` |
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

- MVP получает единый локальный путь для сбора и просмотра логов.
- Поддержка может расследовать ошибки приглашений, доступов, дневника и программ без прямого доступа к БД.
- Решение совместимо с текущим compose-стендом и не требует внешних SaaS.
- Structured logs создают основу для будущих метрик и алертов.

**Negative:**

- OpenSearch тяжелее, чем простой Docker logs или Loki-only стек.
- Retention, index lifecycle и alerting ещё нужно детализировать отдельно.
- Fluent Bit/OpenSearch credentials в текущем local compose являются dev-only и не подходят для production.
- Metrics/traces пока не покрыты полноценно; MVP начинается с logs-first observability.

**Risks:**

| Risk | Likelihood | Impact | Mitigation |
|---|---:|---:|---|
| В логи попадут персональные или health-adjacent данные | Medium | High | Ввести deny-list/маскирование, ревью log statements, негативные тесты на sensitive payloads |
| OpenSearch переполнится из-за отсутствия retention | Medium | Medium | Настроить index lifecycle/retention перед длительным пилотом |
| Нет requestId, сложно связывать события одного запроса | Medium | Medium | Добавить requestId middleware в Ktor backend первым observability task |
| Local credentials попадут в production | Low | High | Разделить local secrets и production secret management до production deploy |

## Заметки по реализации

- Для MVP backend должен логировать critical events из `RTM-013`: принятие/отклонение приглашения, выдача/отзыв доступа, запрос удаления/архивации профиля, создание/изменение/удаление дневника, назначение/изменение/отмена программы, выполнение тренировки и отказ доступа из-за scope.
- Fluent Bit текущего локального стенда уже настроен на `Logstash_Format On` и daily index prefix `app-logs`.
- Performance targets из NFR должны попадать в логи через `durationMs`; отдельная metrics-система может быть добавлена позже.
- Alerting не входит в текущий local MVP, но нарушение порогов latency/error rate должно быть возможно найти через OpenSearch queries.
- Для production потребуется отдельное решение по retention, index lifecycle, secrets и resource limits.
