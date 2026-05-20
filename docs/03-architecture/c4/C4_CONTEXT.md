# C4 Context - FitBridge

Диаграмма показывает FitBridge как систему в окружении пользователей и внешних инфраструктурных сервисов. Уровень отражает целевой MVP: два полноценных пути `Trainer-led` и `Solo-client (PLG)`.

```mermaid
C4Context
  title System Context - FitBridge MVP

  Person(client, "Клиент", "Ведёт дневник, личные программы и управляет доступами")
  Person(trainer, "Тренер", "Подключает клиентов, назначает планы и смотрит историю")
  Person(studioOwner, "Владелец микро-студии", "Phase 2: управляет командой и клиентской базой")
  Person(admin, "Администратор / Support", "Поддерживает пользователей и разбирает инциденты")

  System(fitbridge, "FitBridge", "B2B2C SaaS для client-owned fitness data, дневника, доступов и простых программ")

  System_Ext(keycloak, "Keycloak", "Identity Server: OIDC/OAuth2, пользователи, роли, JWT")
  System_Ext(opensearch, "OpenSearch + Dashboards", "Хранение, поиск и просмотр технических логов")
  System_Ext(manualBilling, "Ручная проверка оплаты", "MVP: qualitative willingness-to-pay без продуктового биллинга")

  Rel(client, fitbridge, "Регистрируется, ведёт дневник, создаёт личную программу, выдаёт/отзывает доступ", "HTTPS/JSON")
  Rel(trainer, fitbridge, "Создаёт профиль, приглашает клиента, назначает программу, смотрит статусы", "HTTPS/JSON")
  Rel(studioOwner, fitbridge, "Пилотирует командные сценарии", "Design reserve")
  Rel(admin, fitbridge, "Разбирает обращения и инциденты", "Admin/support workflow")

  Rel(fitbridge, keycloak, "Делегирует аутентификацию и получает JWT", "OIDC/OAuth2")
  Rel(fitbridge, opensearch, "Отправляет технические и audit-oriented логи", "Fluent Bit")
  Rel(trainer, manualBilling, "Подтверждает готовность платить после пилота", "Interview/manual process")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Scope

| Входит в MVP | За пределами MVP |
|---|---|
| Регистрация клиента и тренера | Полноценный multi-specialist production-сценарий |
| Solo-client дневник и личная программа | Командный тариф студии |
| Trainer-led приглашение, доступ, назначение плана | Встроенный биллинг и автоплатежи |
| Базовая история и статусы выполнения | Отдельный Notification API |
| Инфраструктурное логирование критичных событий | Расширенный AuditEvent API |

## Notes

- Keycloak отвечает за IAM, но доменная авторизация доступа к клиентским данным остаётся внутри FitBridge.
- OpenSearch используется как инфраструктурный контур логирования; продуктовый аудит `AuditEvent` перенесён в Phase 2.
- Ручная проверка оплаты относится к бизнес-процессу MVP и не требует backend API.
