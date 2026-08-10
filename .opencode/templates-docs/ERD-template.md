# TARGET DIRECTORY: docs/03-architecture/
# TARGET FILENAME: ERD.md
# DIAGRAM SOURCE: docs/03-architecture/erd/ERD.drawio
# DIAGRAM EXPORT: docs/03-architecture/erd/ERD.drawio.svg

# ERD — [Область системы]

## Статус

| Параметр | Значение |
|---|---|
| Статус | Черновик / Утверждён |
| Область | [Охватываемые сервисы и сущности] |
| DBMS | [СУБД] |
| Связанные BR/ADR/API | [Ссылки] |

## Каноническая диаграмма

`.drawio` является редактируемым источником истины. `.svg` — производный export,
который необходимо регенерировать после каждого изменения диаграммы.

- [Редактируемый источник](./erd/ERD.drawio)
- [SVG export](./erd/ERD.drawio.svg)

![ERD](./erd/ERD.drawio.svg)

## Ответственность сущностей

| Сущность | Назначение | Владелец данных | Граница сервиса |
|---|---|---|---|
| `[ENTITY]` | [Назначение] | [Владелец] | [Сервис] |

## Связи и кардинальности

| From | Relation | To | Cardinality | Правило удаления |
|---|---|---|---|---|
| `[ENTITY_A]` | [Связь] | `[ENTITY_B]` | `1:N` | [RESTRICT/CASCADE/SET NULL] |

## Ключевые ограничения и индексы

| Таблица | Ограничение / индекс | Причина |
|---|---|---|
| `[TABLE]` | `[UNIQUE/INDEX/CHECK]` | [Инвариант или query pattern] |

## Владение и контроль доступа

- [Правила tenant/owner isolation]
- [Проверки доступа на чтение и изменение]

## Sensitive Data Notes

| Данные | Классификация | Хранение / логирование |
|---|---|---|
| [Поле или группа данных] | [Public/Internal/Confidential/Restricted] | [Ограничения] |

## Evolution / Migration

- [Совместимость и порядок миграции]
- [Rollback и backfill]

## Открытые решения

| Решение | Влияние | Ответственный |
|---|---|---|
| [Вопрос] | [Impact] | [Owner] |
