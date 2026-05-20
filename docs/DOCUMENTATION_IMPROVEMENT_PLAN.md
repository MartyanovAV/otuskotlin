# Documentation Improvement Plan

План сформирован по итогам read-only анализа проекта FitBridge. Цель документа — зафиксировать, что нужно добавить, доработать и переделать в архитектурной, аналитической и бизнес-документации.

## Общий вывод

Документационная база уже сильная: есть Vision, Personas, CJM, MVP Scope, BR-001..BR-009, Product Roadmap, FR/NFR, API markdown, ADR по Keycloak/Ktor/Kotlin/POST Full API, deploy guide.

Главная проблема сейчас не в отсутствии документов вообще, а в несогласованности scope, метрик и разрыве между целевой архитектурой и фактическим состоянием кода.

## P0: Сквозные проблемы

| Что | Нужно сделать | Основания |
|---|---|---|
| Решение по Solo-client MVP | Зафиксировать во всех документах: Solo-client является полноценным MVP-путём наряду с Trainer-led | `MVP_SCOPE_SUMMARY.md`, `01-functional-requirements.md`, `03-architecture/02-api.md`, `03-architecture/03-architecture-overview.md` |
| Матрица трассировки требований создана | Поддерживать `docs/02-analysis/REQUIREMENTS_TRACEABILITY_MATRIX.md` при изменении BR, FR/NFR, API и тестов | Связи BR -> MVP -> FR/NFR -> API -> AC -> тесты зафиксированы, тесты пока плановые до появления backend-кода |
| C4-файлы созданы | Поддерживать `docs/03-architecture/c4/C4_CONTEXT.md`, `C4_CONTAINER.md`, `C4_COMPONENT.md` при изменении архитектуры | README и architecture overview указывают на Mermaid markdown как источник правды |
| ERD / модель данных создана | Поддерживать `docs/03-architecture/ERD.md` при изменении сущностей, ownership, ограничений и storage decisions | ERD фиксирует MVP-сущности, связи, ownership, soft delete и Phase 2 границы |
| Архитектура не совпадает с кодом | Зафиксировать transition-state или привести кодовую структуру к архитектуре | Ktor выбран в ADR, но backend сейчас `Main.kt` с `println("Hello")`; Ktor dependency отсутствует |

## Архитектурная документация

| Приоритет | Добавить / доработать / переделать | Что именно |
|---|---|---|
| P0 | Поддерживать C4-документы | `C4_CONTEXT.md`, `C4_CONTAINER.md`, `C4_COMPONENT.md` созданы в Mermaid markdown; SVG-экспорт можно добавить как производный артефакт |
| P0 | Поддерживать ERD | `docs/03-architecture/ERD.md` содержит `User`, `ClientProfile`, `TrainerProfile`, `AccessGrant`, `Invite`, `TrainingEntry`, `Program`, `ProgramAssignment`; связи, ownership, soft delete и Phase 2 границы |
| P0 | Переделать описание module architecture | `.opencode/manifest.md` требует `app-ktor` и `entities/*/{common,biz,api,repo-*}`, но `fit-bridge-be/settings.gradle.kts` подключает только `:fit-bridge-be-tmp` |
| P1 | Поддерживать ADR по хранилищу | `ADR-005-use-postgresql.md` выбирает PostgreSQL; при реализации нужно уточнить миграции, индексы и backup/PITR |
| P1 | Поддерживать Security Architecture / Threat Model | `docs/03-architecture/SECURITY_ARCHITECTURE.md` создан; поддерживать поток токенов, claims, роли, модель принятия решений о доступе, границы Keycloak vs backend, 152-ФЗ, controls для health-adjacent данных и модель угроз при изменении auth/access архитектуры |
| P1 | Добавить API contracts | Markdown API нужно дополнить OpenAPI/JSON Schema или хотя бы request/response/error model/idempotency/versioning |
| P1 | Доработать deployment architecture | `deploy/README.md` описывает Nginx static prototype, а архитектура говорит про backend API. Нужна deployment diagram и описание будущего Ktor runtime |
| P1 | Поддерживать observability ADR | `ADR-006-use-opensearch-fluent-bit-observability.md` выбирает OpenSearch, Dashboards и Fluent Bit; при реализации нужно уточнить retention, alerting, requestId middleware и production secrets |
| P2 | Поддерживать качество ADR | Шаблонные comparison-таблицы и обрыв фразы в `ADR-002-post-full-api.md` исправлены; новые ADR должны содержать предметное сравнение вариантов |
| P2 | Доработать CI/CD architecture | `.github/workflows/build.yml` запускает только `gradle buildEnvironment`, не сборку/тесты |

## Аналитическая документация

| Приоритет | Добавить / доработать / переделать | Что именно |
|---|---|---|
| P0 | Поддерживать traceability matrix | `docs/02-analysis/REQUIREMENTS_TRACEABILITY_MATRIX.md` содержит стабильные `RTM-*`, связь с BR, API methods, acceptance criteria и будущими тестами |
| P0 | Audit scope синхронизирован — поддерживать | MVP = infrastructure audit-oriented logging через ADR-006 и обязательный список событий; Phase 2 = продуктовый `AuditEvent` API/отдельная audit entity |
| P0 | Notification scope синхронизирован — поддерживать | MVP = pull-model UI статусы через существующие read endpoints/dashboard; Phase 2 = отдельный `Notification` API/provider, push/email/lifecycle communications |
| P0 | Матрица классификации данных создана — поддерживать | `docs/02-analysis/DATA_CLASSIFICATION_MATRIX.md` фиксирует классы данных MVP и Phase 2, необходимость согласия, правила доступа, правила логирования и заметки по хранению/удалению; поддерживать вместе с `BR-009`, NFR, ERD и ADR-006 |
| P1 | Метрики унифицированы | Канон MVP: activation тренеров ≥55%, invite acceptance ≥45%, первая отметка выполнения ≥50%, первая запись дневника ≥70%, willingness-to-pay ≥5 пилотных тренеров |
| P1 | Доработать acceptance criteria | Добавить негативные сценарии: expired invite, повторное принятие, revoke во время сессии, trainer without scope, удалённый профиль, conflict version программы |
| P1 | Добавить acceptance test matrix | E2E-сценарии: trainer-led, solo-client, revoke access, expired invite, no scope, profile deletion, program update conflict |
| P2 | Имя файла `01-functional-requirements.md` синхронизировано — поддерживать | Старое ошибочное имя файла не должно использоваться в ссылках |
| P2 | Убрать нетестируемые формулировки | "минимальный кабинет", "понятная ссылка", "future-ready", "без заметной задержки" заменить проверяемыми критериями |

## Бизнес-документация

| Приоритет | Добавить / доработать / переделать | Что именно |
|---|---|---|
| P0 | BR по privacy/consent/deletion создан | `docs/01-business/BR/BR-009-consent-privacy-deletion.md` добавлен и связан с MVP scope и RTM |
| P0 | Унифицировать "активного клиента" | `GLOSSARY.md`, `MONETIZATION_STRATEGY.md` и `BR-007` синхронизированы: простой вход/открытие приглашения без значимого действия не считается активностью для тарификации |
| P1 | Добавить `VALIDATION_PLAN.md` | Гипотеза -> эксперимент -> метрика -> порог успеха -> срок -> владелец. Сейчас гипотезы разбросаны по Vision, GTM, Monetization, Risks |
| P1 | `PRODUCT_ROADMAP.md` создан | MVP / Gate 1, Phase 2, Phase 3, критерии перехода и зависимости от метрик зафиксированы |
| P1 | CJM структура переделана — поддерживать | `CJM.md` приведён к структуре персона → таблица → соответствующая Mermaid journey → выводы; исправлены неверные диаграммы и Mermaid-блоки с двумя `title` |
| P1 | Free-tier/rich-media уточнены | В MVP нет upload/storage фото, видео и rich-media; допускается только design reserve/Phase 2 после отдельного решения по storage, privacy и unit-экономике |
| P1 | Phase 2 BR пересмотрены — поддерживать | `BR-003` и `BR-008` синхронизированы с `MVP_SCOPE_SUMMARY.md` и `PRODUCT_ROADMAP.md`: не входят в MVP, допустимы только design reserve / design-partner пилоты, ограничения помечены как целевые допущения Phase 2 |
| P2 | Унифицировать язык заголовков | Часть бизнес-документов смешивает RU/EN заголовки |

## Рекомендуемый порядок работ

1. Поддерживать закрытые P0: audit/notification scope, traceability matrix, C4, ERD, BR-009, data classification matrix и уже принятое решение о Solo-client как полном MVP-пути.
2. Поддерживать синхронизацию метрик, active client definition, health-data/consent модель, CJM и Phase 2 границы `AuditEvent`/`Notification`.
3. Поддерживать storage, security и observability документы; отдельно доработать API error/idempotency/versioning.
4. Поддерживать Phase 2 BR как design reserve / design-partner пилоты до отдельного решения о включении в roadmap; отдельно добавить validation plan и косметику документации.
