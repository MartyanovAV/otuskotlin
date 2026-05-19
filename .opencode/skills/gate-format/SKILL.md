---
name: gate-format
description: Format and present Gate review packages (Gate 1 Strategy Sync, Gate 2 Solution Proof, Gate 3 Final Accept) with standardized markdown templates for human approval checkpoints
---

# Gate Package Formats

Шаблоны для формирования сводок на каждый Gate.

## Gate 1: Strategy Sync

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

## Gate 2: Green Build Proof

```markdown
# Gate 2: Solution Review

## Green Build ✅
```
$ ./gradlew test
BUILD SUCCESSFUL
Tests: XX passed, 0 failed
Coverage: YY%
```

## Change Log
- [ ] Изменение 1
- [ ] Изменение 2

## Test Cases статус
- [✅] TC-1: Пройден
- [✅] TC-2: Пройден
- [⚠️] TC-3: Edge case обработан

## Self-Report
[Комментарий разработчика: почему выбрано такое решение]

---
**Принимаете решение?**
- [Approve] → К Quality Gate
- [Reject] → Что меняем?
  - [План] → Вернуться к architect
  - [Реализация] → Вернуться к executor
```

## Gate 3: Final Accept

```markdown
# Gate 3: Final Accept

## Quality Report
| Метрика | Значение |
|---------|---------|
| Покрытие | XX% |
| Тесты | YY passed |
| Warnings | ZZ |

## Проверки
- [✅] Тесты не "пустышки"
- [✅] Покрыты все Test Cases
- [✅] Код соответствует ADR
- [✅] Нет критичных запахов

## Рекомендации
- [Опционально: мелкие улучшения]

## Артефакты
- [BR-NNN](docs/01-business/BR/BR-NNN.md)
- [ADR-NNN](docs/03-architecture/ADR/ADR-NNN.md)
- [Код в feature/XXX](link)

---
**Принимаете и деплоим?**
- [Approve] → Deploy
- [Reject] → Укажите причину
```
