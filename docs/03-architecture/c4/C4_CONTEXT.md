# C4 Context - FitBridge

Диаграмма показывает FitBridge как приложение в окружении пользователей, IAM, внешних бизнес-процессов, операционного сопровождения пилота и платформенного observability-контура. Уровень отражает целевой MVP: два полноценных пути `Trainer-led` и `Solo-client (PLG)`; in-product `ADMIN`/support роль и support console не входят в MVP.

```mermaid
C4Context
  title Контекст системы - FitBridge MVP

  Person(client, "Клиент", "Ведёт дневник, личные программы и управляет доступами")
  Person(trainer, "Тренер", "Подключает клиентов, назначает планы и смотрит историю")
  Person(studioOwner, "Владелец микро-студии", "Phase 2: управляет командой и клиентской базой")
  Person_Ext(supportOps, "Оператор поддержки пилота", "Вне product UI/domain API; исполняет controlled runbook без доступа к чувствительным клиентским данным")

  System(fitbridge, "FitBridge", "B2B2C SaaS для client-owned fitness data, дневника, доступов и простых программ")

  System_Ext(keycloak, "Keycloak", "Identity Server: OIDC/OAuth2, пользователи, роли, JWT")
  System_Ext(opensearch, "OpenSearch + Dashboards", "Внешний/platform observability-контур: хранение, поиск и просмотр технических логов")
  System_Ext(runbook, "Controlled operational runbook", "Процедуры пилота: provisioning, block/revoke/cancel invite без sensitive payload")
  System_Ext(manualBilling, "Ручная проверка оплаты", "MVP: qualitative willingness-to-pay без продуктового биллинга")

  Rel(client, fitbridge, "Регистрируется, ведёт дневник, создаёт личную программу, выдаёт/отзывает доступ", "HTTPS/JSON")
  Rel(trainer, fitbridge, "Создаёт профиль, приглашает клиента, назначает программу, смотрит статусы", "HTTPS/JSON")
  Rel(studioOwner, fitbridge, "Пилотирует командные сценарии", "Design reserve")

  Rel(fitbridge, keycloak, "Делегирует аутентификацию и получает JWT", "OIDC/OAuth2")
  Rel(fitbridge, opensearch, "Передаёт masked structured logs во внешний observability-контур", "Fluent Bit")
  Rel(supportOps, runbook, "Исполняет утверждённые операции пилота", "Manual controlled process")
  Rel(supportOps, keycloak, "Выполняет provisioning/block/revoke по runbook", "Keycloak admin console")
  Rel(supportOps, opensearch, "Смотрит только masked technical/audit-oriented logs", "HTTPS")
  Rel(trainer, manualBilling, "Подтверждает готовность платить после пилота", "Interview/manual process")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Scope

| Входит в MVP | За пределами MVP |
|---|---|
| Регистрация клиента и тренера | Полноценный multi-specialist production-сценарий |
| Solo-client дневник и личная программа | Командный тариф студии |
| Trainer-led приглашение, доступ, назначение плана | Встроенный биллинг и автоплатежи |
| Controlled operational runbook/provisioning пилота через Keycloak без product ADMIN роли | In-product ADMIN/support UI, support console, granular operator roles |
| Базовая история, статусы выполнения и pull-model UI статусы | Отдельный `Notification` API/provider для Phase 2 |
| Инфраструктурное логирование критичных событий через ADR-006 | Продуктовый `AuditEvent` API для Phase 2 |

## Notes

- Keycloak отвечает за IAM, но доменная авторизация доступа к клиентским данным остаётся внутри FitBridge.
- Domain API FitBridge остаётся `CLIENT`/`TRAINER` only; support/operator не является участником MVP domain API и не получает broad bypass.
- Операционные действия пилота (`block user`, revoke/cancel invite edge cases, privacy/deletion support actions) выполняются через Keycloak + runbook и логируются без client profile/diary/training history/health-adjacent payload.
- OpenSearch используется FitBridge как внешний/platform observability-контур и не входит в application boundary FitBridge.
- Отдельный notification-контур не входит в MVP: статусы читаются из MVP endpoints/dashboard.
- Ручная проверка оплаты относится к бизнес-процессу MVP и не требует backend API.
