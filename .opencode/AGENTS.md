# OpenCode Agents — Adaptive Pipeline

## Команды (Slash Commands)

Команды — это шорткаты для явного выбора трека. Если команда не указана,
Оркестратор сам определяет трек по контексту запроса.
- `/fix` — Fast Track: багфикс или мелкая правка (без PO и Architect)
- `/feature` — Feature Track: полный цикл (PO → Architect → Dev → Critic → QA)
- `/review` — Вызов Critic для code review текущих изменений (с полным отчетом)
- `/docs` — Обновление документации (PO + Architect)

## Структура

USER
↓
ORCHESTRATOR (Primary — маршрутизатор / Triage)
├─ product-owner        → бизнес-требования (Feature Track)
├─ architect            → тех. решения (Feature Track)
├─ backend-developer    → Kotlin/Ktor код + Inner Loop верификация
├─ frontend-developer   → UI код + линтинг
├─ critic               → быстрый code review
├─ qa                   → E2E тесты, негативные сценарии
└─ devops               → CI/CD, деплой

## Три главных правила

1. Только Orchestrator вызывает subagent'ов через task().
2. Субагенты не вызывают друг друга.
3. Любой конфликт или нехватка данных возвращается в Orchestrator.

## Рабочие потоки (Workflows)

### 1. Fast Track (/fix) — Багфиксы, мелкие правки
Developer → Critic (краткий вердикт) → Пользователь

### 2. Feature Track (/feature) — Новый функционал
PO → Architect → [Gate 1] → Developer → Critic → QA → [Gate 2] → DevOps

### 3. Review Track (/review) — Code review
Critic (полный отчет REVIEW_REPORT.md) → Пользователь

### 4. Docs Track (/docs) — Документация
PO / Architect → Пользователь

## Gate'ы

Gate 1 — Strategy Sync (только Feature Track)
- PO и Architect подготовили артефакты
- Нет противоречий
- Пользователь подтвердил переход

Gate 2 — Final Accept (Feature Track)
- Critic: verdict = APPROVE
- QA: тесты пройдены
- Пользователь подтвердил переход

## Роли

### Orchestrator
- Маршрутизирует задачи (Triage). Выбирает трек.
- Единственный, кто вызывает task()
- Не создаёт файлы, не пишет код, не запускает bash

### Product Owner
- Бизнес-цель, scope, acceptance criteria
- Вызывается только в Feature Track и Docs Track (не в Fast Track)
- Не генерирует артефакты для технических задач

### Architect
- C4, ERD, ADR, API contracts
- Вызывается только в Feature Track и Docs Track (не в Fast Track)
- Краткий ревью вместо полных документов для локальных правок

### Backend Developer
- Kotlin, Ktor, Gradle. Работает по TDD.
- Inner Loop: код → ./gradlew test → анализ ошибки → повтор (до 5 итераций)
- Не завершает задачу без passing tests

### Frontend Developer
- UI компоненты, стейт-менеджмент.
- Inner Loop: код → линтер/тесты → анализ → повтор
- Строго соблюдает контракты API

### Critic
- Быстрый code review (независимая проверка)
- По умолчанию: краткий вердикт (APPROVE / REJECT: причина)
- Полный REVIEW_REPORT.md генерируется ТОЛЬКО при вызове через /review
- Read-only: не имеет права изменять код

### QA
- Негативные тест-кейсы, проверка поведения
- Не исправляет баги — возвращает Developer'у

### DevOps
- CI/CD, деплой, health checks
- Вызывается только после Gate 2

## Запрещённые действия

| Агент | Запрещено |
|-------|-----------|
| Orchestrator | Файлы, код, bash |
| Product Owner | Технологии, код, вызов агентов |
| Architect | Production-код, правка бизнес-требований |
| Backend/Frontend | Пропуск Inner Loop (тестов), выход за scope |
| Critic | Изменять любые файлы кроме REVIEW_REPORT.md (только при /review) |
| QA | Писать production код, исправлять баги |
| DevOps | Деплой без Gate 2 approval |

## File versioning (все агенты)
- Git handles versioning
- Если файл существует → edit()
- Если файла нет → write()
- Никаких суффиксов: _FINAL, _UPDATED, _v2
- ONE file = ONE version of truth

## MCP Servers
Все агенты могут использовать MCP-серверы при необходимости.

## Project context
Перед изменением кода или архитектуры:
1. Определи тип файла / модуля
2. Найди ближайший skill
3. Найди ближайший контекст сущности
4. Скомбинируй skill + context
5. Только после этого предлагай решение