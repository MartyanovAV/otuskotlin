# Documentation Improvement Plan

План фиксирует, что нужно добавить, доработать и переделать в архитектурной, аналитической и бизнес-документации FitBridge.

## Общий вывод

Документационная база уже сильная: есть Vision, Personas, CJM, MVP Scope, BR-001..BR-009, Product Roadmap, FR/NFR, API markdown и ADR по ключевым архитектурным решениям.

Главная задача — поддерживать согласованность scope, метрик, аналитических требований и целевой архитектуры.

## Закрытые / завершённые задачи

Раздел ниже фиксирует историю уже выполненных улучшений и не является активным backlog.

| Что | Статус | Основания |
|---|---|---|
| Решение о Solo-client как полном MVP-пути | ✅ Завершено | Зафиксировано во всех документах: `MVP_SCOPE_SUMMARY.md`, `01-functional-requirements.md`, `03-architecture/02-api.md`, `03-architecture/03-architecture-overview.md` |
| Матрица трассировки требований | ✅ Завершено | `docs/02-analysis/REQUIREMENTS_TRACEABILITY_MATRIX.md` создана и поддерживается |
| C4-файлы созданы | ✅ Завершено | `docs/03-architecture/c4/C4_CONTEXT.md`, `C4_CONTAINER.md`, `C4_COMPONENT.md` созданы в Mermaid markdown |
| ERD / модель данных создана | ✅ Завершено | `docs/03-architecture/ERD.md` содержит все MVP-сущности, связи, ownership, soft delete и Phase 2 границы |
| BR по privacy/consent/deletion | ✅ Завершено | `docs/01-business/BR/BR-009-consent-privacy-deletion.md` добавлен и связан с MVP scope и RTM |
| Унификация «активного клиента» | ✅ Завершено | `GLOSSARY.md`, `MONETIZATION_STRATEGY.md` и `BR-007` синхронизированы |
| Audit scope синхронизирован | ✅ Завершено | MVP = infrastructure audit-oriented logging через ADR-006; Phase 2 = продуктовый `AuditEvent` API |
| Notification scope синхронизирован | ✅ Завершено | MVP = pull-model UI статусы; Phase 2 = отдельный `Notification` API/provider |
| Матрица классификации данных | ✅ Завершено | `docs/02-analysis/DATA_CLASSIFICATION_MATRIX.md` создана и связана с `BR-009`, NFR, ERD и ADR-006 |
| PRODUCT_ROADMAP создан | ✅ Завершено | MVP / Gate 1, Phase 2, Phase 3, критерии перехода и зависимости от метрик зафиксированы |
| CJM структура переделана | ✅ Завершено | `CJM.md` приведён к корректной структуре; исправлены неверные диаграммы и Mermaid-блоки |
| Phase 2 BR пересмотрены | ✅ Завершено | `BR-003` и `BR-008` синхронизированы с `MVP_SCOPE_SUMMARY.md` и `PRODUCT_ROADMAP.md` |

## Актуальные действия

| Что | Нужно сделать | Основания |
|---|---|---|
| Синхронизировать архитектурные решения и модульную модель | Поддерживать единое описание целевой структуры сервиса и границ сущностей | Требуется единый источник правил по структуре модулей |
| Уточнить границы MVP и Phase 2 в связанных документах | Поддерживать единые формулировки по API, данным, ролям и метрикам | Требуется регулярная проверка связей между BR, FR/NFR, RTM и roadmap |

## Поддерживать / monitoring

Эти пункты не считаются новыми задачами, но требуют проверки при изменении scope, BR, FR/NFR, API, roadmap или архитектурных решений.

## Архитектурная документация

| Приоритет | Добавить / доработать / переделать | Что именно |
|---|---|---|
| P0 | ~~Поддерживать C4-документы~~ ✅ | `C4_CONTEXT.md`, `C4_CONTAINER.md`, `C4_COMPONENT.md` созданы в Mermaid markdown; SVG-экспорт можно добавить как производный артефакт |
| P0 | ~~Поддерживать ERD~~ ✅ | `docs/03-architecture/ERD.md` содержит все MVP-сущности, связи, ownership, soft delete и Phase 2 границы |
| P0 | Поддерживать описание module architecture | Целевая модульная модель должна быть описана единообразно и связана с C4/ERD/API-документами |
| P1 | Поддерживать ADR по хранилищу | `ADR-005-use-postgresql.md` выбирает PostgreSQL; нужно уточнить миграции, индексы и backup/PITR |
| P1 | Поддерживать Security Architecture / Threat Model | `docs/03-architecture/SECURITY_ARCHITECTURE.md` создан; поддерживать поток токенов, claims, роли, модель принятия решений о доступе, границы Keycloak vs backend, 152-ФЗ, controls для health-adjacent данных и модель угроз при изменении auth/access архитектуры |
| P1 | Добавить API contracts | Markdown API нужно дополнить OpenAPI/JSON Schema или хотя бы request/response/error model/idempotency/versioning |
| P1 | Доработать runtime architecture | Нужна deployment diagram и описание целевого runtime-контура |
| P1 | Поддерживать observability ADR | `ADR-006-use-opensearch-fluent-bit-observability.md` выбирает OpenSearch, Dashboards и Fluent Bit; нужно уточнить retention, alerting, requestId middleware и правила управления секретами |
| P2 | Поддерживать качество ADR | Шаблонные comparison-таблицы и обрыв фразы в `ADR-002-post-full-api.md` исправлены; новые ADR должны содержать предметное сравнение вариантов |
| P2 | Доработать CI/CD architecture | Описать целевые quality gates, сборку, тесты и публикацию артефактов |

## Аналитическая документация

| Приоритет | Добавить / доработать / переделать | Что именно |
|---|---|---|
| P0 | ~~Поддерживать traceability matrix~~ ✅ | `docs/02-analysis/REQUIREMENTS_TRACEABILITY_MATRIX.md` содержит стабильные `RTM-*`, связь с BR, API methods, acceptance criteria и будущими тестами |
| P0 | ~~Audit scope синхронизирован~~ ✅ | MVP = infrastructure audit-oriented logging через ADR-006 и обязательный список событий; Phase 2 = продуктовый `AuditEvent` API/отдельная audit entity |
| P0 | ~~Notification scope синхронизирован~~ ✅ | MVP = pull-model UI статусы через существующие read endpoints/dashboard; Phase 2 = отдельный `Notification` API/provider, push/email/lifecycle communications |
| P0 | ~~Матрица классификации данных создана~~ ✅ | `docs/02-analysis/DATA_CLASSIFICATION_MATRIX.md` фиксирует классы данных MVP и Phase 2, необходимость согласия, правила доступа, правила логирования и заметки по хранению/удалению; поддерживать вместе с `BR-009`, NFR, ERD и ADR-006 |
| P1 | Метрики унифицированы | Канон MVP: activation тренеров ≥55%, invite acceptance ≥45%, первая отметка выполнения ≥50%, первая запись дневника ≥70%, willingness-to-pay ≥5 пилотных тренеров |
| P1 | Доработать acceptance criteria | Добавить негативные сценарии: expired invite, повторное принятие, revoke во время сессии, trainer without scope, удалённый профиль, conflict version программы |
| P1 | Добавить acceptance test matrix | E2E-сценарии: trainer-led, solo-client, revoke access, expired invite, no scope, profile deletion, program update conflict |
| P2 | Имя файла `01-functional-requirements.md` синхронизировано — поддерживать | Старое ошибочное имя файла не должно использоваться в ссылках |
| P2 | Убрать нетестируемые формулировки | "минимальный кабинет", "понятная ссылка", "future-ready", "без заметной задержки" заменить проверяемыми критериями |

## Бизнес-документация

| Приоритет | Добавить / доработать / переделать | Что именно |
|---|---|---|
| P0 | ~~BR по privacy/consent/deletion создан~~ ✅ | `docs/01-business/BR/BR-009-consent-privacy-deletion.md` добавлен и связан с MVP scope и RTM |
| P0 | ~~Унифицировать "активного клиента"~~ ✅ | `GLOSSARY.md`, `MONETIZATION_STRATEGY.md` и `BR-007` синхронизированы: простой вход/открытие приглашения без значимого действия не считается активностью для тарификации |
| P1 | Добавить `VALIDATION_PLAN.md` | Гипотеза -> эксперимент -> метрика -> порог успеха -> срок -> владелец. Гипотезы должны быть сведены из Vision, GTM, Monetization и Risks |
| P1 | `PRODUCT_ROADMAP.md` создан | MVP / Gate 1, Phase 2, Phase 3, критерии перехода и зависимости от метрик зафиксированы |
| P1 | CJM структура переделана — поддерживать | `CJM.md` приведён к структуре персона → таблица → соответствующая Mermaid journey → выводы; исправлены неверные диаграммы и Mermaid-блоки с двумя `title` |
| P1 | Free-tier/rich-media уточнены | В MVP нет upload/storage фото, видео и rich-media; допускается только design reserve/Phase 2 после отдельного решения по storage, privacy и unit-экономике |
| P1 | Phase 2 BR пересмотрены — поддерживать | `BR-003` и `BR-008` синхронизированы с `MVP_SCOPE_SUMMARY.md` и `PRODUCT_ROADMAP.md`: не входят в MVP, допустимы только design reserve / design-partner пилоты, ограничения помечены как целевые допущения Phase 2 |
| P2 | Унифицировать язык заголовков | Часть бизнес-документов смешивает RU/EN заголовки |

## Рекомендуемый порядок работ

1. Не возвращать завершённые P0-пункты в активный backlog без нового решения по scope.
2. Актуальные действия: синхронизировать архитектурные решения, модульную модель и границы MVP/Phase 2.
3. Monitoring: поддерживать синхронизацию метрик, health-data/consent модель и Phase 2 границы `AuditEvent`/`Notification`.
4. Monitoring: поддерживать storage, security и observability документы; отдельно доработать API error/idempotency/versioning.
5. Плановые улучшения: добавить validation plan и косметику документации (имена файлов, проверяемые формулировки).
