# API FitBridge B2B2C SaaS

Этот документ является индексом API-документации FitBridge. Подробные контракты разбиты на небольшие файлы в папке [api/](./api/).

## Как читать

Для Gate 1 в первую очередь нужны:

1. [Разделение scope](./api/01-scope.md)
2. [MVP API Entities](./api/02-mvp-entities.md)
3. [MVP Profile and Access API Methods](./api/04-mvp-profile-access-methods.md)
4. [MVP Diary, Plan  API Methods](./api/05-mvp-diary-plan-methods.md)
5. [API Business Rules](./api/07-business-rules.md)
6. [API Metrics and Limits](./api/08-metrics-and-limits.md)

## MVP / Gate 1

Сокращённый MVP реализует два критических пути:

**Путь 1: Trainer-led**
1. Тренер регистрируется и создаёт профиль.
2. Тренер приглашает клиента.
3. Клиент принимает приглашение и явно выдаёт доступ.
4. Тренер создаёт простой индивидуальный план.
5. Клиент отмечает выполнение, тренер видит статусы выполнения.

**Путь 2: Solo-client (PLG)**
1. Клиент самостоятельно регистрируется.
2. Создаёт себе индивидуальный план.
3. Отмечает выполнение.

## Файлы

| Документ | Назначение |
|----------|------------|
| [01-scope.md](./api/01-scope.md) | Описание состава MVP |
| [02-mvp-entities.md](./api/02-mvp-entities.md) | Сущности обязательного MVP |
| [04-mvp-profile-access-methods.md](./api/04-mvp-profile-access-methods.md) | Профили, онбординг, приглашения и доступы |
| [05-mvp-diary-plan-methods.md](./api/05-mvp-diary-plan-methods.md) | Дневник, простые планы, назначения, выполнение  |
| [07-business-rules.md](./api/07-business-rules.md) | Бизнес-правила MVP |
| [08-metrics-and-limits.md](./api/08-metrics-and-limits.md) | Метрики успеха, performance targets, rate limits и надёжность |
