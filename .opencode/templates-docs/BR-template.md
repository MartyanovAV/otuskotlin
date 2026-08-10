# TARGET DIRECTORY: docs/01-business/BR/
# TARGET FILENAME: BR-[NNN]-[feature-name].md (e.g., BR-001-user-registration.md)
# NEXT NUMBER: Extract numeric prefixes from glob("docs/01-business/BR/BR-[0-9][0-9][0-9]-*.md"), fail on duplicates, then use max + 1 (zero-padded). Do not reuse gaps.
# UNIQUENESS: Exactly one canonical file per BR-NNN. Deprecated aliases must not use a BR-NNN-* filename.

# BR-[BR-Number]: [Название фичи]

## Статус

| Параметр | Значение |
|----------|----------|
| **Статус** | ⬜ Черновик / ✅ Утверждён / ❌ Отклонён |
| **Дата утверждения** | YYYY-MM-DD |
| **Комментарий** | [пусто] |
| **Приоритет MVP** | [Опционально: P0 / P1 / Phase 2] |

## Business Value

[Одна строка: какую проблему решает, какую выгоду приносит]

## Приоритет реализации (Опционально)

- [Описание, что входит в MVP, а что в Phase 2]

## User Stories


### US-R[BR-Number]-[US-Number]: [Название] {Каждое требование может иметь несколько пользовательских историй}

* **As a** [Роль]
* **I want** [Действие]
* **So that** [Результат]

**Acceptance Criteria:**

* **AC-R[BR-Number]-U[US-Number]-[AC-Number]:** {Каждая пользовательская история может иметь несколько критериев приемки}
    * **Given:** [Контекст]
    * **When:** [Действие]
    * **Then:** [Результат]

## System Constraints

| Constraint   | Value     | Notes |
|--------------|-----------|-------|
| Latency      | < N ms    |       |
| Throughput   | N req/sec |       |
| Availability | N%        |       |

## Метрики успеха (Опционально)

- [Какие продуктовые метрики ожидаются при запуске]

## Out of Scope

- [Что НЕ входит в это требование]
