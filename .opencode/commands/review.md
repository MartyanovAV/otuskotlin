---
description: Полный review кода и документации; файл отчёта создаётся только с --report
agent: orchestrator
---
Вызови Critic в режиме Review Track (РЕЖИМ 2) и передай точный scope.

Поддерживаемые аргументы:
- без scope → `worktree`;
- `worktree`, `staged`, `branch <ref>` или `commit <sha>`;
- опциональный `--report` → передай `WRITE_REPORT: true`.

Без `--report` Critic возвращает полный анализ в ответе и НЕ создаёт/не изменяет `docs/REVIEW_REPORT.md`.
Review Track остаётся read-only для проверяемых изменений и не запускает автоматические исправления.
Контекст: $ARGUMENTS
