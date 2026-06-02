# ADR-005: Использовать PostgreSQL как основное хранилище приложения

**Статус:** Accepted  
**Дата:** 2026-05-21

## Контекст

Trainer Diary MVP FitBridge хранит trainer-owned рабочий контур: зарегистрированного тренера, профиль тренера, клиентские карточки и простые тренировочные планы. Ключевые требования к хранилищу:

- строгие связи между `FITBRIDGE_USER`, `TRAINER_PROFILE`, `CLIENT_CARD` и `TRAINING_PLAN`;
- транзакционная корректность создания, изменения и архивирования карточек и планов;
- фильтрация списков и чтения по `trainerUserId` для защиты trainer-owned данных;
- поддержка `RPO <= 24h` и восстановления критичных данных;
- возможность хранить структурированные, но ещё эволюционирующие поля тренировок и программ;
- хорошая совместимость с Kotlin/JVM и Ktor.

## Сравнение

| Criteria | PostgreSQL | MongoDB | Cassandra | In-memory storage |
|---|:---:|:---:|:---:|:---:|
| Relational integrity for trainer/profile/card/plan | ✅ | ⚠️ | ❌ | ❌ |
| Transactions for create/update/archive flows | ✅ | ⚠️ | ❌ | ⚠️ |
| Flexible workout/program payloads | ✅ JSONB | ✅ | ⚠️ | ✅ |
| Backup, PITR and operational maturity | ✅ | ✅ | ⚠️ | ❌ |
| Kotlin/JVM ecosystem support | ✅ JDBC/R2DBC/Exposed/jOOQ | ✅ | ⚠️ | ✅ |
| Fit for MVP complexity | ✅ | ⚠️ | ❌ | ❌ |
| Future analytics/read models | ✅ | ⚠️ | ✅ | ❌ |

## Решение

Использовать **PostgreSQL** как основное прикладное хранилище FitBridge MVP.

Для MVP PostgreSQL хранит:

- внутреннюю проекцию пользователя Keycloak (`FITBRIDGE_USER`);
- профиль тренера;
- клиентские карточки `ClientCard`;
- планы `TrainingPlan` с `clientCardId`, `trainerUserId`, статусом и версией;
- soft-delete/archive состояния и технические timestamps.

Поля с ещё нестабильной внутренней структурой, например структура недель/тренировок плана, допускается хранить в `JSONB` при наличии явных ограничений на размер, валидации на уровне приложения и индексов только под подтверждённые запросы.

## Обоснование

- Домен Trainer Diary MVP реляционный: профиль принадлежит тренеру, карточки принадлежат тренеру, планы принадлежат карточкам и тренеру.
- Операции create/update/archive требуют ACID-транзакций и согласованной проверки ownership.
- PostgreSQL покрывает базовые требования к резервному копированию, восстановлению, индексам, уникальным ограничениям и partial indexes для правил trainer-owned поиска.
- `JSONB` даёт достаточную гибкость для простых программ и тренировочных payloads без преждевременного вынесения упражнения/подходов в сложную нормализованную модель.
- PostgreSQL хорошо поддерживается JVM/Kotlin-экосистемой и не конфликтует с будущей модульной структурой `repo-pgjvm`.

## Последствия

**Positive:**

- Упрощается обеспечение консистентности trainer-owned данных.
- Можно реализовать archive, уникальные ограничения, foreign keys и transactional close/revoke без дополнительной инфраструктуры.
- Есть понятный путь к backup/PITR и выполнению RPO/RTO требований.
- Можно начать с одной прикладной БД и позже добавить read models или analytics storage без смены основного хранилища.

**Negative:**

- Нужны миграции схемы и дисциплина изменения JSONB-структур.
- Для аналитических сценариев Phase 2 может понадобиться отдельная витрина или специализированное хранилище.
- Неправильное использование JSONB может ухудшить читаемость модели и производительность запросов.

**Risks:**

| Risk | Likelihood | Impact | Mitigation |
|---|---:|---:|---|
| JSONB станет “свалкой” без контрактов | Medium | High | Валидировать payloads в API, документировать schema fragments, индексировать только подтверждённые запросы |
| Ошибки в ownership/index constraints | Medium | High | Использовать транзакции, foreign keys, индексы по `trainerUserId`, негативные тесты на доступ к чужим ресурсам |
| Backup настроен формально, но восстановление не проверяется | Medium | High | Ввести регулярную проверку восстановления тестового набора данных по NFR `9.3` |
| PostgreSQL будет перегружен аналитикой Phase 2 | Low | Medium | Выносить тяжёлые отчёты в read model/analytics storage после подтверждения нагрузки |

## Заметки по реализации

- Для MVP использовать одну прикладную PostgreSQL database/schema для FitBridge domain data.
- Keycloak остаётся владельцем authentication data; FitBridge хранит только `keycloakSubject` и доменную проекцию пользователя.
- Для поиска предусмотреть индексы по `CLIENT_CARD.trainerUserId/status/displayName` и `TRAINING_PLAN.trainerUserId/clientCardId/status/title`.
- Для archive использовать `archivedAt` и бизнес-фильтры, а не физическое удаление в пользовательском сценарии.
- Для `TRAINING_PLAN.planBody` зафиксировать application-level validation и ограничения размера до появления более детализированной схемы упражнений.
- Future `ClientProfile`, `Invite`, `AccessGrant`, `TrainingEntry`, `ProgramAssignment` остаются Phase 2 и могут использовать PostgreSQL без смены основного хранилища.
