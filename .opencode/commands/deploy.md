---
description: Отдельный безопасный Deploy Track с preflight и явным подтверждением
agent: orchestrator
---
Запусти Deploy Track. Это отдельный поток и он НЕ является продолжением `/feature`.
1. Уточни environment, version/ref и способ rollout из аргументов или у пользователя.
2. Вызови DevOps только в режиме PREFLIGHT_ONLY.
3. Покажи deployment plan, health checks и rollback plan.
4. Запроси отдельное явное подтверждение точных environment и version/ref.
5. Только после подтверждения вызови DevOps в режиме EXECUTION с `DEPLOY_APPROVED: true`.
Параметры: $ARGUMENTS
