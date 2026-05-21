# C4 Context / Контекст системы - FitBridge

Диаграмма фиксирует границы системы и внешних акторов MVP. Подробные правила доступа, данных и аудита не дублируются здесь: см. [Security Architecture](../SECURITY_ARCHITECTURE.md), [ERD](../ERD.md), [API index](../02-api.md) и [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md).

```mermaid
C4Context
  title Контекст системы - FitBridge MVP

  Person(client, "Клиент", "Ведёт дневник, личные программы и управляет доступами")
  Person(trainer, "Тренер", "Подключает клиентов, назначает планы и смотрит историю")
  Person(studioOwner, "Владелец микро-студии", "Phase 2: управляет командой и клиентской базой")
  Person_Ext(supportOps, "Оператор поддержки пилота", "Вне product UI/domain API; controlled runbook без доступа к sensitive payload")

  System(fitbridge, "FitBridge", "B2B2C SaaS для client-owned fitness data, дневника, доступов и простых программ")

  System_Ext(keycloak, "Keycloak", "Identity Server: OIDC/OAuth2, пользователи, роли, JWT")
  System_Ext(opensearch, "OpenSearch + Dashboards", "Внешний/platform observability-контур")
  System_Ext(runbook, "Controlled operational runbook", "Процедуры пилота без domain API bypass")
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

## Scope MVP

| Входит в MVP | За пределами MVP |
|---|---|
| Регистрация клиента и тренера | Полноценный multi-specialist production-сценарий |
| Solo-client дневник и личная программа | Командный тариф студии |
| Trainer-led приглашение, доступ, назначение плана | Встроенный биллинг и автоплатежи |
| Controlled operational runbook через Keycloak без product `ADMIN` роли | In-product ADMIN/support UI, support console, granular operator roles |
| Базовая история, статусы выполнения и pull-model UI статусы | Отдельный `Notification` API/provider для Phase 2 |
| Инфраструктурное логирование критичных событий через ADR-006 | Продуктовый `AuditEvent` API |

## Ответственность и ссылки

| Тема | Канонический источник |
|---|---|
| Политика доступа, роли, JWT, граница support | [SECURITY_ARCHITECTURE.md](../SECURITY_ARCHITECTURE.md) |
| Данные и статусы `Invite`/`AccessGrant` | [ERD.md](../ERD.md) |
| API MVP и pull-model статусы | [../02-api.md](../02-api.md) |
| Внешний/platform observability-контур | [ADR-006](../ADR/ADR-006-use-opensearch-fluent-bit-observability.md) |
