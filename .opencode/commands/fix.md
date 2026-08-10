---
description: Fast Track — локальное исправление через владельца затронутой области
agent: orchestrator
---
Задача поступила через Fast Track. Убедись, что она не меняет business scope, публичный API, БД, security/privacy или границы сервисов; иначе переведи её в Feature Full.

Выбери Owner по области:
- Kotlin/Ktor/Gradle → Backend Developer;
- `ux-prototype/**` или существующий production frontend → Frontend Developer;
- CI/CD, Docker, observability → DevOps PREPARATION;
- test-only → QA;
- архитектурное исправление без production-кода → Architect;
- deployment → остановись: он разрешён только через `/deploy`.

После изменения вызови Critic в Gate review для полного change set. При REJECT верни findings Owner и повтори максимум три раза. QA после APPROVE вызывай только для изменения поведения с существенным риском. `REVIEW_REPORT.md` не создавай.
Задача: $ARGUMENTS
