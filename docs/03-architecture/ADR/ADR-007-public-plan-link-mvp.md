# ADR-007: Зафиксировать MVP с публичной ссылкой на план как capability-token доступ без клиентской регистрации

## Контекст

Реализует [BR-010](../../01-business/BR/BR-010-public-plan-link-mvp.md) и уточняет архитектурные границы [MVP_SCOPE_SUMMARY](../../01-business/MVP_SCOPE_SUMMARY.md).

Утверждённый MVP должен проверить trainer-first B2B-ценность: тренер — единственный зарегистрированный пользователь, создаёт `ClientCard`, создаёт `TrainingPlan`, получает публичную ссылку, клиент без регистрации открывает план и оставляет `CompletionMark`, тренер видит статус и может закрыть доступ по ссылке.

Ранее архитектурные документы ориентировались на более широкую client-owned модель с `ClientProfile`, `Invite`, `AccessGrant`, дневником и solo-client путём. Эти концепты остаются стратегическим направлением, но не входят в MVP публичного доступа к плану.

## Сравнение

Сравнение вариантов относительно [BR-010](../../01-business/BR/BR-010-public-plan-link-mvp.md):

| Критерий | [Вариант A: публичный доступ через capability-token](../proposals/OPT-A-public-plan-access.md) | Вариант B: клиентский аккаунт + `AccessGrant` сразу | Вариант C: обычная share-ссылка без lifecycle |
|----------|:---:|:---:|:---:|
| Клиент без регистрации | ✅ | ❌ | ✅ |
| Time-to-first-plan-link | ✅ | ⚠️ | ✅ |
| Тренер может закрыть доступ | ✅ | ✅ | ❌/⚠️ |
| Не расширяет MVP до client-owned модели | ✅ | ❌ | ✅ |
| Guardrails для public access | ✅ | ✅ | ❌ |
| Путь к будущим `ClientProfile`/`Invite`/`AccessGrant` | ✅ | ✅ | ⚠️ |
| Стоимость реализации MVP | Low/Medium | High | Low |
| Privacy/security риск | Medium при guardrails | Low/Medium | High |

## Решение

**Выбрано:** вариант A — публичный доступ через capability-token к конкретному `TrainingPlan`.

Архитектурно фиксируем:

- минимальные доменные концепты MVP: `ClientCard`, `TrainingPlan`, `CompletionMark`;
- `CompletionMark` допустимо моделировать как дочерний объект/value object внутри `TrainingPlan` на MVP;
- публичная ссылка не является самостоятельной продуктовой сущностью MVP;
- public-link lifecycle хранится как техническое состояние `TrainingPlan`: token hash, status, TTL, revoke timestamps;
- полноценные `ClientProfile`, `Invite`, `AccessGrant`, client-owned history и клиентский кабинет являются Phase 2 / future scope.

## Обоснование

Выбранный вариант напрямую поддерживает утверждённый бизнес-сценарий BR-010:

- тренер быстрее получает первую ценность и проверяет willingness-to-pay;
- клиент не проходит регистрацию ради одного плана и отметки выполнения;
- система сохраняет минимальную модель данных и не строит преждевременную client-owned инфраструктуру;
- закрытие ссылки тренером покрывает MVP-потребность управления доступом без `AccessGrant`;
- separation между MVP с публичной ссылкой и future access-grant моделью снижает риск неверной миграции security semantics.

## Последствия

**Положительные:**
- MVP scope становится согласованным с PO-документами и не требует регистрации клиента.
- Доменная модель упрощается до `ClientCard`, `TrainingPlan`, `CompletionMark`.
- Путь к future client-owned модели сохраняется через явные migration/evolution точки.

**Отрицательные:**
- Клиент в MVP не владеет историей и не управляет доступом самостоятельно.
- Публичный token становится критичным security boundary.
- Нужно строго ограничить публичный payload, иначе публичная ссылка может раскрывать лишние данные.

**Риски:**

| Риск | Вероятность | Влияние | Митигация |
|------|------------|--------|------------|
| Raw token попадёт в БД или логи | Medium | High | Хранить только hash; запрет логирования raw token, URL query, Authorization/header payload; masked structured logs |
| Перебор/abuse публичных ссылок | Medium | High | Длинный random token, TTL, rate limiting по IP/fingerprint, generic errors |
| IDOR через `clientId`/`planId` в публичном API | Medium | High | Public endpoint принимает только token; внутренние id не передаются клиентом |
| Ссылка остаётся активной после завершения работы | Medium | Medium/High | Обязательный revoke/close action тренера; `publicAccessStatus` проверяется на каждый public request |
| MVP незаметно превратится в client-owned scope | Medium | Medium | `ClientProfile`, `Invite`, `AccessGrant`, клиентский кабинет и solo-client путь явно помечены Phase 2 |
| Сложная будущая миграция истории | Medium | Medium | Хранить completion marks со stable ids/date/status; не смешивать public token с future grant |
