# Бизнес-документация FitBridge

## Структура

| Документ | Описание |
|----------|----------|
| [BUSINESS_VISION.md](BUSINESS_VISION.md) | Общее видение продукта, проблематика, стратегия и дорожная карта |
| [CUSTOMER_PERSONAS.md](CUSTOMER_PERSONAS.md) | Персоны целевой аудитории: тренеры, клиенты, владельцы студий |
| [CJM.md](CJM.md) | Customer Journey Map — карта пути клиента по ключевым сценариям |
| [MONETIZATION_STRATEGY.md](MONETIZATION_STRATEGY.md) | Модель монетизации, тарифы, unit-экономика |
| [GTM_PLAN.md](GTM_PLAN.md) | Go-To-Market план: каналы, стратегия запуска, метрики |
| [MVP_SCOPE_SUMMARY.md](MVP_SCOPE_SUMMARY.md) | Границы и состав MVP |
| [RISK_REGISTER.md](RISK_REGISTER.md) | Реестр рисков с вероятностью, влиянием и планами митигации |
| [GLOSSARY.md](GLOSSARY.md) | Глоссарий ключевых бизнес-терминов |
| [BR/](BR/) | Бизнес-требования (Business Requirements) |

### Бизнес-требования (BR)

| Документ | Описание |
|----------|----------|
| [BR-001-training-diary.md](BR/BR-001-training-diary.md) | Клиентский тренировочный дневник |
| [BR-002-access-control.md](BR/BR-002-access-control.md) | Управление доступом тренер-клиент |
| [BR-003-multi-specialist.md](BR/BR-003-multi-specialist.md) | Мульти-специалист: работа с несколькими тренерами |
| [BR-004-program-builder.md](BR/BR-004-program-builder.md) | Конструктор тренировочных программ |
| [BR-005-progress-analytics.md](BR/BR-005-progress-analytics.md) | Аналитика прогресса клиента |
| [BR-006-trainer-onboarding.md](BR/BR-006-trainer-onboarding.md) | Онбординг тренера |
| [BR-007-billing-subscriptions.md](BR/BR-007-billing-subscriptions.md) | Биллинг и подписки |
| [BR-008-team-management.md](BR/BR-008-team-management.md) | Управление командой (студия) |

## Как создать новый BR

1. Скопируйте структуру из существующего BR (например, [BR-001-training-diary.md](BR/BR-001-training-diary.md)).
2. Присвойте следующий порядковый номер: `BR-NNN-краткое-название.md`.
3. Заполните обязательные секции:
   - **Статус** — таблица с параметрами утверждения.
   - **Business Value** — краткое описание бизнес-ценности.
   - **User Stories** — пользовательские истории в формате As a / I want / So that с Acceptance Criteria.
   - **System Constraints** — технические и системные ограничения.
   - **Metrics** — метрики успеха для требования.
   - **Out of Scope** — явно указанные границы: что не входит в данное требование.
4. Отправьте на ревью через Pull Request.

## Процесс ревью

- Product Owner (PO) проверяет и утверждает каждый BR.
- Статус утверждения фиксируется в таблице «Статус» внутри документа.
- Все изменения отслеживаются через Git: история правок доступна в коммитах и PR.

## Периодичность пересмотра

| Тип документа | Периодичность |
|---------------|---------------|
| Стратегические документы (Vision, Monetization, GTM) | Ежеквартально |
| Бизнес-требования (BR) | При изменении scope |
| Risk Register | Ежемесячный обзор индикаторов |

## Определения и глоссарий

Определения ключевых бизнес-терминов — см. [GLOSSARY.md](GLOSSARY.md).
