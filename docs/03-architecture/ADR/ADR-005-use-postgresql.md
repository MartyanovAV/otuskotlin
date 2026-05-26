# ADR-005: Использовать PostgreSQL как основное хранилище приложения

**Статус:** Accepted  
**Дата:** 2026-05-21

## Context

MVP FitBridge хранит client-owned data: профили клиентов и тренеров, приглашения, разрешения доступа, дневник тренировок, простые программы и назначения программ. Ключевые требования к хранилищу:

- строгие связи между `ClientProfile`, `TrainerProfile`, `AccessGrant`, `Invite`, `TrainingEntry`, `Program` и `ProgramAssignment`;
- транзакционная корректность операций доступа: принять приглашение, выдать доступ, отозвать доступ, запретить дальнейшие действия тренера;
- сохранение истории клиента при смене тренера и отзыве доступа;
- поддержка `RPO <= 24h` и восстановления критичных данных;
- возможность хранить структурированные, но ещё эволюционирующие поля тренировок и программ;
- хорошая совместимость с Kotlin/JVM и Ktor.

## Comparison

| Criteria | PostgreSQL | MongoDB | Cassandra | In-memory storage |
|---|:---:|:---:|:---:|:---:|
| Relational integrity for grants, profiles, assignments | ✅ | ⚠️ | ❌ | ❌ |
| Transactions for invite/access/revoke flows | ✅ | ⚠️ | ❌ | ⚠️ |
| Flexible workout/program payloads | ✅ JSONB | ✅ | ⚠️ | ✅ |
| Backup, PITR and operational maturity | ✅ | ✅ | ⚠️ | ❌ |
| Kotlin/JVM ecosystem support | ✅ JDBC/R2DBC/Exposed/jOOQ | ✅ | ⚠️ | ✅ |
| Fit for MVP complexity | ✅ | ⚠️ | ❌ | ❌ |
| Future analytics/read models | ✅ | ⚠️ | ✅ | ❌ |

## Decision

Использовать **PostgreSQL** как основное прикладное хранилище FitBridge MVP.

Для MVP PostgreSQL хранит:

- внутреннюю проекцию пользователя Keycloak (`FITBRIDGE_USER`);
- клиентские и тренерские профили;
- приглашения и доступы;
- дневниковые записи;
- программы и назначения;
- soft-delete/archive состояния и технические timestamps.

Поля с ещё нестабильной внутренней структурой, например упражнения, структура недель/тренировок, цели или scopes, допускается хранить в `JSONB` при наличии явных ограничений на размер, валидации на уровне приложения и индексов только под подтверждённые запросы.

## Rationale

- Домен MVP реляционный: доступ тренера зависит от пары клиент-тренер, назначения программы зависят от клиента, программы и действующего доступа, а история должна оставаться у клиента независимо от тренера.
- Операции доступа требуют ACID-транзакций: `acceptInvite` должен атомарно менять статус приглашения и создавать/активировать `AccessGrant`; `revoke` должен немедленно блокировать дальнейшие действия тренера.
- PostgreSQL покрывает базовые требования к резервному копированию, восстановлению, индексам, уникальным ограничениям и partial indexes для правил вроде “один активный тренер в MVP”.
- `JSONB` даёт достаточную гибкость для простых программ и тренировочных payloads без преждевременного вынесения упражнения/подходов в сложную нормализованную модель.
- PostgreSQL хорошо поддерживается JVM/Kotlin-экосистемой и не конфликтует с будущей модульной структурой `repo-pgjvm`.

## Consequences

**Positive:**

- Упрощается обеспечение консистентности client-owned data и access-control правил.
- Можно реализовать soft delete, уникальные ограничения, foreign keys и transactional revoke без дополнительной инфраструктуры.
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
| Ошибки в access-control constraints | Medium | High | Использовать транзакции, foreign keys, partial unique index для активного доступа, негативные тесты на revoke/no-scope |
| Backup настроен формально, но восстановление не проверяется | Medium | High | Ввести регулярную проверку восстановления тестового набора данных по NFR `9.3` |
| PostgreSQL будет перегружен аналитикой Phase 2 | Low | Medium | Выносить тяжёлые отчёты в read model/analytics storage после подтверждения нагрузки |

## Implementation Notes

- Для MVP использовать одну прикладную PostgreSQL database/schema для FitBridge domain data.
- Keycloak остаётся владельцем authentication data; FitBridge хранит только `keycloakSubject` и доменную проекцию пользователя.
- Для правила “один активный тренер на клиента в MVP” предусмотреть partial unique constraint по `clientProfileId` для `ACCESS_GRANT.status = 'ACTIVE'`.
- Для soft delete использовать `archivedAt`, `deletedAt`, `cancelledAt` и бизнес-фильтры, а не физическое удаление в пользовательском сценарии.
- Для `PROGRAM.workoutsJson`, `TRAINING_ENTRY.exercisesJson`, `ACCESS_GRANT.scopes` и похожих полей зафиксировать application-level validation до появления машинно-проверяемых API contracts.
