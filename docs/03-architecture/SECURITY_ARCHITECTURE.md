# Архитектура безопасности FitBridge Trainer Diary MVP

Security baseline распространяется на trainer-only кабинет с `ClientCard` и `TrainingPlan`. Клиентская регистрация, публичный API и расширенный профиль тренера не входят в MVP.

## 1. Модель доверия

| Актор / система | Ответственность | Граница доверия |
|---|---|---|
| Trainer | Регистрируется, входит, управляет своими карточками и планами | Не передаёт доверенный owner id или роль |
| Keycloak | Username, credentials, identity profile, `TRAINER`, OIDC/JWT/JWKS | Не решает domain ownership |
| Envoy | Edge-проверка подписи, issuer и audience | Не заменяет backend authorization |
| Training Service | Повторная JWT validation, role и ownership guards, business rules | Единственная точка доменной авторизации |
| PostgreSQL | Trainer-owned карточки и планы | Не хранит credentials или локальную user projection |
| Fluent Bit / GreptimeDB | Masked technical logs | Не получает JWT или пользовательские payload |

## 2. Registration и login

- Self-registration создаёт аккаунт тренера с уникальным Keycloak username.
- UI и Keycloak theme явно показывают, что создаётся аккаунт тренера.
- `TRAINER` назначается серверной default role; форма не принимает произвольную роль.
- В MVP вход выполняется по username. Вход по email и изменение username отключены.
- Клиентская роль и регистрация отсутствуют до отдельного Phase 2 решения.

## 3. Access token contract

Прикладные claims:

```yaml
sub: stable-keycloak-subject
preferred_username: coach_ivan
realm_access:
  roles: [TRAINER]
```

Токен также содержит стандартные protocol claims (`iss`, `aud`, `exp`, `iat`, `nbf`, `jti`, `scope`). Имя, email и профессиональные поля не включаются в access token; UI получает identity profile через ID Token/UserInfo.

Backend проверяет:

1. Подпись по JWKS.
2. Точное совпадение `iss`.
3. Наличие ожидаемого `aud=fit-bridge-service`.
4. `exp`, `nbf` и допустимый clock skew.
5. Непустой `sub` и `preferred_username`.
6. Наличие `TRAINER` в `realm_access.roles`.

Envoy выполняет edge-проверку, но backend повторяет security-critical validation самостоятельно.

## 4. Ownership algorithm

- `AuthPrincipal.userId = JWT.sub`.
- Создаваемая сущность получает `ownerId` только из principal.
- Read/update/archive возвращают uniform `NOT_FOUND` или `FORBIDDEN` без раскрытия чужого объекта.
- Search всегда содержит обязательный predicate `ownerId = principal.userId`.
- При создании плана backend в транзакции проверяет ownership карточки и сохраняет тот же `ownerId` в плане.
- Username никогда не используется как ключ владения.

## 5. IAM status и revoke

Keycloak `enabled` является источником IAM-статуса. Локального `ACTIVE/BLOCKED/DELETED` нет.

Self-contained access token остаётся действительным до `exp`, даже если аккаунт отключён после выпуска. MVP ограничивает окно коротким access-token TTL. Немедленный revoke потребует introspection, deny-list или отдельного session-state решения и относится к дальнейшему security hardening.

## 6. Минимизация данных

| Область | Правило |
|---|---|
| Keycloak profile | Только username, email и стандартные имя/фамилия; specialization не обязательна |
| ClientCard | Отображаемое имя/псевдоним, короткая заметка, технические статусы |
| TrainingPlan | Простой план без медданных, body metrics и rich-media |
| Logs | Не писать JWT, username, email, ФИО, request body, заметки и содержимое плана |
| Errors | Не раскрывать существование чужих объектов и внутренние auth details |

## 7. Audit-oriented logging

Разрешённые поля: `timestamp`, `level`, `service`, `environment`, `requestId`, `actorType`, `ownerId`, `action`, `entityType`, `entityId`, `result`, `reasonCode`, `durationMs`.

Обязательные события:

- `trainer.authenticated` / `trainer.authFailed`;
- `clientCard.create/read/update/archive/search`;
- `trainingPlan.create/read/update/archive/search`;
- `access.denied` с безопасным reason code.

## 8. Threat model

| Угроза | Влияние | Митигация |
|---|---|---|
| Подстановка чужого id | Чтение/изменение чужих данных | Mandatory owner predicate и cross-owner tests |
| Подделка роли на регистрации | Получение лишних прав | Default role назначается Keycloak, role input отсутствует |
| Использование username как owner | Потеря связи при lifecycle-изменениях | Ownership только по `sub` |
| Token replay после блокировки | Доступ до истечения токена | TTL 5 минут, TLS, refresh/session revoke |
| Sensitive access token/logs | Раскрытие PII | Минимальные claims, запрет raw token и payload logging |
| Ошибка audience/issuer | Приём токена другого клиента/realm | Строгая проверка `iss` и `aud` в Envoy и backend |
| Scope creep Keycloak profile | IAM превращается в domain DB | Короткие identity attributes; сложный профиль через новый ADR |

## 9. Security acceptance criteria

- Регистрация явно обозначена как trainer registration.
- Duplicate username отклоняется.
- Access token содержит `sub`, `preferred_username`, audience и `TRAINER`, но не ФИО/email.
- UserInfo возвращает тот же `sub`, что и access token.
- No-token, wrong-role и cross-owner операции отклоняются.
- Поиск никогда не возвращает ресурсы другого `sub`.
- Raw JWT и пользовательские payload отсутствуют в логах.
