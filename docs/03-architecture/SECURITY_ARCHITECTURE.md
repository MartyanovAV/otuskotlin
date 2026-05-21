# Архитектура безопасности / модель угроз - FitBridge MVP

Документ фиксирует архитектуру безопасности и модель угроз для FitBridge MVP. Структура документа адаптирована из `.opencode/templates-docs/ADR-template.md`: контекст, решение, последствия и таблица рисков/угроз сохранены как каркас, но документ не является новым ADR.

## 1. Статус и область действия

| Параметр | Значение |
|---|---|
| Статус | Целевой baseline безопасности MVP |
| Область | Trainer-led MVP, Solo-client MVP, целевой backend API и observability-контур |
| Не входит в MVP | In-product `ADMIN`/support роль, личный кабинет администратора, support console, granular operator roles, broad bypass к domain API, продуктовый `AuditEvent` API, отдельный DLP-контур, SSO для корпоративных клиентов, production-grade SIEM |
| Связанные решения | [ADR-001 Keycloak](./ADR/ADR-001-use-keycloak.md), [ADR-002 POST Full API](./ADR/ADR-002-post-full-api.md), [ADR-005 PostgreSQL](./ADR/ADR-005-use-postgresql.md), [ADR-006 Observability](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md) |

Security scope MVP покрывает:

- аутентификацию пользователей через Keycloak/OIDC;
- обязательную JWT validation для всех MVP `/v1/*` endpoints на входном proxy;
- независимую backend-проверку JWT, user context и доменной policy;
- доменную авторизацию доступа к клиентским данным;
- управление согласиями через `Invite` и `AccessGrant`;
- защиту персональных и health-adjacent данных;
- маскированные структурированные логи и audit-oriented observability;
- угрозы критичных MVP-сценариев: приглашение, принятие доступа, отзыв доступа, дневник, программы, удаление/архивация профиля.

## 2. Акторы и модель доверия

| Актор / система | Доверие | Основные действия | Заметки по безопасности |
|---|---|---|---|
| `Client` | Внешний пользователь, владелец данных | Регистрация, ведение дневника, принятие/отзыв доступа, запрос удаления профиля | Владелец `ClientProfile`, `TrainingEntry`, истории и согласий |
| `Trainer` | Внешний пользователь с профессиональным профилем | Создание приглашений, просмотр разрешённых полей профиля клиента, назначение программ и ведение тренировочного процесса | Не владеет клиентскими данными; в MVP не редактирует `ClientProfile` и работает только по активному `AccessGrant` и scopes |
| `Support/Ops operator` | Операционный актор вне продукта | Controlled runbook/provisioning для пилота, просмотр masked технических логов, блокировка пользователя или отмена edge-case операции без sensitive payload | Не является MVP product role и не вызывает domain API как `ADMIN`; не читает client profile/diary/training history/health-adjacent data |
| `Keycloak` | Доверенный Identity Provider | Login, OIDC authorization code flow, выдача JWT/refresh token, realm roles | Не принимает доменные решения доступа к клиентским данным |
| `FitBridge Backend` | Доверенная доменная система | POST Full API, маппинг `sub` на пользователя, access decision, запись доменных данных | Единственный владелец правил owner/grant/scope/consent/deletion |
| `Envoy Gateway` | Инфраструктурный boundary | Маршрутизация `/v1/*`, проверка JWT/JWKS для всех MVP API endpoints | Не заменяет backend JWT/user-context validation и доменную авторизацию |
| `PostgreSQL` | Доверенное прикладное хранилище | Профили, доступы, приглашения, дневник, программы | Требует миграций, ограничений и backup/PITR по ADR-005 |
| `OpenSearch / Dashboards` | Внешний/platform observability-контур с повышенным риском | Поиск логов и audit-oriented событий | Не является частью application boundary FitBridge; должен получать только masked logs без JWT, invite tokens и raw health-adjacent payloads |

## 3. Поток аутентификации и токенов

Целевой поток основан на OIDC/OAuth2 и решении [ADR-001](./ADR/ADR-001-use-keycloak.md):

1. `Client` или `Trainer` открывает Web UI.
2. Web UI перенаправляет пользователя в Keycloak для входа.
3. Keycloak выполняет аутентификацию и выдаёт OIDC/JWT-токены.
4. Web UI вызывает FitBridge API через Envoy с `Authorization: Bearer <access_token>`.
5. Envoy проверяет `iss`, подпись и срок действия JWT через JWKS Keycloak для каждого MVP `/v1/*` endpoint и пробрасывает запрос в backend только после успешной edge validation.
6. FitBridge Backend независимо валидирует JWT/claims и user context, маппит `sub` на `FITBRIDGE_USER.keycloakSubject`, проверяет внутренний статус пользователя и выполняет доменную авторизацию.
7. Refresh token остаётся на стороне клиента/Keycloak-flow и не должен попадать в backend logs или OpenSearch.

Минимальные проверки backend для каждого MVP `/v1/*` POST Full endpoint:

- `iss` соответствует realm `fit-bridge`;
- `aud`/client соответствует ожидаемому client id FitBridge API;
- `exp`, `nbf`, `iat` валидны с ограниченным clock skew;
- `sub` присутствует и связан с внутренним `FITBRIDGE_USER`;
- пользователь не `BLOCKED` и не `DELETED`;
- role/claim достаточен только как coarse-grained признак, после чего выполняется доменная проверка owner/grant/scope.

## 4. Роли и claims

### 4.1. Роли

| Роль | Статус MVP | Назначение | Требование к policy |
|---|---|---|---|
| `CLIENT` | MVP product role | Владелец клиентского профиля, дневника, согласий и доступов | Роль должна попадать в JWT через scope `roles`; backend дополнительно проверяет профиль клиента |
| `TRAINER` | MVP product role | Владелец тренерского профиля, инициатор приглашения и получатель доступа | Backend требует активный профиль тренера и не полагается только на role claim |
| `ADMIN` / `SUPPORT` | Product role вне MVP | Полноценный product admin/support UI, support console и granular operator roles | Не используется в MVP domain API; если такая роль появится в Keycloak для эксплуатации, backend не должен давать ей доступ к client-owned domain data без отдельного Phase 2 решения |

Совмещение ролей `CLIENT` и `TRAINER` должно быть явно разрешено или запрещено security policy и покрыто тестами доступа. Domain API MVP остаётся `CLIENT`/`TRAINER` only; любые support/operator действия выполняются вне пользовательского продукта по controlled runbook.

### 4.2. Claims

| Claim | Источник | Использование FitBridge |
|---|---|---|
| `sub` | Keycloak | Стабильная связка с `FITBRIDGE_USER.keycloakSubject` |
| `realm_access.roles` / roles scope | Keycloak | Coarse-grained role check только для MVP product roles `CLIENT`, `TRAINER`; `ADMIN`/`SUPPORT` не являются разрешением на domain API |
| `iss` | Keycloak | Проверка доверенного issuer |
| `aud` | Keycloak client | Проверка назначения токена для FitBridge API |
| `exp`, `iat`, `nbf` | Keycloak | Проверка срока действия и replay window |
| `jti` | Keycloak | Потенциальная основа для deny-list/anti-replay в критичных future-сценариях |
| `email`, `preferred_username` | Keycloak | Не использовать для авторизации; не писать в логи без маскирования |
| `scope` | Keycloak/OIDC | Только OIDC/API scopes; доменные scopes доступа клиента хранятся в `ACCESS_GRANT.scopes` |

## 5. Границы ответственности Keycloak и FitBridge Backend

| Область | Keycloak | FitBridge Backend |
|---|---|---|
| Регистрация и вход | Аутентифицирует пользователя, управляет паролями/session/refresh | Создаёт или связывает внутреннюю проекцию пользователя |
| Токены | Выпускает и подписывает JWT | Независимо валидирует claims для каждого `/v1/*` endpoint и не хранит raw token |
| Realm roles | Хранит `CLIENT`, `TRAINER`; возможные эксплуатационные роли Keycloak не являются product roles MVP | Использует роли как предварительное условие, но не как единственный фактор доступа; domain access остаётся owner/grant/scope |
| Доменные профили | Не владеет | Хранит `ClientProfile`, `TrainerProfile`, статусы и ownership |
| Доступ к клиентским данным | Не решает | Проверяет owner access, active `AccessGrant`, scopes, статусы удаления/архивации |
| Согласие и отзыв | Не владеет | Реализует `Invite`, `AccessGrant`, `revoke`, consent/deletion impact |
| Аудит | Может логировать IAM-события | Логирует masked business/security events по ADR-006 |

## 5.1. MVP Support Operations Model

В MVP нет in-product `ADMIN` роли, личного кабинета администратора или support console. Операционное сопровождение пилота отделено от пользовательского продукта и не расширяет domain API.

| Область | Целевая MVP-модель |
|---|---|
| Канал выполнения | Keycloak + controlled operational runbook/provisioning вне product UI и вне обычных `domain.action` методов |
| Разрешённые действия пилота | Допуск или отзыв пилотного тренера, перевод `FITBRIDGE_USER.status` в `BLOCKED`, отмена invite/access edge case, сопровождение privacy/deletion action |
| Запрещённый доступ | Чтение `ClientProfile`, дневника, training history, health-adjacent payload, raw invite token, JWT и raw request/response payload |
| Access policy | Broad bypass отсутствует: owner/grant/scope policy применяется всегда для domain API; support/operator не становится владельцем и не получает `AccessGrant` |
| Аудит | Каждое действие фиксируется как masked operational/security event: internal ids, actor/operator id, action, result, requestId/ticketId, timestamp; sensitive payload запрещён |
| Резерв Phase 2 | Full admin UI, support console, granular operator roles и отдельная support-policy проектируются отдельно и не входят в MVP |

Поддерживаемые эффекты операционных действий должны выражаться существующими MVP-состояниями (`FITBRIDGE_USER.status = BLOCKED`, `Invite.status = CANCELLED`, `AccessGrant.status = REVOKED/EXPIRED`, `ProgramAssignment.status = CANCELLED`) без добавления новых admin/support сущностей в модель данных MVP.

## 6. Модель принятия решений о доступе

Все бизнес-операции FitBridge API используют `deny by default`. Разрешение выдаётся только если выполнено одно из явных правил.

### 6.1. Общий алгоритм

1. Проверить JWT/claims и внутреннего пользователя в backend независимо от Envoy.
2. Проверить, что endpoint/action известен и имеет policy.
3. Проверить статус пользователя: `ACTIVE` обязателен.
4. Проверить статус целевого ресурса: профиль/дневник/программа не должны быть `DELETED` или недоступны из-за `archivedAt/deletedAt`.
5. Если пользователь владелец ресурса — разрешить только действия владельца.
6. Если пользователь тренер — найти его `TrainerProfile`, затем активный `AccessGrant` для целевого `ClientProfile`.
7. Проверить `AccessGrant.status = ACTIVE`, `revokedAt is null`, `expiresAt` не истёк и `scopes` содержит нужный scope.
8. Если запрос пытается использовать `ADMIN`/`SUPPORT` или любую эксплуатационную роль как основание чтения/изменения client-owned domain data — отказать; MVP domain API допускает только явные правила `CLIENT`/`TRAINER` + owner/grant/scope.
9. Все отказы по scope/owner/grant/support-bypass логировать как masked `access.validateScope` с `DENIED` по [ADR-006](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

### 6.2. Scopes MVP

| Scope | Разрешает | Не разрешает |
|---|---|---|
| `PROFILE_READ` | Читать разрешённые поля клиентского профиля через активный `AccessGrant`, включая optional `gender` и `goals` | Читать скрытые поля, контакты вне политики, данные удалённого профиля или Phase 2 body metrics |
| `DIARY_READ` | Читать дневник клиента в рамках активного доступа | Читать дневник после `revoke` или после удаления профиля |
| `DIARY_WRITE` | Создавать/изменять разрешённые записи от имени тренера | Физически удалять историю клиента |
| `PROGRAM_READ` | Читать назначенные или разрешённые программы | Читать private drafts клиента или других тренеров |
| `PROGRAM_WRITE` | Назначать/изменять план в рамках активного доступа | Назначать программу без активного grant или после revoke |

`PROFILE_WRITE` для клиентского профиля не входит в минимальные MVP scopes. Тренер в MVP получает только `PROFILE_READ` на разрешённые поля `ClientProfile`; любые write-действия тренера ограничены тренировочным доменом (`DIARY_WRITE`, `PROGRAM_WRITE`) и не меняют `ClientProfile`. Возможный trainer `PROFILE_WRITE` для клиентского профиля рассматривается только как Phase 2 / out of MVP и не должен попадать в `Invite.proposedScopes` или `AccessGrant.scopes` MVP.

### 6.3. Field-level privacy для optional profile fields MVP

`ClientProfile.gender` и `ClientProfile.goals` входят в MVP как добровольные nullable поля профиля. Они не являются onboarding blockers и не требуют заполнения для создания рабочего профиля, trainer-led сценария или solo-client пути.

| Поле | Статус MVP | Owner `CLIENT` | `TRAINER` с `AccessGrant` + `PROFILE_READ` | Trainer write | Support/Ops | Логирование |
|---|---|---|---|---|---|---|
| `gender` | Optional/nullable profile field | Может читать, устанавливать, изменять и очищать своё значение | Может читать только при `AccessGrant.status = ACTIVE` и scope `PROFILE_READ` | Запрещён; trainer `PROFILE_WRITE` отсутствует в MVP | Не может читать значение через domain API, runbook или observability | Raw value не логируется; допустимы только field name/action/result/internal ids |
| `goals` | Optional/nullable profile field; может быть health-adjacent | Может читать, устанавливать, изменять и очищать своё значение | Может читать только при `AccessGrant.status = ACTIVE` и scope `PROFILE_READ` | Запрещён; trainer `PROFILE_WRITE` отсутствует в MVP | Не может читать значение через domain API, runbook или observability | Raw value и текст целей не логируются; допустимы только field name/action/result/internal ids |

`ClientProfile.heightCm` не входит в supported profile fields MVP. Рост и связанные body metrics относятся к Phase 2 / later measurement scope и не должны становиться доступными через `PROFILE_READ` до отдельного решения по consent, retention и field-level access.

## 7. Consent, deletion и влияние на access grants

| Событие | Security effect | Данные / доступы |
|---|---|---|
| `access.createInvite` | Не создаёт доступ | Хранится `Invite` с `tokenHash`, proposed scopes и сроком действия |
| `access.acceptInvite` | Явное согласие клиента на scopes | В транзакции переводит `Invite.status` в `ACCEPTED` и создаёт/активирует `AccessGrant.status = ACTIVE`; pending invite больше не может использоваться повторно |
| `access.revoke` | Немедленный отзыв согласия | `AccessGrant.status = REVOKED`, `revokedAt` заполнен; дальнейшие trainer-запросы получают отказ |
| Истечение invite | Не даёт доступ | `Invite.status = EXPIRED`; новый доступ возможен только через новое приглашение/подтверждение |
| Запрос удаления/архивации client profile | Блокирует новые операции над профилем | Активные grants переводятся в `REVOKED` или `EXPIRED`; активные assignments переводятся в `CANCELLED` согласно API docs |
| Удаление/архивация trainer profile | Блокирует действия тренера | Нельзя архивировать тренера при активных клиентах без отзыва доступов |

Модель статусов разделена: `Invite.status` принимает `PENDING`, `ACCEPTED`, `DECLINED`, `EXPIRED`, `CANCELLED`; `AccessGrant.status` принимает только `ACTIVE`, `REVOKED`, `EXPIRED`. Ключевое правило: удаление/архивация профиля и отзыв согласия должны влиять на access decision немедленно, а не только на UI. Backend обязан проверять статус grant/resource на каждый запрос.

## 8. 152-ФЗ и health-adjacent controls

FitBridge MVP обрабатывает персональные данные и health-adjacent данные: `gender`, цели, интенсивность, настроение, заметки, упражнения, вес/RPE. Рост (`heightCm`) и связанные body metrics не входят в supported profile fields MVP и переносятся в Phase 2 / later measurement scope. Полноценная юридическая модель требует отдельной матрицы типов данных, оснований обработки и сроков хранения, но MVP baseline должен включать следующие controls.

| Control | MVP правило |
|---|---|
| Минимизация данных | Не собирать замеры, `heightCm` и расширенные health metrics в MVP; `Measurement` остаётся Phase 2 |
| Purpose limitation | Использовать данные только для дневника, плана, прогресса и явно выданного тренеру доступа |
| Явное согласие | Trainer access появляется только после `acceptInvite` и подтверждения scopes клиентом |
| Отзыв согласия | `revoke` немедленно закрывает доступ тренера независимо от UI/session |
| Разграничение доступа | Owner/grant/scope checks на каждый запрос; no shared trainer visibility без grant |
| Маскирование логов | Не логировать ФИО, email, телефон, JWT, invite token, raw `gender`/`goals`/notes/exercises/mood/RPE |
| Retention | Для профилей, soft-deleted записей, invites, grants и логов задаётся retention policy по типу данных и окружению |
| Доступ к observability | OpenSearch/Dashboards доступны только ограниченному support/ops контуру для masked logs; этот доступ не является domain API access и не раскрывает sensitive payload |
| Transport security | Все внешние контуры production должны использовать HTTPS; исключения допустимы только для изолированных dev/test окружений |
| Storage security | PostgreSQL хранит доменные данные с FK/constraints; backup/PITR и проверка восстановления нужны по ADR-005 |

## 9. Audit и observability

Security events связаны с [ADR-006](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md). В MVP используется infrastructure audit-oriented logging, а не продуктовый `AuditEvent` API.

Обязательные security-события:

- `access.acceptInvite`;
- `access.declineInvite`;
- `access.grant`;
- `access.revoke`;
- `access.validateScope` с `DENIED`;
- `profile.deleteOrArchiveRequested`;
- `ops.blockUser` / `ops.cancelInvite` / `ops.revokePilotAccess` как controlled support-operation без sensitive payload;
- `program.assign` / `program.updateAssignment` / `program.cancelAssignment`;
- `diary.createEntry` / `diary.updateEntry` / `diary.deleteEntry`.

Минимальные поля: `timestamp`, `level`, `service`, `environment`, `requestId`, `userId` или `keycloakSubject`, `action`, `entityType`, `entityId`, `result`, `durationMs`, `errorCode`. Raw payload и секреты запрещены.

## 10. Критичный поток: принятие trainer-led приглашения / выдача доступа / проверка API-доступа

```mermaid
sequenceDiagram
    autonumber
    actor Trainer as Тренер
    actor Client as Клиент
    participant UI as Web UI
    participant KC as Keycloak
    participant GW as Envoy Gateway
    participant API as FitBridge Backend
    participant DB as PostgreSQL
    participant LOG as Fluent Bit / OpenSearch

    Trainer->>KC: Login
    KC-->>Trainer: JWT с role TRAINER
    Trainer->>UI: Создать приглашение клиента
    UI->>GW: POST /v1/access.createInvite + Bearer JWT + proposedScopes
    GW->>GW: Edge JWT validation для /v1/*
    GW->>API: Проксировать валидированный запрос
    API->>API: Backend JWT/user-context validation, role TRAINER, TrainerProfile
    API->>DB: Сохранить Invite(tokenHash, PENDING, expiresAt, proposedScopes)
    API->>LOG: access.createInvite SUCCESS без raw token/email
    API-->>UI: inviteLink с одноразовым raw token

    Client->>UI: Открыть inviteLink
    Client->>KC: Login / registration
    KC-->>Client: JWT с role CLIENT
    UI->>GW: POST /v1/access.readInvite + Bearer JWT + raw token
    GW->>GW: Edge JWT validation для /v1/*
    GW->>API: Проксировать валидированный запрос
    API->>API: Backend JWT/user-context validation, role CLIENT
    API->>DB: Найти Invite по hash(token), проверить PENDING/expiresAt
    API-->>UI: Публичные данные тренера + requested scopes

    Client->>UI: Принять scopes
    UI->>GW: POST /v1/access.acceptInvite + Bearer JWT + raw token + acceptedScopes
    GW->>GW: Edge JWT validation для /v1/*
    GW->>API: Проксировать валидированный запрос
    API->>API: Backend JWT/user-context validation, role CLIENT, owner/client profile, token hash
    API->>DB: Transaction: mark Invite ACCEPTED, create AccessGrant ACTIVE, revoke/close conflicting active grant if policy requires
    API->>LOG: access.acceptInvite SUCCESS; access.grant SUCCESS
    API-->>UI: AccessGrant id/status ACTIVE

    Trainer->>UI: Открыть карточку клиента
    UI->>GW: POST /v1/profile.readClientProfile + Bearer JWT + clientProfileId
    GW->>GW: Edge JWT validation для /v1/*
    GW->>API: Проксировать валидированный запрос
    API->>API: Backend JWT/user-context validation, role TRAINER, user ACTIVE
    API->>DB: Проверить TrainerProfile и AccessGrant ACTIVE + PROFILE_READ scope + client not deleted
    alt grant valid
        API->>LOG: access.validateScope SUCCESS masked
        API-->>UI: Разрешённые поля client profile
    else grant revoked/expired/no scope
        API->>LOG: access.validateScope DENIED masked
        API-->>UI: 403 ACCESS_DENIED
    end
```

## 11. Модель угроз

| Угроза | Затронутые активы | Вероятность | Влияние | Митигация |
|---|---|---:|---:|---|
| Утечка JWT/access token через браузер, логи или proxy headers | Аккаунт пользователя, клиентские данные, grants | Medium | High | HTTPS в production, короткий TTL access token, refresh token только в безопасном клиентском flow, запрет логирования `Authorization`, маскирование headers, CSP/XSS controls для UI |
| Broken access control в backend policy | ClientProfile, TrainingEntry, ProgramAssignment | Medium | High | Централизованный `access.validateScope`, deny by default, policy tests для owner/grant/scope и запрета support/admin bypass, ревью всех POST Full endpoints |
| IDOR: trainer подставляет чужой `clientProfileId` | Client-owned data другого клиента | Medium | High | Никогда не доверять id из запроса без проверки `AccessGrant`; фильтровать все read/list по caller ownership/grant; negative acceptance tests |
| Stale access после `revoke` | Дневник, программы, профиль клиента | Medium | High | Проверять `AccessGrant.status/revokedAt/expiresAt` на каждый запрос; не кэшировать разрешение дольше одного запроса; transactional revoke в PostgreSQL |
| Abuse invite token: перебор, повторное принятие, пересылка ссылки | Invite, AccessGrant, consent | Medium | High | Длинный random token, хранить только hash, TTL 1-30 дней, single-use status transition, rate limit create/read/accept, не логировать raw token |
| Excessive logging sensitive data | PII, health-adjacent fields, optional profile fields `gender`/`goals`, JWT, invite token | Medium | High | Sensitive data rules из ADR-006, deny-list полей, structured logs без raw payload, ревью log statements, test fixtures на отсутствие секретов в логах |
| Privilege escalation: CLIENT/TRAINER получает `ADMIN`/`SUPPORT` или чужую роль | Админские операции, чужие данные | Low/Medium | High | Backend не использует `ADMIN`/`SUPPORT` как разрешение на domain data; domain API остаётся `CLIENT`/`TRAINER` only; роли из token сверять с внутренним статусом и профилем; realm config review |
| Replay/double POST для `acceptInvite`, `grantAccess`, `assignProgram` | Дубли grants/assignments, неконсистентность consent | Medium | Medium/High | Idempotency keys для критичных POST по ADR-002, unique constraints/transactions, status machine, optimistic locking/version для programs |
| Profile deletion bypass: доступ после архивации/удаления профиля | ClientProfile, diary, assignments, grants | Medium | High | Access decision проверяет `archivedAt/deletedAt`; delete/archive транзакционно отзывает grants и отменяет assignments; negative tests profile deleted + active token |
| OpenSearch exposure через слабые dev/test credentials или открытый порт | Логи, user ids, operational data | Medium | High | Не переносить dev/test credentials в production, закрыть Dashboards/VPC/VPN, auth для Dashboards, retention/index lifecycle, не хранить sensitive payloads в логах |
| Keycloak realm misconfiguration: roles не попадают в JWT или неверный issuer/audience | Все защищённые endpoints | Medium | High | Realm/client config as code, smoke-test OIDC flow, проверка `iss/aud/roles`, мониторинг 401/403 spikes, явное правило: support/ops роли не дают domain API access |
| PostgreSQL constraint risk для одного active trainer | Grants, trainer-led ownership | Medium | Medium/High | Partial unique index на active grant по `clientProfileId`, транзакции `acceptInvite/revoke`, миграционные тесты по ADR-005 |
| Consent mismatch: accepted scopes отличаются от proposed scopes | Согласие клиента, legal/privacy posture | Low/Medium | High | UI показывает requested scopes; backend разрешает только subset/equal proposed scopes; сохраняет accepted scopes в grant; логирует grant без payload |

## 12. Целевые требования безопасности MVP

| Требование | Риск, который закрывается | Целевое правило MVP |
|---|---|---|
| Единое JWT-покрытие `/v1/*` | Бизнес endpoint без edge validation | Все MVP `/v1/*` endpoints проходят JWT validation в Envoy; backend дополнительно выполняет JWT/user-context validation и доменную access policy |
| Support operations access | Небезопасный support-доступ | Нет in-product `ADMIN`/support роли и support domain API в MVP; операции пилота выполняются через Keycloak/runbook, не читают sensitive client data и не обходят owner/grant/scope |
| Data classification / retention | Несоответствие 152-ФЗ и избыточное хранение | Типы данных, основания обработки, сроки хранения и удаления фиксируются в data classification / retention matrix |
| Idempotency для критичных POST | Replay/double submit | Критичные операции используют idempotency keys/error model по ADR-002 |
| Secret management для Keycloak/OpenSearch/PostgreSQL | Компрометация окружения | Secrets, rotation и network isolation задаются deployment architecture/release plan |
| Централизованная backend policy | Расхождение поведения handlers и security-модели | Policy tests покрывают owner/grant/scope/deny by default для бизнес-операций |

## 13. Последствия

**Positive:**

- Уточнена граница Keycloak vs FitBridge Backend.
- Зафиксирован единый access decision model для owner access, active `AccessGrant`, scopes и deny by default.
- Security baseline связан с ADR-001, ADR-002, ADR-005 и ADR-006.

**Negative:**

- Backend обязан выполнять доменную авторизацию на каждый запрос; простой role-based access недостаточен.
- Целевой MVP требует согласованной конфигурации realm, Envoy, secrets, retention и data classification.

**Risks:**

- Если security checks будут реализованы точечно в каждом handler, появится риск IDOR и broken access control.
- Если OpenSearch будет использоваться для debugging raw payloads, observability станет каналом утечки чувствительных данных.
