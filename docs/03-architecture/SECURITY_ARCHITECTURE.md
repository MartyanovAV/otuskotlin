# Архитектура безопасности / модель угроз - FitBridge MVP

Документ фиксирует архитектуру безопасности и модель угроз для FitBridge MVP. Структура документа адаптирована из `.opencode/templates-docs/ADR-template.md`: контекст, решение, последствия и таблица рисков/угроз сохранены как каркас, но документ не является новым ADR.

## 1. Статус и область действия

| Параметр | Значение |
|---|---|
| Статус | Черновик / базовый контур безопасности MVP |
| Область | Trainer-led MVP, Solo-client MVP, локальный стенд и целевой backend API |
| Не входит в MVP | Полноценная админ-панель, продуктовый `AuditEvent` API, отдельный DLP-контур, SSO для корпоративных клиентов, production-grade SIEM |
| Связанные решения | [ADR-001 Keycloak](./ADR/ADR-001-use-keycloak.md), [ADR-002 POST Full API](./ADR/ADR-002-post-full-api.md), [ADR-005 PostgreSQL](./ADR/ADR-005-use-postgresql.md), [ADR-006 Observability](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md) |

Security scope MVP покрывает:

- аутентификацию пользователей через Keycloak/OIDC;
- проверку JWT на входном proxy и в FitBridge Backend;
- доменную авторизацию доступа к клиентским данным;
- управление согласиями через `Invite` и `AccessGrant`;
- защиту персональных и health-adjacent данных;
- маскированные структурированные логи и audit-oriented observability;
- угрозы критичных MVP-сценариев: приглашение, принятие доступа, отзыв доступа, дневник, программы, удаление/архивация профиля.

## 2. Акторы и модель доверия

| Актор / система | Доверие | Основные действия | Заметки по безопасности |
|---|---|---|---|
| `Client` | Внешний пользователь, владелец данных | Регистрация, ведение дневника, принятие/отзыв доступа, запрос удаления профиля | Владелец `ClientProfile`, `TrainingEntry`, истории и согласий |
| `Trainer` | Внешний пользователь с профессиональным профилем | Создание приглашений, просмотр разрешённых клиентов, назначение программ | Не владеет клиентскими данными; работает только по активному `AccessGrant` и scopes |
| `Admin/Support` | Future/support role | Диагностика пилота, просмотр технических логов, ручная поддержка | `ADMIN` описан в API entities, но отсутствует в текущем Keycloak realm import; это config gap до реализации support-сценариев |
| `Keycloak` | Доверенный Identity Provider | Login, OIDC authorization code flow, выдача JWT/refresh token, realm roles | Не принимает доменные решения доступа к клиентским данным |
| `FitBridge Backend` | Доверенная доменная система | POST Full API, маппинг `sub` на пользователя, access decision, запись доменных данных | Единственный владелец правил owner/grant/scope/consent/deletion |
| `Envoy Gateway` | Инфраструктурный boundary | Маршрутизация `/v1/*`, проверка JWT/JWKS для защищённых маршрутов | Не заменяет доменную авторизацию backend |
| `PostgreSQL` | Доверенное прикладное хранилище | Профили, доступы, приглашения, дневник, программы | Требует миграций, ограничений и backup/PITR по ADR-005 |
| `OpenSearch / Dashboards` | Доверенный observability-контур с повышенным риском | Поиск логов и audit-oriented событий | Должен получать только masked logs без JWT, invite tokens и raw health-adjacent payloads |

## 3. Поток аутентификации и токенов

Целевой поток основан на OIDC/OAuth2 и решении [ADR-001](./ADR/ADR-001-use-keycloak.md):

1. `Client` или `Trainer` открывает Web UI.
2. Web UI перенаправляет пользователя в Keycloak для входа.
3. Keycloak выполняет аутентификацию и выдаёт OIDC/JWT-токены.
4. Web UI вызывает FitBridge API через Envoy с `Authorization: Bearer <access_token>`.
5. Envoy проверяет `iss`, подпись и срок действия JWT через JWKS Keycloak и пробрасывает запрос в backend.
6. FitBridge Backend повторно доверяет только проверенному токену/claims из защищённого контура, маппит `sub` на `FITBRIDGE_USER.keycloakSubject`, проверяет локальный статус пользователя и выполняет доменную авторизацию.
7. Refresh token остаётся на стороне клиента/Keycloak-flow и не должен попадать в backend logs или OpenSearch.

Минимальные проверки backend для каждого защищённого POST Full endpoint:

- `iss` соответствует realm `fit-bridge`;
- `aud`/client соответствует ожидаемому client id, когда настройка будет финализирована;
- `exp`, `nbf`, `iat` валидны с ограниченным clock skew;
- `sub` присутствует и связан с локальным `FITBRIDGE_USER`;
- пользователь не `BLOCKED` и не `DELETED`;
- role/claim достаточен только как coarse-grained признак, после чего выполняется доменная проверка owner/grant/scope.

## 4. Роли и claims

### 4.1. Роли

| Роль | Статус MVP | Назначение | Разрыв конфигурации |
|---|---|---|---|
| `CLIENT` | Есть в текущем Keycloak realm import | Владелец клиентского профиля, дневника, согласий и доступов | Нужно проверить, что роль попадает в JWT через scope `roles` |
| `TRAINER` | Есть в текущем Keycloak realm import | Владелец тренерского профиля, инициатор приглашения и получатель доступа | Нужно проверить, что backend требует профиль тренера, а не только role claim |
| `ADMIN` | Future/support role в API entities | Поддержка и ограниченная диагностика | В `deploy/volumes/keycloak/import/fit-bridge-realm.json` роль отсутствует; до добавления в realm и политики backend любые admin-действия должны быть недоступны |

Текущий test user в локальном import имеет одновременно `CLIENT` и `TRAINER`. Это допустимо для локальной проверки MVP, но production-policy должна явно разрешать или запрещать совмещение ролей и покрывать этот случай тестами.

### 4.2. Claims

| Claim | Источник | Использование FitBridge |
|---|---|---|
| `sub` | Keycloak | Стабильная связка с `FITBRIDGE_USER.keycloakSubject` |
| `realm_access.roles` / roles scope | Keycloak | Coarse-grained role check: `CLIENT`, `TRAINER`, future `ADMIN` |
| `iss` | Keycloak | Проверка доверенного issuer |
| `aud` | Keycloak client | Проверка назначения токена после финализации client config |
| `exp`, `iat`, `nbf` | Keycloak | Проверка срока действия и replay window |
| `jti` | Keycloak | Потенциальная основа для deny-list/anti-replay в критичных future-сценариях |
| `email`, `preferred_username` | Keycloak | Не использовать для авторизации; не писать в логи без маскирования |
| `scope` | Keycloak/OIDC | Только OIDC/API scopes; доменные scopes доступа клиента хранятся в `ACCESS_GRANT.scopes` |

## 5. Границы ответственности Keycloak и FitBridge Backend

| Область | Keycloak | FitBridge Backend |
|---|---|---|
| Регистрация и вход | Аутентифицирует пользователя, управляет паролями/session/refresh | Создаёт или связывает локальную проекцию пользователя |
| Токены | Выпускает и подписывает JWT | Валидирует claims и не хранит raw token |
| Realm roles | Хранит `CLIENT`, `TRAINER`, future `ADMIN` | Использует роли как предварительное условие, но не как единственный фактор доступа |
| Доменные профили | Не владеет | Хранит `ClientProfile`, `TrainerProfile`, статусы и ownership |
| Доступ к клиентским данным | Не решает | Проверяет owner access, active `AccessGrant`, scopes, статусы удаления/архивации |
| Согласие и отзыв | Не владеет | Реализует `Invite`, `AccessGrant`, `revoke`, consent/deletion impact |
| Аудит | Может логировать IAM-события | Логирует masked business/security events по ADR-006 |

## 6. Модель принятия решений о доступе

Все бизнес-операции FitBridge API используют `deny by default`. Разрешение выдаётся только если выполнено одно из явных правил.

### 6.1. Общий алгоритм

1. Проверить JWT и локального пользователя.
2. Проверить, что endpoint/action известен и имеет policy.
3. Проверить статус пользователя: `ACTIVE` обязателен.
4. Проверить статус целевого ресурса: профиль/дневник/программа не должны быть `DELETED` или недоступны из-за `archivedAt/deletedAt`.
5. Если пользователь владелец ресурса — разрешить только действия владельца.
6. Если пользователь тренер — найти его `TrainerProfile`, затем активный `AccessGrant` для целевого `ClientProfile`.
7. Проверить `AccessGrant.status = ACTIVE`, `revokedAt is null`, `expiresAt` не истёк и `scopes` содержит нужный scope.
8. Если пользователь future `ADMIN` — применять отдельную support-policy после появления роли в realm; до этого запрещать.
9. Все отказы по scope/owner/grant логировать как masked `access.validateScope` с `DENIED` по [ADR-006](./ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

### 6.2. Scopes MVP

| Scope | Разрешает | Не разрешает |
|---|---|---|
| `PROFILE_READ` | Читать разрешённые поля клиентского профиля | Читать скрытые поля, контакты вне политики, данные удалённого профиля |
| `PROFILE_WRITE` | Изменять явно разрешённые рабочие поля/заметки | Менять ownership, consent, статус удаления |
| `DIARY_READ` | Читать дневник клиента в рамках активного доступа | Читать дневник после `revoke` или после удаления профиля |
| `DIARY_WRITE` | Создавать/изменять разрешённые записи от имени тренера | Физически удалять историю клиента |
| `PROGRAM_READ` | Читать назначенные или разрешённые программы | Читать private drafts клиента или других тренеров |
| `PROGRAM_WRITE` | Назначать/изменять план в рамках активного доступа | Назначать программу без активного grant или после revoke |

## 7. Consent, deletion и влияние на access grants

| Событие | Security effect | Данные / доступы |
|---|---|---|
| `access.createInvite` | Не создаёт доступ | Хранится `Invite` с `tokenHash`, proposed scopes и сроком действия |
| `access.acceptInvite` | Явное согласие клиента на scopes | В транзакции создаёт/активирует `AccessGrant`; pending invite больше не может использоваться повторно |
| `access.revoke` | Немедленный отзыв согласия | `AccessGrant.status = REVOKED`, `revokedAt` заполнен; дальнейшие trainer-запросы получают отказ |
| Истечение invite | Не даёт доступ | `Invite.status = EXPIRED`; новый доступ возможен только через новое приглашение/подтверждение |
| Запрос удаления/архивации client profile | Блокирует новые операции над профилем | Активные grants должны быть отозваны или сделаны недействительными; активные assignments переводятся в `CANCELLED` согласно API docs |
| Удаление/архивация trainer profile | Блокирует действия тренера | Нельзя архивировать тренера при активных клиентах без отзыва доступов |

Ключевое правило: удаление/архивация профиля и отзыв согласия должны влиять на access decision немедленно, а не только на UI. Backend обязан проверять статус grant/resource на каждый запрос.

## 8. 152-ФЗ и health-adjacent controls

FitBridge MVP обрабатывает персональные данные и health-adjacent данные: цели, рост, интенсивность, настроение, заметки, упражнения, вес/RPE. Полноценная юридическая модель требует отдельной матрицы типов данных, оснований обработки и сроков хранения, но MVP baseline должен включать следующие controls.

| Control | MVP правило |
|---|---|
| Минимизация данных | Не собирать замеры и расширенные health metrics в MVP; `Measurement` остаётся Phase 2 |
| Purpose limitation | Использовать данные только для дневника, плана, прогресса и явно выданного тренеру доступа |
| Явное согласие | Trainer access появляется только после `acceptInvite` и подтверждения scopes клиентом |
| Отзыв согласия | `revoke` немедленно закрывает доступ тренера независимо от UI/session |
| Разграничение доступа | Owner/grant/scope checks на каждый запрос; no shared trainer visibility без grant |
| Маскирование логов | Не логировать ФИО, email, телефон, JWT, invite token, raw notes/goals/exercises/mood/RPE |
| Retention | Для профилей, soft-deleted записей, invites, grants и логов нужно зафиксировать сроки до production/pilot expansion |
| Доступ к observability | OpenSearch/Dashboards доступны только support/admin контуру; local credentials не использовать в production |
| Transport security | Все внешние контуры production должны использовать HTTPS; local HTTP допускается только для dev стенда |
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
    participant API as FitBridge Backend
    participant DB as PostgreSQL
    participant LOG as OpenSearch/Fluent Bit

    Trainer->>KC: Login
    KC-->>Trainer: JWT с role TRAINER
    Trainer->>UI: Создать приглашение клиента
    UI->>API: POST /v1/access.createInvite + Bearer JWT + proposedScopes
    API->>API: Проверить JWT, role TRAINER, TrainerProfile
    API->>DB: Сохранить Invite(tokenHash, PENDING, expiresAt, proposedScopes)
    API->>LOG: access.createInvite SUCCESS без raw token/email
    API-->>UI: inviteLink с одноразовым raw token

    Client->>UI: Открыть inviteLink
    UI->>API: POST /v1/access.readInvite + raw token
    API->>DB: Найти Invite по hash(token), проверить PENDING/expiresAt
    API-->>UI: Публичные данные тренера + requested scopes

    Client->>KC: Login / registration
    KC-->>Client: JWT с role CLIENT
    Client->>UI: Принять scopes
    UI->>API: POST /v1/access.acceptInvite + Bearer JWT + raw token + acceptedScopes
    API->>API: Проверить JWT, role CLIENT, owner/client profile, token hash
    API->>DB: Transaction: mark Invite ACCEPTED, create AccessGrant ACTIVE, revoke/close conflicting active grant if policy requires
    API->>LOG: access.acceptInvite SUCCESS; access.grant SUCCESS
    API-->>UI: AccessGrant id/status ACTIVE

    Trainer->>UI: Открыть карточку клиента
    UI->>API: POST /v1/profile.readClientProfile + Bearer JWT + clientProfileId
    API->>API: Проверить JWT, role TRAINER, local user ACTIVE
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
| Broken access control в backend policy | ClientProfile, TrainingEntry, ProgramAssignment | Medium | High | Централизованный `access.validateScope`, deny by default, policy tests для owner/grant/scope/admin, ревью всех POST Full endpoints |
| IDOR: trainer подставляет чужой `clientProfileId` | Client-owned data другого клиента | Medium | High | Никогда не доверять id из запроса без проверки `AccessGrant`; фильтровать все read/list по caller ownership/grant; negative acceptance tests |
| Stale access после `revoke` | Дневник, программы, профиль клиента | Medium | High | Проверять `AccessGrant.status/revokedAt/expiresAt` на каждый запрос; не кэшировать разрешение дольше одного запроса; transactional revoke в PostgreSQL |
| Abuse invite token: перебор, повторное принятие, пересылка ссылки | Invite, AccessGrant, consent | Medium | High | Длинный random token, хранить только hash, TTL 1-30 дней, single-use status transition, rate limit create/read/accept, не логировать raw token |
| Excessive logging sensitive data | PII, health-adjacent fields, JWT, invite token | Medium | High | Sensitive data rules из ADR-006, deny-list полей, structured logs без raw payload, ревью log statements, test fixtures на отсутствие секретов в логах |
| Privilege escalation: CLIENT/TRAINER получает ADMIN или чужую роль | Админские операции, чужие данные | Low/Medium | High | `ADMIN` отсутствует в realm до отдельного решения; backend запрещает admin endpoints до policy; роли из token сверять с локальным статусом и профилем; realm config review |
| Replay/double POST для `acceptInvite`, `grantAccess`, `assignProgram` | Дубли grants/assignments, неконсистентность consent | Medium | Medium/High | Idempotency keys для критичных POST по ADR-002, unique constraints/transactions, status machine, optimistic locking/version для programs |
| Profile deletion bypass: доступ после архивации/удаления профиля | ClientProfile, diary, assignments, grants | Medium | High | Access decision проверяет `archivedAt/deletedAt`; delete/archive транзакционно отзывает grants и отменяет assignments; negative tests profile deleted + active token |
| OpenSearch exposure через слабые local credentials или открытый порт | Логи, user ids, operational data | Medium | High | Local credentials не переносить в production, закрыть Dashboards/VPC/VPN, auth для Dashboards, retention/index lifecycle, не хранить sensitive payloads в логах |
| Keycloak realm misconfiguration: roles не попадают в JWT или неверный issuer/audience | Все защищённые endpoints | Medium | High | Realm/client config as code, smoke-test OIDC flow, проверка `iss/aud/roles`, мониторинг 401/403 spikes, документация config gap для `ADMIN` |
| PostgreSQL constraint gap для одного active trainer | Grants, trainer-led ownership | Medium | Medium/High | Partial unique index на active grant по `clientProfileId`, транзакции `acceptInvite/revoke`, миграционные тесты по ADR-005 |
| Consent mismatch: accepted scopes отличаются от proposed scopes | Согласие клиента, legal/privacy posture | Low/Medium | High | UI показывает requested scopes; backend разрешает только subset/equal proposed scopes; сохраняет accepted scopes в grant; логирует grant без payload |

## 12. Разрывы безопасности и следующие шаги

| Gap | Риск | Действие до production/pilot expansion |
|---|---|---|
| `ADMIN` роль описана в API entities, но отсутствует в текущем Keycloak realm import | Нельзя безопасно реализовать support-доступ | Добавить отдельное ADR/решение или realm migration + backend admin policy; до этого admin endpoints запрещены |
| Текущий Envoy защищает только часть `/v1/*` prefixes (`diary`, `program`, `report`) | `profile/access` endpoints могут быть не покрыты proxy auth после реализации | Расширить правила JWT auth на все business API endpoints или перенести обязательную проверку в backend middleware |
| Нет формальной data classification / retention matrix | Риск несоответствия 152-ФЗ и избыточного хранения | Создать матрицу типов данных, оснований обработки, сроков хранения и удаления перед расширением MVP |
| Нет idempotency model для критичных POST | Replay/double submit | Зафиксировать idempotency keys/error model в API contracts по ADR-002 |
| Production secrets/credentials для Keycloak/OpenSearch/PostgreSQL не определены | Компрометация стенда | Вынести в deployment architecture/release plan: secret management, rotation, network isolation |
| Нет backend-кода с централизованной policy | Риск расхождения реализации и документов | При реализации начать с policy tests для owner/grant/scope/deny by default |

## 13. Последствия

**Positive:**

- Уточнена граница Keycloak vs FitBridge Backend.
- Зафиксирован единый access decision model для owner access, active `AccessGrant`, scopes и deny by default.
- Security baseline связан с ADR-001, ADR-002, ADR-005 и ADR-006.

**Negative:**

- Backend обязан выполнять доменную авторизацию на каждый запрос; простой role-based access недостаточен.
- До production нужны дополнительные настройки realm, Envoy, secrets, retention и data classification.

**Risks:**

- Если security checks будут реализованы точечно в каждом handler, появится риск IDOR и broken access control.
- Если OpenSearch будет использоваться для debugging raw payloads, observability станет каналом утечки чувствительных данных.
