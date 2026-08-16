# Requirements Traceability Matrix

Документ связывает текущий trainer-only MVP FitBridge с бизнес-, функциональными и нефункциональными требованиями и приёмочными проверками. Канонический бизнес-источник: [BR-010-trainer-diary-mvp.md](../01-business/BR/BR-010-trainer-diary-mvp.md).

## Статус

| Параметр | Значение |
|---|---|
| Статус | Утверждена для MVP / справочный источник трассировки |
| Область | Gate 1 «Trainer Diary» без клиентского доступа |
| MVP-путь | `Trainer signup → ClientCard → TrainingPlan → search/reuse` |
| Пользователь MVP | Только аутентифицированный тренер |
| Источники | `docs/01-business`, `docs/02-analysis`, `docs/03-architecture/api`, OpenAPI v1/v2 |

`Ready` означает готовность трассировки требования, а не завершённость production-реализации.

## MVP Traceability

| Trace ID | MVP capability | Funnel stage | BR / AC | FR / NFR | Tests | Status |
|---|---|---|---|---|---|---|
| RTM-001 | Регистрация и вход тренера | trainer signup | [BR-006](../01-business/BR/BR-006-trainer-onboarding.md), `US-R006-001` | FR `1.1`; NFR `1.1`, `3.1` | `TEST-E2E-001`: форма явно обозначает trainer account; уникальный username входит и получает `TRAINER` | Ready |
| RTM-002 | Создание минимальной `ClientCard` | client card | [BR-010](../01-business/BR/BR-010-trainer-diary-mvp.md), `US-R010-001` | FR `2.1`; NFR `1.2`, `3.2` | `TEST-E2E-002`: тренер создаёт карточку без клиентского аккаунта | Ready |
| RTM-003 | Поиск и просмотр своих `ClientCard` | trainer return | [BR-010](../01-business/BR/BR-010-trainer-diary-mvp.md), `US-R010-002` | FR `2.2`; NFR `1.2`, `2.1`, `3.2` | `TEST-E2E-003`: поиск возвращает только карточки владельца | Ready |
| RTM-004 | Создание `TrainingPlan` для своей карточки | plan | [BR-010](../01-business/BR/BR-010-trainer-diary-mvp.md), `US-R010-003` | FR `3.1`; NFR `1.3`, `2.2` | `TEST-E2E-004`: план создаётся только для карточки того же тренера | Ready |
| RTM-005 | Поиск и просмотр своих `TrainingPlan` | search/reuse | [BR-010](../01-business/BR/BR-010-trainer-diary-mvp.md), `US-R010-004` | FR `3.2`; NFR `1.3`, `2.1`, `3.2` | `TEST-E2E-005`: фильтры работают и не раскрывают планы другого владельца | Ready |
| RTM-006 | Изменение и архивирование карточек/планов | lifecycle | [BR-010](../01-business/BR/BR-010-trainer-diary-mvp.md), supporting API contract | FR `2.3`, `3.3`; NFR `2.2`, `2.3`, `4.2` | `TEST-E2E-006`: update учитывает lock; archive переводит объект в архивное состояние | Ready |
| RTM-007 | Минимизация данных, JWT, ownership и безопасные логи | all private stages | [BR-002](../01-business/BR/BR-002-access-control.md), [BR-009](../01-business/BR/BR-009-consent-privacy-deletion.md), `US-R009-001`, `US-R009-003` | FR `1.2`, `4.1`–`4.3`; NFR `3.1`–`3.3`, `5.3` | `TEST-NEG-001`: no-token/cross-owner запросы отклоняются; payload не попадает в логи | Ready |
| RTM-008 | Проверка активации, возврата и willingness-to-pay | MVP validation | [BR-010](../01-business/BR/BR-010-trainer-diary-mvp.md), [BR-007](../01-business/BR/BR-007-billing-subscriptions.md) | FR `6.1`, `6.2`; NFR `6.1`, `6.2` | `TEST-BIZ-001`: метрики пилота и ≥5 подтверждений готовности платить фиксируются вне продуктового биллинга | Ready |

## Coverage By MVP Funnel

| Этап | Covered traces | Gate 1 acceptance focus |
|---|---|---|
| trainer signup | `RTM-001` | Только тренер получает приватный рабочий контур |
| client card | `RTM-002`, `RTM-003` | Карточка создаётся и находится без регистрации клиента |
| plan | `RTM-004` | План связан с карточкой того же владельца |
| search/reuse | `RTM-005`, `RTM-006` | Тренер возвращается к объектам и управляет их жизненным циклом |
| security/privacy | `RTM-007` | JWT, ownership и data minimization действуют на всём пути |
| validation | `RTM-008` | Проверяются активация, возврат и willingness-to-pay |

## Явно отложенные возможности

Публичные ссылки/capability-token, клиентская регистрация и кабинет, отметки выполнения/`COMPLETED`, `AccessGrant`, multi-specialist, биллинг, медиа и расширенная аналитика не имеют MVP trace и не должны появляться в OpenAPI или runtime без отдельного изменения scope.

## Известные разрывы

| Gap | Impact | Recommended action |
|---|---|---|
| Business processor пока ориентирован на stub/test путь, production persistence и ownership policy не завершены | Контракты компилируются, но end-to-end бизнес-сценарий ещё не production-ready | Реализовать repository/authorization chains и заменить плановые `TEST-*` ссылками на реальные тесты |
| Нет утверждённой минимальной event taxonomy пилота | Activation/return метрики могут считаться неодинаково | До инструментирования согласовать названия событий и поля без пользовательского payload |
| Supporting update/archive операции шире минимальных P0 user stories BR-010 | Возможен незаметный scope creep | Считать их техническим lifecycle-support, не включать в Gate 1 value hypothesis без отдельного решения |

## Maintenance Rules

1. Каждое новое MVP-требование получает строку `RTM-*`, ссылку на BR и проверку.
2. Клиентский или публичный контур не добавляется без отдельного изменения `BR-010`, FR/NFR, RTM, security architecture и OpenAPI.
3. При реализации приёмки плановые `TEST-*` заменяются ссылками на реальные test class/method.
4. Phase 2 требования остаются только как constraint, gap или явная граница scope.
