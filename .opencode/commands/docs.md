---
description: Обновление документации через владельца домена и Critic review
agent: orchestrator
---
Определи владельца документа:
- business/analysis → Product Owner;
- API/ADR/ERD/C4/security architecture → Architect;
- deployment/runbook/CI → DevOps PREPARATION;
- README/KDoc конкретной реализации → соответствующий Developer.

Если меняется product scope или исполняемый API-контракт, останови Docs Track и переведи задачу в Feature Full.
После изменения обязательно вызови Critic: проверь фактическую согласованность с кодом и соседними docs, traceability, ссылки, термины, структуру и общий стиль. При REJECT верни findings владельцу; максимум три цикла. QA для docs-only изменения не вызывай. `REVIEW_REPORT.md` не создавай.
Задача: $ARGUMENTS
