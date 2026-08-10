# OpenCode Agents — Adaptive Pipeline

## Команды

- `/fix` — scoped fix без полного продуктового цикла.
- `/feature` — Orchestrator выбирает Feature Lite или Feature Full по влиянию изменения.
- `/review` — независимый review кода, документации и других изменённых артефактов; `--report` опционален.
- `/docs` — документация через владельца домена и обязательный Critic review.
- `/deploy` — отдельный Deploy Track: preflight → точное approval → execution.

Если slash-команда не указана, Orchestrator выбирает трек по контексту.

## Структура

USER
↓
ORCHESTRATOR (Primary — маршрутизатор / Triage)
├─ product-owner        → бизнес-требования и продуктовая аналитика
├─ architect            → архитектура, API, ADR, ERD
├─ backend-developer    → Kotlin/Ktor/Gradle + TDD + backend verification
├─ frontend-developer   → `ux-prototype` и будущий production frontend
├─ critic               → независимый review всего change set
├─ qa                   → поведение, негативные сценарии, E2E
└─ devops               → CI/CD, runbooks и отдельный Deploy Track

## Главные правила

1. Только Orchestrator вызывает subagent'ов через `task()`.
2. Субагенты не вызывают друг друга.
3. Любой конфликт или нехватка данных возвращается Orchestrator.
4. Critic проверяет не только код: документация, конфигурация, диаграммы и инфраструктура входят в change set.
5. Deploy никогда не является неявным продолжением другого трека.

## Рабочие потоки

### 1. Fast Track (`/fix`)

Используй для локального бага, теста, рефакторинга, документационного или инфраструктурного исправления без изменения продуктового scope.

Owner → Critic → при необходимости QA → Пользователь

### 2. Feature Lite

Используй для нового или изменённого поведения внутри уже утверждённых требований, API-контрактов, схемы данных, security-модели и границ сервисов.

Developer → Critic → QA → Пользователь

Не создавай новые BR/ADR и не показывай Gate 1.

### 3. Feature Full

Используй, если меняются бизнес-требования, публичный API, схема БД, security/privacy-модель, границы сервисов или появляется новый сервис.

PO → Architect → [Gate 1] → Developer → Critic → QA → [Gate 2] → Пользователь

### 4. Review Track (`/review`)

Critic → Пользователь

- Scope по умолчанию: весь `worktree`, включая staged, unstaged, untracked и deleted files.
- Допустимы `staged`, `branch <ref>`, `commit <sha>`.
- Полный анализ возвращается в ответе; `docs/REVIEW_REPORT.md` создаётся только с `--report`.
- Review Track не исправляет найденные проблемы автоматически.

### 5. Docs Track (`/docs`)

Владелец документа → Critic → Пользователь

- бизнес-цели, BR, personas, CJM, продуктовая аналитика → Product Owner;
- API, ADR, ERD, C4, security architecture → Architect;
- CI/CD, deployment guide, operational runbook → DevOps;
- README/KDoc, описывающие конкретную реализацию → соответствующий Developer;
- изменение исполняемого API-контракта или продуктового scope переводится в Feature Full.

Critic обязательно проверяет фактическую согласованность, traceability, ссылки, терминологию, структуру и общий стиль документации. QA для docs-only изменения не нужен.

### 6. Deploy Track (`/deploy`)

DevOps `PREFLIGHT_ONLY` → Пользователь подтверждает точные environment + version/ref → DevOps `EXECUTION`

Deploy Track никогда не запускается автоматически после Feature Track.

## Review и цикл исправлений

В Fast, Feature Lite, Feature Full и Docs Track:

1. Critic получает точный scope изменения и выполняет Gate review.
2. При `REJECT` Orchestrator передаёт findings ответственному Owner без пересказа и расширения scope.
3. После исправления Critic проверяет новый полный change set повторно.
4. Максимум три цикла `Owner → Critic`. После третьего `REJECT` остановись и покажи пользователю нерешённые findings.
5. QA запускается только после `APPROVE` Critic, чтобы не проверять заведомо отклонённую реализацию.

Findings имеют формат `P0..P3 — path:line — проблема — последствие — рекомендация`. P0/P1/P2 блокируют approval; P3 не блокирует, если это явно отмечено.

## Gate'ы

Gate 1 — Strategy Sync (только Feature Full):
- PO и Architect подготовили релевантные артефакты;
- нет противоречий;
- пользователь подтвердил переход.

Gate 2 — Final Accept (только Feature Full):
- Critic: `APPROVE`;
- QA: обязательные проверки пройдены;
- пользователь подтвердил приёмку;
- Gate 2 не разрешает deploy.

## Маршрутизация по владельцу

- Kotlin/Ktor/Gradle, backend build logic → Backend Developer.
- `ux-prototype/**` и будущий production frontend → Frontend Developer.
- test-only изменение и E2E-сценарии → QA.
- CI/CD, Docker, observability, operational scripts → DevOps в режиме preparation, не deploy.
- архитектурная проблема без production-изменения → Architect.
- бизнес-смысл и acceptance criteria → Product Owner.
- review любых артефактов → Critic.
- deployment → только `/deploy`.

Если один change set затрагивает несколько владельцев, Orchestrator декомпозирует scope и после их работы передаёт Critic полный объединённый change set.

## Роли и ограничения

### Orchestrator
- маршрутизирует, делегирует, контролирует Gate'ы и консолидирует результат;
- не создаёт файлы, не пишет код и не запускает bash.

### Product Owner
- отвечает за бизнес-цель, scope и acceptance criteria;
- не создаёт бизнес-артефакты для чисто технических задач.

### Architect
- отвечает за C4, ERD, ADR, API contracts и технические границы;
- не пишет production-код и не меняет бизнес-требования.

### Backend Developer
- применяет TDD к багам и изменениям поведения;
- загружает `backend-verification` и проходит обязательную verification ladder;
- не запускает `clean` без подтверждённой причины.

### Frontend Developer
- текущий scope ограничен `ux-prototype/**`;
- агент остаётся доступным, но не должен придумывать package manager или команды production frontend, пока такой проект не появится.

### Critic
- read-only для проверяемого change set;
- проверяет код, тесты, docs, конфигурацию, диаграммы и infra;
- пишет `docs/REVIEW_REPORT.md` только для `/review --report`.

### QA
- проверяет наблюдаемое поведение, негативные и E2E-сценарии;
- не исправляет production-код.

### DevOps
- готовит CI/CD и operational artifacts;
- deploy выполняет только через `/deploy` после отдельного точного approval.

## File versioning

- Git handles versioning.
- Если файл существует → edit; если отсутствует → write.
- Никаких `_FINAL`, `_UPDATED`, `_v2`.
- One file = one source of truth.

## Project context

Перед изменением кода, документации или архитектуры:

1. Определи тип файла и модуль.
2. Найди релевантный skill.
3. Найди ближайший источник истины и связанные контракты.
4. Скомбинируй task context, skill и project context.
5. Только после этого выполняй изменение.
