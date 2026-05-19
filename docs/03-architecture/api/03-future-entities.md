# Phase 2 and Design Reserve API Entities

Сущности, которые описаны для будущей совместимости, но не входят в обязательный MVP / Gate 1.

**[Phase 2] Measurement (замер прогресса)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор замера | `mea_01HX7MBK9T` |
| clientProfileId | Владелец замера | `clp_01HX7M3B1K` |
| authorUserId | Кто внёс замер | `usr_01HX7M2A9Q` |
| measuredAt | Дата замера | `2026-05-19` |
| weightKg | Вес | `64.2` |
| bodyFatPercent | Процент жира, если известен | `22.5` |
| chestCm | Обхват груди | `88` |
| waistCm | Обхват талии | `70` |
| hipsCm | Обхват бёдер | `96` |
| customMetrics | Дополнительные метрики клиента | `{ "sleepHours": 7.5, "steps": 9200 }` |
| photos | Ссылки на фото прогресса с приватным доступом | `["file_01HX..."]` |
| notes | Комментарий к замеру | `"утром натощак"` |


## Phase 2 Analytics, Billing and Notifications

**[Phase 2] Report (аналитический отчёт)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор отчёта | `rep_01HX7MENBX` |
| clientProfileId | Клиент, по которому построен отчёт | `clp_01HX7M3B1K` |
| requestedByUserId | Пользователь, запросивший отчёт | `usr_01HX7M5D3M` |
| type | Тип: `WORKOUT_SUMMARY`, `ADHERENCE`, `MEASUREMENT_TREND`, `TRAINER_DASHBOARD` | `ADHERENCE` |
| periodFrom | Начало периода | `2026-05-01` |
| periodTo | Конец периода | `2026-05-31` |
| metrics | Рассчитанные показатели | `{ "planned": 12, "completed": 10, "adherence": 83 }` |
| format | Формат результата: `JSON`, `PDF`, `CSV` | `JSON` |
| status | Статус генерации: `READY`, `GENERATING`, `FAILED` | `READY` |
| generatedAt | Время генерации | `2026-05-19T12:00:00Z` |

**[Phase 2] Subscription (подписка и лимиты)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор подписки | `sub_01HX7M6E4N` |
| ownerUserId | Пользователь-плательщик | `usr_01HX7M5D3M` |
| ownerType | Тип владельца: `CLIENT`, `TRAINER`, `STUDIO` | `TRAINER` |
| tier | Тариф: `CLIENT_FREE`, `COACH_START`, `COACH_PRO`, `STUDIO_TEAM` | `COACH_START` |
| priceRub | Цена в рублях за месяц | `1490` |
| billingPeriod | Период оплаты: `MONTHLY`, `YEARLY` | `MONTHLY` |
| status | Статус: `TRIAL`, `ACTIVE`, `PAST_DUE`, `CANCELLED`, `EXPIRED` | `ACTIVE` |
| clientLimit | Лимит активных клиентов | `15` |
| teamMemberLimit | Лимит участников команды | `1` |
| autoPayEnabled | Автоплатёж включён | `true` |
| currentPeriodStart | Начало оплаченного периода | `2026-05-01` |
| currentPeriodEnd | Конец оплаченного периода | `2026-05-31` |

**[Phase 2] PaymentTransaction (платёжная операция)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор платежа | `pay_01HX7MFP6Y` |
| subscriptionId | Подписка, по которой проводится платёж | `sub_01HX7M6E4N` |
| provider | Платёжный провайдер | `yookassa` |
| amountRub | Сумма платежа | `1490` |
| status | Статус: `PENDING`, `SUCCEEDED`, `FAILED`, `REFUNDED` | `SUCCEEDED` |
| failureReason | Причина отказа, если есть | `insufficient_funds` |
| paidAt | Дата успешной оплаты | `2026-05-01T09:00:00Z` |
| nextRetryAt | Следующая попытка списания | `2026-05-02T09:00:00Z` |

**[Phase 2] Notification (системное уведомление)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор уведомления | `ntf_01HX7MGQ7Z` |
| recipientUserId | Получатель | `usr_01HX7M2A9Q` |
| type | Тип: `INVITE`, `ACCESS_REVOKED`, `PROGRAM_ASSIGNED`, `PAYMENT_FAILED`, `LIMIT_WARNING` | `PROGRAM_ASSIGNED` |
| channel | Канал: `IN_APP`, `EMAIL`, `SMS`, `PUSH` | `IN_APP` |
| title | Заголовок | `Вам назначена программа` |
| body | Текст уведомления | `Иван Петров назначил программу Старт 4 недели` |
| priority | Приоритет: `LOW`, `NORMAL`, `HIGH`, `CRITICAL` | `NORMAL` |
| payload | Бизнес-контекст для перехода в интерфейсе | `{ "assignmentId": "pas_01HX7MAJ8S" }` |
| status | Статус доставки: `QUEUED`, `SENT`, `DELIVERED`, `READ`, `FAILED` | `READ` |
| createdAt | Дата создания | `2026-05-19T12:10:00Z` |


## Design Reserve Entities

**[Design reserve] CollaborationRole (роль специалиста у клиента)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор роли специалиста | `col_01HX7MJS91` |
| clientProfileId | Клиент, вокруг которого формируется команда | `clp_01HX7M3B1K` |
| specialistProfileId | Тренер, нутрициолог, врач или иной специалист | `trp_01HX7M4C2L` |
| specialistType | Тип специалиста: `TRAINER`, `NUTRITIONIST`, `DOCTOR`, `PHYSIO` | `NUTRITIONIST` |
| scopes | Области доступа | `["MEASUREMENT_READ", "NOTE_WRITE"]` |
| coordinationMode | Режим совместной работы: `INDEPENDENT`, `SHARED_NOTES`, `CARE_TEAM` | `SHARED_NOTES` |
| status | Статус участия | `ACTIVE` |

**[Design reserve] StudioTeam (команда студии)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор студии или команды | `std_01HX7MDMBW` |
| ownerUserId | Владелец студии | `usr_01HX7MKTA2` |
| name | Название студии | `FitLab Moscow` |
| memberIds | Участники команды | `["stm_01HX7MMVB3"]` |
| roles | Ролевые политики команды | `{ "MANAGER": ["INVITE_MEMBER"], "TRAINER": ["WORK_WITH_CLIENT"] }` |
| subscriptionId | Командная подписка | `sub_01HX7MNVC4` |
| status | Статус команды | `ACTIVE` |

