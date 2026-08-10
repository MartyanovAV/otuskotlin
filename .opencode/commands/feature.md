---
description: Feature Track — автоматический выбор Lite или Full по влиянию фичи
agent: orchestrator
---
Сначала классифицируй изменение.

Feature Lite — только если поведение полностью укладывается в утверждённые требования, API, БД, security/privacy и границы сервисов:
1. Developer → реализация с Inner Loop.
2. Critic → Gate review полного change set.
3. При REJECT → Developer исправляет findings; максимум три цикла.
4. После APPROVE → QA проверяет поведение и негативные сценарии.
5. Верни результат пользователю без Gate 1 и без новых BR/ADR.

Feature Full — если меняется хотя бы один из перечисленных контрактов или создаётся сервис:
1. Product Owner → бизнес-требования и acceptance criteria.
2. Architect → техническое решение.
3. Gate 1 → human approval.
4. Developer → реализация с Inner Loop.
5. Critic → Gate review; цикл исправлений максимум три раза.
6. После APPROVE → QA.
7. Gate 2 → human acceptance; это не deploy approval.

Задача: $ARGUMENTS
