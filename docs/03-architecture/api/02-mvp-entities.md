# MVP API Entities

Сущности, которые нужны для Gate 1: профили, доступ, приглашение, дневник, простой план, назначение и аудит.

**[MVP] User (учётная запись Keycloak)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Глобальный идентификатор пользователя в FitBridge | `usr_01HX7M2A9Q` |
| keycloakSubject | Идентификатор субъекта в Keycloak; является источником аутентификации | `3f8d9f3a-7b2b-4f0b-9e2b-8b4d` |
| email | Подтверждённый email для входа и приглашений; биллинг использует поле в Phase 2 | `client@example.com` |
| phone | Телефон для уведомлений и восстановления доступа | `+79991234567` |
| displayName | Отображаемое имя пользователя | `Анна Иванова` |
| roles | Роли в системе: `CLIENT`, `TRAINER`, `STUDIO_OWNER`, `TEAM_MEMBER`, `ADMIN` | `["CLIENT", "TRAINER"]` |
| status | Состояние учётной записи: `ACTIVE`, `PENDING_EMAIL`, `BLOCKED`, `DELETED` | `ACTIVE` |
| locale | Предпочитаемый язык интерфейса и уведомлений | `ru-RU` |
| timezone | Часовой пояс пользователя | `Europe/Moscow` |
| createdAt | Дата регистрации | `2026-05-19T10:00:00Z` |
| lastLoginAt | Последний успешный вход | `2026-05-19T12:30:00Z` |

**[MVP] ClientProfile (клиентский профиль и история тренировок)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор профиля клиента | `clp_01HX7M3B1K` |
| userId | Владелец профиля; только он управляет доступом | `usr_01HX7M2A9Q` |
| fullName | Имя клиента для тренерского интерфейса | `Анна Иванова` |
| birthDate | Дата рождения; Phase 2 использует для отчётов и расширенной аналитики | `1992-04-12` |
| gender | Самоопределённый пол или `NOT_SPECIFIED` | `FEMALE` |
| heightCm | Рост в сантиметрах | `168` |
| goals | Цели клиента | `["снижение веса", "выносливость"]` |
| restrictions | Phase 2: медицинские или тренировочные ограничения, заполняемые клиентом | `"не нагружать колено"` |
| visibility | Видимость профиля: `PRIVATE`, `GRANTED_ONLY` | `GRANTED_ONLY` |
| activeTrainerIds | Тренеры с действующим доступом | `["trp_01HX7M4C2L"]` |
| createdAt | Дата создания профиля | `2026-05-19T10:05:00Z` |
| updatedAt | Дата последнего изменения | `2026-05-19T11:00:00Z` |

**[MVP] TrainerProfile (профессиональный профиль тренера)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор профиля тренера | `trp_01HX7M4C2L` |
| userId | Пользователь, владеющий тренерским профилем | `usr_01HX7M5D3M` |
| publicName | Имя, отображаемое клиентам в приглашениях | `Иван Петров` |
| specialization | Специализации тренера | `["силовой тренинг", "реабилитация"]` |
| bio | Краткое описание опыта | `"8 лет практики"` |
| certificates | Список сертификатов или ссылок на документы | `["FPA-2025"]` |
| contactPolicy | Правила показа контактов клиентам | `AFTER_ACCEPT_INVITE` |
| onboardingStatus | Статус онбординга: `NEW`, `PROFILE_READY`, `FIRST_CLIENT_INVITED`, `COMPLETED` | `FIRST_CLIENT_INVITED` |
| dashboardCounters | Агрегированные счётчики минимального кабинета | `{ "activeClients": 12, "pendingInvites": 2 }` |
| subscriptionId | Phase 2: активная подписка тренера или студии | `sub_01HX7M6E4N` |
| createdAt | Дата создания профиля | `2026-05-19T09:00:00Z` |

**[MVP] AccessGrant (разрешение на доступ к клиентским данным)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор разрешения | `agr_01HX7M7F5P` |
| clientProfileId | Профиль клиента, к которому выдаётся доступ | `clp_01HX7M3B1K` |
| trainerProfileId | Тренер или специалист, получающий доступ | `trp_01HX7M4C2L` |
| grantedByUserId | Пользователь, выдавший доступ; для MVP всегда клиент | `usr_01HX7M2A9Q` |
| status | Статус: `PENDING`, `ACTIVE`, `REVOKED`, `EXPIRED`, `DECLINED` | `ACTIVE` |
| scopes | MVP: профиль, дневник и планы; Phase 2: замеры, отчёты и расширенные scopes | `["DIARY_READ", "PROGRAM_WRITE"]` |
| reason | Phase 2: комментарий клиента или тренера к выдаче доступа | `"подготовка к марафону"` |
| expiresAt | Phase 2: дата автоматического истечения доступа, если задана | `2026-11-19T00:00:00Z` |
| grantedAt | Момент активации доступа | `2026-05-19T10:10:00Z` |
| revokedAt | Момент отзыва доступа | `null` |

**[MVP] Invite (приглашение клиента)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор приглашения | `inv_01HX7M8G6Q` |
| type | MVP: `TRAINER_TO_CLIENT`; Phase 2/design reserve: `CLIENT_TO_TRAINER`, `TEAM_MEMBER`, `SPECIALIST` | `TRAINER_TO_CLIENT` |
| senderUserId | Отправитель приглашения | `usr_01HX7M5D3M` |
| recipientEmail | Email получателя, если пользователь ещё не зарегистрирован | `client@example.com` |
| recipientUserId | Идентификатор получателя, если он уже зарегистрирован | `usr_01HX7M2A9Q` |
| targetClientProfileId | Профиль клиента, к которому относится приглашение | `clp_01HX7M3B1K` |
| targetTrainerProfileId | Профиль тренера, если приглашение от тренера | `trp_01HX7M4C2L` |
| proposedScopes | Запрашиваемые области доступа; MVP использует профиль, дневник и планы | `["DIARY_READ", "PROGRAM_READ"]` |
| tokenHash | Хэш одноразового токена приглашения | `sha256:...` |
| status | Статус: `PENDING`, `ACCEPTED`, `DECLINED`, `EXPIRED`, `CANCELLED` | `PENDING` |
| expiresAt | Дата истечения приглашения | `2026-05-26T10:00:00Z` |

**[MVP] TrainingEntry (запись тренировочного дневника)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор записи дневника | `ten_01HX7M9H7R` |
| clientProfileId | Владелец записи | `clp_01HX7M3B1K` |
| authorUserId | Автор: клиент или тренер с правом записи | `usr_01HX7M2A9Q` |
| type | MVP: `WORKOUT`, `REST_DAY`; Phase 2: `NOTE`, `CHECK_IN` | `WORKOUT` |
| occurredAt | Дата и время события | `2026-05-19T07:30:00Z` |
| title | Краткое название | `Ноги и корпус` |
| exercises | Упражнения, подходы, повторы, вес, RPE; MVP допускает упрощённую структуру | `[{"name":"присед", "sets":3, "reps":8, "weightKg":60}]` |
| durationMinutes | Длительность тренировки | `65` |
| intensity | Субъективная интенсивность 1-10 | `7` |
| mood | Самочувствие: `GREAT`, `OK`, `TIRED`, `PAIN` | `OK` |
| notes | Свободный текст клиента или тренера | `"последний подход тяжело"` |
| source | Источник: `MANUAL`, `PROGRAM_ASSIGNMENT`, `IMPORT` | `MANUAL` |
| linkedProgramAssignmentId | Связь с назначенной программой | `pas_01HX7MAJ8S` |
| visibility | Видимость записи в рамках активных доступов | `GRANTED_TRAINERS` |
| updatedAt | Дата последнего изменения | `2026-05-19T08:45:00Z` |


## MVP Plan Entities

**[MVP] Program (простой тренировочный план; шаблоны — Phase 2)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор программы | `prg_01HX7MCLAV` |
| ownerTrainerProfileId | Тренер-владелец программы | `trp_01HX7M4C2L` |
| studioId | Design reserve: студия-владелец, если программа командная | `std_01HX7MDMBW` |
| type | MVP: `PERSONAL_PROGRAM`; Phase 2: `TEMPLATE` | `PERSONAL_PROGRAM` |
| title | Название программы | `Старт 4 недели` |
| goal | Цель программы | `адаптация к силовым тренировкам` |
| difficulty | Уровень: `BEGINNER`, `INTERMEDIATE`, `ADVANCED` | `BEGINNER` |
| durationWeeks | Плановая длительность | `4` |
| workouts | Структура недель, тренировок и упражнений | `[{"week":1,"day":1,"title":"Full body"}]` |
| tags | Phase 2: теги для поиска шаблонов | `["зал", "новичок"]` |
| status | Статус: `DRAFT`, `ACTIVE`, `ARCHIVED` | `ACTIVE` |
| version | Версия программы для безопасного назначения | `3` |
| createdAt | Дата создания | `2026-05-19T09:20:00Z` |

**[MVP] ProgramAssignment (назначение плана клиенту)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор назначения | `pas_01HX7MAJ8S` |
| programId | Назначенная программа | `prg_01HX7MCLAV` |
| clientProfileId | Клиент-получатель | `clp_01HX7M3B1K` |
| assignedByTrainerProfileId | Тренер, назначивший программу | `trp_01HX7M4C2L` |
| accessGrantId | Действующий доступ, на основании которого назначена программа | `agr_01HX7M7F5P` |
| startDate | Дата начала | `2026-05-20` |
| endDate | Плановая дата окончания | `2026-06-16` |
| status | Статус: `PLANNED`, `ACTIVE`, `PAUSED`, `COMPLETED`, `CANCELLED` | `ACTIVE` |
| completionPercent | MVP: простой процент выполнения по назначению | `37` |
| adherencePercent | Phase 2: соблюдение расписания | `82` |
| currentWeek | Текущая неделя | `2` |
| clientFeedback | Последняя обратная связь клиента | `"сложно в день 3"` |


## MVP Audit Entity

**[MVP] AuditEvent (журнал действий)**

| Поле | Бизнес-описание | Пример значения |
|------|-----------------|-----------------|
| id | Идентификатор события аудита | `aud_01HX7MHR80` |
| actorUserId | Пользователь или системный процесс, выполнивший действие | `usr_01HX7M5D3M` |
| actorRole | Роль субъекта в момент действия | `TRAINER` |
| action | Действие в формате `domain.action` | `access.grant` |
| entityType | Тип затронутой сущности | `AccessGrant` |
| entityId | Идентификатор затронутой сущности | `agr_01HX7M7F5P` |
| clientProfileId | Клиентский профиль, если событие касается персональных данных | `clp_01HX7M3B1K` |
| before | Снимок значимых полей до изменения | `{ "status": "PENDING" }` |
| after | Снимок значимых полей после изменения | `{ "status": "ACTIVE" }` |
| ipAddress | IP-адрес источника | `203.0.113.10` |
| userAgent | Клиентское приложение | `FitBridge Web/1.0` |
| createdAt | Время события | `2026-05-19T10:10:01Z` |

