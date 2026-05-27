# ADR-005: Использовать PostgreSQL как основное хранилище приложения

**Статус:** Accepted  
**Дата:** 2026-05-21

## Контекст

Trainer-first MVP с публичной ссылкой FitBridge хранит trainer-owned рабочий контур: зарегистрированного тренера, клиентские карточки, простые планы, technical public-access state и отметки выполнения по публичной ссылке. Ключевые требования к хранилищу:

- строгие связи между `FITBRIDGE_USER`, `TRAINER_PROFILE`, `CLIENT_CARD`, `TRAINING_PLAN` и `COMPLETION_MARK`;
- транзакционная корректность public-link lifecycle: создать hash token, выставить TTL, закрыть/revoke ссылку, запретить новые public opens/marks;
- сохранение отметок выполнения как основы будущей миграции в client-owned историю;
- поддержка `RPO <= 24h` и восстановления критичных данных;
- возможность хранить структурированные, но ещё эволюционирующие поля тренировок и программ;
- хорошая совместимость с Kotlin/JVM и Ktor.

## Сравнение

| Criteria | PostgreSQL | MongoDB | Cassandra | In-memory storage |
|---|:---:|:---:|:---:|:---:|
| Relational integrity for trainer/card/plan/completion | ✅ | ⚠️ | ❌ | ❌ |
| Transactions for public-link generate/close flows | ✅ | ⚠️ | ❌ | ⚠️ |
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
- планы `TrainingPlan` с hash-only public-access state;
- отметки выполнения `CompletionMark` или value-object/jsonb представление;
- soft-delete/archive состояния и технические timestamps.

Поля с ещё нестабильной внутренней структурой, например структура недель/тренировок плана и completion marks, допускается хранить в `JSONB` при наличии явных ограничений на размер, валидации на уровне приложения и индексов только под подтверждённые запросы.

## Обоснование

- Домен MVP публичного доступа к плану реляционный: карточки принадлежат тренеру, планы принадлежат карточкам, отметки выполнения принадлежат планам, а public-access state должен быть консистентным.
- Операции public access требуют ACID-транзакций: `generatePublicLink` должен атомарно сохранить hash/TTL/status, а `closePublicLink` должен немедленно блокировать дальнейшие public opens/marks.
- PostgreSQL покрывает базовые требования к резервному копированию, восстановлению, индексам, уникальным ограничениям и partial indexes для правил public-access lifecycle.
- `JSONB` даёт достаточную гибкость для простых программ и тренировочных payloads без преждевременного вынесения упражнения/подходов в сложную нормализованную модель.
- PostgreSQL хорошо поддерживается JVM/Kotlin-экосистемой и не конфликтует с будущей модульной структурой `repo-pgjvm`.

## Последствия

**Positive:**

- Упрощается обеспечение консистентности trainer-owned данных и public-link lifecycle.
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
| Ошибки в public-link constraints | Medium | High | Использовать транзакции, foreign keys, unique/hash indexes, негативные тесты на expired/revoked token |
| Backup настроен формально, но восстановление не проверяется | Medium | High | Ввести регулярную проверку восстановления тестового набора данных по NFR `9.3` |
| PostgreSQL будет перегружен аналитикой Phase 2 | Low | Medium | Выносить тяжёлые отчёты в read model/analytics storage после подтверждения нагрузки |

## Заметки по реализации

- Для MVP использовать одну прикладную PostgreSQL database/schema для FitBridge domain data.
- Keycloak остаётся владельцем authentication data; FitBridge хранит только `keycloakSubject` и доменную проекцию пользователя.
- Для public access предусмотреть уникальный индекс по `TRAINING_PLAN.publicAccessTokenHash` и обязательные проверки `publicAccessStatus`, `publicAccessExpiresAt`, `publicAccessRevokedAt`.
- Для archive использовать `archivedAt` и бизнес-фильтры, а не физическое удаление в пользовательском сценарии.
- Для `TRAINING_PLAN.planBody` и `CompletionMark`/`completionMarksJson` зафиксировать application-level validation до появления машинно-проверяемых API contracts.
- Future `ClientProfile`, `Invite`, `AccessGrant`, `TrainingEntry`, `ProgramAssignment` остаются Phase 2 и могут использовать PostgreSQL без смены основного хранилища.
