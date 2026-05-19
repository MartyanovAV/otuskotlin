# API FitBridge B2B2C SaaS

Этот документ является индексом API-документации FitBridge. Подробные контракты разбиты на небольшие файлы в папке [api/](./api/), чтобы MVP, Phase 2 и design reserve не смешивались в одной большой спецификации.

## Как читать

Для Gate 1 в первую очередь нужны:

1. [Разделение scope](./api/01-scope.md)
2. [MVP API Entities](./api/02-mvp-entities.md)
3. [MVP Profile and Access API Methods](./api/04-mvp-profile-access-methods.md)
4. [MVP Diary, Plan and Audit API Methods](./api/05-mvp-diary-plan-audit-methods.md)
5. [API Business Rules](./api/07-business-rules.md)
6. [API Metrics and Limits](./api/08-metrics-and-limits.md)

Phase 2 и design reserve вынесены отдельно:

- [Phase 2 and Design Reserve API Entities](./api/03-future-entities.md)
- [Phase 2 and Design Reserve API Methods](./api/06-phase2-design-reserve-methods.md)

## MVP / Gate 1

Сокращённый MVP реализует только критический путь:

1. Тренер регистрируется и создаёт профиль.
2. Тренер приглашает клиента.
3. Клиент принимает приглашение и явно выдаёт доступ.
4. Тренер создаёт простой индивидуальный план.
5. Тренер назначает план клиенту.
6. Клиент отмечает выполнение.
7. Тренер видит историю и статусы выполнения в карточке клиента.

В Gate 1 не входят: биллинг, тарифные лимиты, замеры, check-in, шаблоны программ, отдельный отчётный модуль, adherence-аналитика, расширенные уведомления, multi-specialist и team-management.

## Файлы

| Документ | Назначение |
|----------|------------|
| [01-scope.md](./api/01-scope.md) | Правила разделения MVP, Phase 2 и design reserve |
| [02-mvp-entities.md](./api/02-mvp-entities.md) | Сущности обязательного MVP |
| [03-future-entities.md](./api/03-future-entities.md) | Сущности Phase 2 и design reserve |
| [04-mvp-profile-access-methods.md](./api/04-mvp-profile-access-methods.md) | Профили, онбординг, приглашения и доступы |
| [05-mvp-diary-plan-audit-methods.md](./api/05-mvp-diary-plan-audit-methods.md) | Дневник, простые планы, назначения, выполнение и audit |
| [06-phase2-design-reserve-methods.md](./api/06-phase2-design-reserve-methods.md) | Аналитика, биллинг, уведомления, multi-specialist и team-management |
| [07-business-rules.md](./api/07-business-rules.md) | Бизнес-правила по доменам и scope |
| [08-metrics-and-limits.md](./api/08-metrics-and-limits.md) | Метрики успеха, performance targets, rate limits и надёжность |
