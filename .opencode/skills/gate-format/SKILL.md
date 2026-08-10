---
name: gate-format
description: Format Feature Full Gate 1 Strategy Sync, Feature Full Gate 2 Final Accept, and separate Deploy Track approval packages
---

# Gate Package Formats

Шаблоны для формирования сводок на каждый Gate.

## Gate 1: Strategy Sync (Feature Full only)

```markdown
# Gate 1: Strategy Sync

## Что делаем
[Краткое описание фичи одной строкой]

## Зачем
[Business Value одной строкой]

## Как поймём что готово (Test Cases)
- [ ] TC-1: [Название сценария]
- [ ] TC-2: [Название сценария]
- [ ] TC-N: [Edge case]

## Что затрагиваем
- Модуль A
- Модуль B

## Ожидаемые результаты
- [Что получит пользователь]

## Риски
- [Известные риски]

## Артефакты
- [BR-NNN](docs/01-business/BR/BR-NNN.md)
- [ADR-NNN](docs/03-architecture/ADR/ADR-NNN.md)

---
**Утверждаете план?**
- [Approve] → Перейти к разработке
- [Reject] → Укажите что не так
```

## Gate 2: Final Accept (Feature Full only)

````markdown
# Gate 2: Final Accept

## Verification evidence
```text
$ [фактически выполненная команда]
BUILD SUCCESSFUL
Tests: XX passed, 0 failed
```

## Critic
- Verdict: APPROVE
- Критичные замечания: отсутствуют

## QA
- Статус: PASS
- Негативные и граничные сценарии: [кратко]

## Change Log
- [x] Изменение 1
- [x] Изменение 2

## Test Cases статус
- [✅] TC-1: Пройден
- [✅] TC-2: Пройден
- [⚠️] TC-3: Edge case обработан

## Остаточные риски
- [Известные ограничения или «нет»]

---
**Принимаете результат Feature Full Track?**
- [Approve] → Завершить Feature Full Track
- [Reject] → Вернуть Developer/Architect с конкретной причиной

> Gate 2 не является разрешением на деплой. Деплой запускается отдельно через `/deploy`.
````

## Deploy Track: Execution Approval

```markdown
# Deploy Track: Execution Approval

## Target
- Environment: [точное окружение]
- Version/ref: [точный commit/tag/image digest]
- Rollout: [стратегия]

## Preflight
- [x] Артефакт существует и идентифицирован
- [x] Необходимые проверки пройдены
- [x] Конфигурация окружения проверена
- [x] Миграции и совместимость оценены

## Health checks
- [Проверка 1]
- [Проверка 2]

## Rollback plan
- Trigger: [условие отката]
- Action: [точное действие]
- Recovery verification: [проверка]

---
**Разрешаете deploy именно `[version/ref]` в `[environment]`?**
- [Approve] → DevOps EXECUTION
- [Reject] → Остановить Deploy Track
```
