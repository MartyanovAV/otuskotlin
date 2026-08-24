# Обзор архитектуры FitBridge

FitBridge MVP **Trainer Diary** позволяет зарегистрированному тренеру вести клиентские карточки и тренировочные планы. Клиентский кабинет, публичный доступ и расширенный профессиональный профиль остаются за пределами MVP.

## Канонические источники

| Область | Источник |
|---|---|
| Business scope | [MVP Scope](../01-business/MVP_SCOPE_SUMMARY.md) |
| Архитектурные решения | [ADR index](./01-adrs.md), включая [ADR-007](./ADR/ADR-007-use-keycloak-as-mvp-user-profile.md) |
| Архитектурные диаграммы | [Context](./c4/C4_CONTEXT.md), [Container](./c4/C4_CONTAINER.md), [Component](./c4/C4_COMPONENT.md) |
| Модель данных | [ERD](./ERD.md) |
| Security | [Security Architecture](./SECURITY_ARCHITECTURE.md) |
| API | [API index](./02-api.md), Training OpenAPI v1/v2 |
| Backend boundaries | [Microservices plan](./04-microservices-plan.md) |
| Local deployment | [Deploy guide](../../deploy/README.md) |

## Критический путь MVP

1. Тренер переходит к регистрации, где явно указано, что создаётся аккаунт тренера.
2. Keycloak создаёт пользователя с уникальным username и ролью `TRAINER`.
3. Web UI получает OIDC tokens; access token содержит стабильный `sub`.
4. Тренер создаёт `ClientCard`; backend назначает `ownerId = sub`.
5. Тренер создаёт `TrainingPlan` для своей карточки.
6. Search/read/update/archive всегда ограничиваются текущим `ownerId`.

## Runtime-архитектура

| Контейнер / система | Назначение |
|---|---|
| Web UI (Vue 3) | OIDC login/registration и приватный кабинет тренера (ADR-008) |
| Keycloak | Identity Server: username, credentials, profile claims, `TRAINER`, JWT/JWKS |
| Caddy Gateway | REST/WS-маршрутизация и edge JWT validation, включая WebSocket Upgrade |
| Training Service | POST Full и WebSocket API, Auth Guard, ownership и бизнес-правила карточек/планов |
| PostgreSQL | `ClientCard`, `TrainingPlan`, locks и archive state |
| Fluent Bit / GreptimeDB | Masked logs и техническая наблюдаемость |

Отдельного Profile Service и локальной user/profile projection нет.

## Архитектурные принципы

- **Identity outside domain:** Keycloak владеет аккаунтом и минимальным identity-профилем.
- **Stable ownership:** `sub` отображается в `AuthPrincipal.userId` и сохраняется как `ownerId`.
- **Username is not ownership:** `preferred_username` используется для входа и отображения, но не связывает доменные данные.
- **Deny by default:** каждый приватный endpoint проверяет JWT, `TRAINER` и ownership.
- **No runtime UserInfo dependency:** профиль запрашивает UI; backend не вызывает UserInfo на каждом запросе.
- **Minimal domain:** в PostgreSQL находятся только подтверждённые бизнес-сущности.
- **Entity boundaries inside service:** Training Service собирает независимые модули `ClientCard` и `TrainingPlan`; common/biz/api/repo-*/app и миграции принадлежат соответствующей сущности, общего repo-layer сервиса нет.


## API-поверхность

- `/v1/clientCard/*`, `/v2/clientCard/*`;
- `/v1/trainingPlan/*`, `/v2/trainingPlan/*`;
- `/v1/training/ws`, `/v2/training/ws` — дополнительный WebSocket transport с теми же DTO;
- Keycloak `/realms/fit-bridge/*` для OIDC registration/login/UserInfo.

Profile API отсутствует. `ClientProfile`, `Invite`, `AccessGrant`, client registration, public endpoints и completion diary остаются Phase 2.

## Текущее состояние реализации

OpenAPI, transport/common models, мапперы, Ktor application и REST/WS endpoints Training Service находятся в tracked sources. Процессоры реализуют полноценные цепочки бизнес-логики с использованием репозиториев (PostgreSQL). Проверка JWT, извлечение стабильного `ownerId` и авторизационные проверки (`accessValidation`) встроены в backend. Структура `training-service` успешно разнесена по модулям сущностей (`client-card`, `training-plan`, `core`, `common`), изолирующим свои `biz`, `common` и `repo` слои.
