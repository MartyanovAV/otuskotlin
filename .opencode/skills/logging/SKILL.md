---
name: logging
description: Log a completed agent task to the session log using standard format with task number, agent name, timestamp, request summary, output summary, and status
---

# Logging Format

Стандартный формат логирования для всех агентов.

## Формат записи в лог сессии

```
### Task [N]: [agent_name] @[time] [фаза]
**Request:** [что просил пользователь]
**Output:** [краткое описание что сделал агент]
**Status:** [success/failed/blocked]
```

## Поля лога

| Поле | Описание |
|------|----------|
| Task N | Порядковый номер задачи |
| agent_name | Имя агента |
| time | Время выполнения |
| фаза | planning / design / implementation / review / release / validation |
| Request | Что было запрошено |
| Output | Что сделано |
| Status | Результат |

## Пример записи

```markdown
### Task 5: reviewer @14:30 review
**Request:** Напиши тесты на моках для модуля UserService
**Output:** Создал 12 тестов: 8 позитивных, 4 негативных. Все на MockK.
**Status:** success
```

## Рекомендации

- Записывай сразу после завершения task
- Будь краток в Output
- Указывай конкретные результаты (кол-во тестов, файлов и т.д.)
