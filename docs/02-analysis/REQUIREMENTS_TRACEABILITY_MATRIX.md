# Requirements Traceability Matrix

Документ связывает бизнес-требования FitBridge с MVP scope, функциональными и нефункциональными требованиями, API-методами, acceptance criteria и будущими приёмочными тестами.

## Статус

| Параметр | Значение |
|---|---|
| Статус | Draft |
| Область | MVP / Gate 1 |
| MVP-пути | Trainer-led и Solo-client (PLG) |
| Источники | `docs/01-business`, `docs/02-analysis`, `docs/03-architecture/api` |

## Правила чтения

| Поле | Значение |
|---|---|
| Trace ID | Стабильный идентификатор строки трассировки |
| MVP capability | Проверяемая возможность MVP |
| MVP path | `Trainer-led`, `Solo-client` или `Both` |
| BR / AC | Бизнес-требование, user story и acceptance criteria |
| FR / NFR | Ссылки на функциональные и нефункциональные требования по номерам разделов |
| API | Методы из `docs/03-architecture/api` |
| Tests | Планируемые приёмочные тесты. Реализация тестов появится после появления backend-кода |
| Status | Готовность трассировки: `Ready`, `Needs detail`, `Gap` |

## MVP Traceability

| Trace ID | MVP capability | MVP path | BR / AC | FR / NFR | API | Tests | Status |
|---|---|---|---|---|---|---|---|
| RTM-001 | Регистрация клиента и создание клиентского профиля | Both | [BR-001](../01-business/BR/BR-001-training-diary.md), `US-R001-001`, `AC-R001-U001-001` | FR `1.1`, `2.1`; NFR `3.1`, `3.2`, `5.2`, `8.1` | `profile.createClientProfile`, `profile.readClientProfile`, `profile.updateClientProfile` | `TEST-E2E-001`: новый клиент регистрируется и получает пустой клиентский профиль | Ready |
| RTM-002 | Регистрация тренера и старт онбординга | Trainer-led | [BR-006](../01-business/BR/BR-006-trainer-onboarding.md), `US-R006-001`, `AC-R006-U001-001` | FR `1.2`, `6.1`, `6.4`; NFR `1.6`, `6.1`, `10.3` | `profile.createTrainerProfile`, `profile.readTrainerProfile`, `profile.updateTrainerProfile`, `onboarding.startTrainerWizard`, `onboarding.completeTrainerWizard` | `TEST-E2E-002`: тренер создаёт профиль и переходит к мастеру первого клиента | Ready |
| RTM-003 | Создание и принятие приглашения клиента | Trainer-led | [BR-002](../01-business/BR/BR-002-access-control.md), `AC-R002-U001-001`; [BR-006](../01-business/BR/BR-006-trainer-onboarding.md), `AC-R006-U001-001` | FR `3.1`, `6.2`, `8.1`; NFR `1.2`, `2.2`, `3.3`, `6.2`, `10.2` | `access.createInvite`, `access.readInvite`, `access.acceptInvite`, `access.declineInvite`, `access.validateScope` | `TEST-E2E-003`: тренер создаёт приглашение, клиент принимает его и явно подтверждает доступ; `TEST-NEG-001`: истёкшее приглашение нельзя принять | Needs detail |
| RTM-004 | Отзыв доступа и сохранение истории клиента | Trainer-led | [BR-002](../01-business/BR/BR-002-access-control.md), `US-R002-001`, `US-R002-002`, `AC-R002-U001-002`, `AC-R002-U002-001` | FR `3.2`, `3.3`, `3.4`; NFR `1.2`, `2.2`, `2.3`, `3.3`, `3.4`, `9.2` | `access.revoke`, `access.readGrant`, `access.listGrants`, `access.validateScope` | `TEST-E2E-004`: клиент отзывает доступ, тренер теряет доступ, история остаётся у клиента; `TEST-NEG-002`: тренер с отозванным доступом получает отказ | Ready |
| RTM-005 | Ведение базового дневника клиентом | Both | [BR-001](../01-business/BR/BR-001-training-diary.md), `US-R001-001`, `US-R001-002`, `AC-R001-U001-002`, `AC-R001-U001-003`, `AC-R001-U002-001` | FR `2.2`, `2.5`, `5.1`; NFR `1.1`, `2.1`, `4.2`, `5.2`, `6.3`, `10.2` | `diary.createEntry`, `diary.readEntry`, `diary.updateEntry`, `diary.deleteEntry`, `diary.searchEntries` | `TEST-E2E-005`: клиент создаёт, открывает и редактирует запись дневника; `TEST-E2E-006`: solo-клиент ведёт дневник без тренера | Ready |
| RTM-006 | Самостоятельный MVP-путь клиента с личной программой | Solo-client | [BR-001](../01-business/BR/BR-001-training-diary.md), `AC-R001-U001-003`; [BR-004](../01-business/BR/BR-004-program-builder.md), `US-R004-003`, `AC-R004-U003-001`, `AC-R004-U003-002` | FR `2.5`, `4.1`, `4.4`, `5.1`; NFR `1.1`, `1.3`, `5.2`, `6.3`, `10.3` | `program.create`, `program.assign`, `program.readAssignment`, `program.completeWorkout`, `program.trackCompletion`, `diary.createEntry`, `diary.searchEntries` | `TEST-E2E-007`: solo-клиент создаёт личную программу, назначает её себе и отмечает выполнение | Ready |
| RTM-007 | Создание и назначение программы тренером | Trainer-led | [BR-004](../01-business/BR/BR-004-program-builder.md), `US-R004-001`, `US-R004-002`, `AC-R004-U001-001`, `AC-R004-U001-002`, `AC-R004-U002-001`; [BR-006](../01-business/BR/BR-006-trainer-onboarding.md), `AC-R006-U002-002` | FR `4.1`, `4.3`, `4.4`, `6.4`; NFR `1.3`, `2.1`, `5.3`, `10.2` | `program.create`, `program.read`, `program.update`, `program.delete`, `program.assign`, `program.readAssignment`, `program.updateAssignment`, `program.deleteAssignment` | `TEST-E2E-008`: тренер создаёт план и назначает его клиенту; `TEST-NEG-003`: тренер без `PROGRAM_WRITE` не может назначить программу | Needs detail |
| RTM-008 | Отметка выполнения тренировки по программе | Both | [BR-004](../01-business/BR/BR-004-program-builder.md), `AC-R004-U001-003`; [BR-005](../01-business/BR/BR-005-progress-analytics.md), `AC-R005-U002-001` | FR `4.4`, `5.1`, `8.3`; NFR `1.1`, `1.4`, `2.1`, `10.2` | `program.completeWorkout`, `program.trackCompletion`, `diary.createEntry`, `diary.searchEntries` | `TEST-E2E-009`: клиент отмечает тренировку выполненной, статус виден клиенту и тренеру с активным доступом | Ready |
| RTM-009 | Минимальный список клиентов и карточка клиента тренера | Trainer-led | [BR-006](../01-business/BR/BR-006-trainer-onboarding.md), `US-R006-002`, `AC-R006-U002-001`, `AC-R006-U002-002`; [BR-005](../01-business/BR/BR-005-progress-analytics.md), `AC-R005-U001-001` | FR `5.1`, `6.3`, `8.3`; NFR `1.4`, `5.3`, `6.1`, `10.1`, `10.3` | `dashboard.getTrainerSummary`, `access.listGrants`, `diary.searchEntries`, `program.trackCompletion` | `TEST-E2E-010`: тренер видит список клиентов, статусы приглашений/доступов, назначенный план и историю выполнения | Needs detail |
| RTM-010 | Просмотр клиентом собственной истории и прогресса | Both | [BR-001](../01-business/BR/BR-001-training-diary.md), `AC-R001-U002-001`, `AC-R001-U002-002`; [BR-005](../01-business/BR/BR-005-progress-analytics.md), `US-R005-002`, `AC-R005-U002-001` | FR `5.1`, `2.5`; NFR `1.4`, `4.2`, `5.2`, `6.3` | `diary.searchEntries`, `program.trackCompletion`, `program.readAssignment` | `TEST-E2E-011`: клиент открывает историю и видит выполненные/пропущенные тренировки за период | Ready |
| RTM-011 | Pull-model уведомления и статусы событий MVP | Both | [BR-002](../01-business/BR/BR-002-access-control.md), `AC-R002-U001-001`; [BR-004](../01-business/BR/BR-004-program-builder.md), `AC-R004-U001-002`; [BR-006](../01-business/BR/BR-006-trainer-onboarding.md), `AC-R006-U002-001` | FR `8.1`, `8.2`, `8.3`, `8.5`; NFR `10.4` | `access.readInvite`, `access.listGrants`, `program.readAssignment`, `dashboard.getTrainerSummary` | `TEST-E2E-012`: пользователь видит статусы приглашения, назначения программы и выполнения через существующие read endpoints/dashboard; отдельный `Notification` API/provider проверяется как Phase 2/out of MVP | Ready |
| RTM-012 | Управление профилем, согласиями и удалением | Both | [BR-009](../01-business/BR/BR-009-consent-privacy-deletion.md), `US-R009-001`, `US-R009-002`, `US-R009-003` | FR `1.1`, `3.6`; NFR `3.1`, `3.5`, `8.1`, `8.2`, `8.3`, `10.2` | `profile.deleteClientProfile`, `profile.deleteTrainerProfile`, `access.revoke`, `access.validateScope` | `TEST-E2E-013`: пользователь запрашивает удаление/архивацию профиля, активные доступы блокируются; `TEST-NEG-004`: после удаления/архивации тренер не видит данные; `TEST-OBS-002`: чувствительные payload не попадают в логи | Ready |
| RTM-013 | Инфраструктурный аудит критичных действий | Both | [BR-002](../01-business/BR/BR-002-access-control.md); [BR-006](../01-business/BR/BR-006-trainer-onboarding.md); [BR-009](../01-business/BR/BR-009-consent-privacy-deletion.md); API business rule `8`; [ADR-006](../03-architecture/ADR/ADR-006-use-opensearch-fluent-bit-observability.md) | FR none; NFR `3.4`, `10.2` | Cross-cutting для `access.*`, `profile.*`, `diary.*`, `program.*`; без продуктового `AuditEvent` API в MVP | `TEST-OBS-001`: accept/decline invite, grant/revoke access, profile delete/archive request, diary create/update/delete, program assign/update/cancel, complete workout и access denied for scope логируются как masked structured logs без раскрытия лишних персональных данных | Ready |
| RTM-014 | Резервное копирование и восстановление клиентской истории | Both | [BR-001](../01-business/BR/BR-001-training-diary.md); [BR-002](../01-business/BR/BR-002-access-control.md); [BR-005](../01-business/BR/BR-005-progress-analytics.md) | FR none; NFR `9.1`, `9.2`, `9.3` | Нет публичного API; инфраструктурный контур | `TEST-OPS-001`: восстановление тестового набора профиля, истории, доступов и назначений из резервной копии | Gap |
| RTM-015 | Ручная проверка платёжной гипотезы без продуктового биллинга | Trainer-led | [BR-007](../01-business/BR/BR-007-billing-subscriptions.md), metrics lines `55-58` | FR `7.1`, `7.2`, `7.3`, `7.4` marked Phase 2; NFR `10.3` | Нет API в MVP | `TEST-BIZ-001`: не менее 5 пилотных тренеров подтверждают willingness-to-pay после прохождения критического пути | Ready |

## Coverage By MVP Path

| MVP path | Covered traces | Gate 1 acceptance focus |
|---|---|---|
| Trainer-led | `RTM-001`, `RTM-002`, `RTM-003`, `RTM-004`, `RTM-005`, `RTM-007`, `RTM-008`, `RTM-009`, `RTM-010`, `RTM-011`, `RTM-012`, `RTM-013`, `RTM-014`, `RTM-015` | Тренер регистрируется, приглашает клиента, клиент подтверждает доступ, тренер назначает план, клиент отмечает выполнение, тренер видит историю и статусы |
| Solo-client | `RTM-001`, `RTM-005`, `RTM-006`, `RTM-008`, `RTM-010`, `RTM-011`, `RTM-012`, `RTM-013`, `RTM-014` | Клиент регистрируется самостоятельно, ведёт дневник, создаёт личную программу, назначает её себе и отмечает выполнение |

## Известные разрывы и заметки поддержки

| Gap | Impact | Recommended action |
|---|---|---|
| Расширенная consent/data classification matrix пока не выделена отдельным документом | MVP-трассировка закрыта через `BR-009`, но Phase 2 granular consent потребует детализации | Поддерживать `BR-009` и перед Phase 2 добавить матрицу типов данных, оснований обработки и сроков хранения |
| Нет машинно-проверяемых API contracts | Тесты пока можно трассировать только до markdown-методов | Добавить OpenAPI/JSON Schema или единый request/response/error contract |
| Нет backend-кода MVP | Тесты в матрице имеют статус планируемых, не реализованных | После появления модулей добавить ссылки на test classes/methods |
| Audit и notification scope синхронизированы | Scope зафиксирован в FR/NFR/API/ERD/ADR-006: MVP = infrastructure audit-oriented logging и pull-model UI; Phase 2 = `AuditEvent` и `Notification` API/provider | Поддерживать эту границу при изменениях BR, API и roadmap |

## Maintenance Rules

1. Каждое новое MVP-требование должно получить строку `RTM-*`.
2. Каждая строка `RTM-*` должна ссылаться минимум на один BR или явно помеченный gap.
3. После появления тестов колонка `Tests` должна содержать реальные test class/method references вместо плановых `TEST-*`.
4. Phase 2 требования остаются в матрице только если они влияют на MVP как constraint, gap или manual validation.
