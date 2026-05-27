# Метрики и лимиты API - trainer-first MVP с публичной ссылкой

Продуктовые метрики, performance targets, rate limits и требования надёжности. Этот файл является каноническим источником API SLO для архитектурной документации MVP.

## Бизнес-метрики успеха

| Метрика | Целевое значение MVP | Комментарий |
|---------|----------------------|-------------|
| Активация тренера | ≥55% пилотных тренеров создают «клиентская карточка + план + публичная ссылка» в течение 7 дней | BR-010 / MVP_SCOPE_SUMMARY |
| Time-to-first-plan-link | Median ≤ 1 день после регистрации тренера | BR-010 |
| Отметка выполнения | ≥50% отправленных активных ссылок получают хотя бы одну отметку выполнения в течение 14 дней | BR-010 |
| Возврат тренера к статусам | ≥60% активированных тренеров возвращаются к просмотру статусов выполнения в течение 14 дней | BR-010 |
| Willingness-to-pay | ≥5 пилотных тренеров подтверждают готовность платить после сценария | PRODUCT_ROADMAP |
| Клиентский friction | ≤10% пилотных клиентов требуют регистрацию/кабинет как блокер первого использования | BR-010 |

## Производительность API

| Операция | Ожидание | Порог |
|----------|----------|-------|
| `clientCard.create` | P95 < 500 мс | Приватный trainer API |
| `trainingPlan.create` | P95 < 800 мс | Простой план без медиа |
| `trainingPlan.generatePublicLink` | P95 < 500 мс | Генерация token + hash storage |
| `publicPlan.openByToken` | P95 < 700 мс | Token hash lookup + минимальный payload |
| `publicPlan.markCompletion` | P95 < 700 мс | Token check + запись `CompletionMark` |
| `trainingPlan.closePublicLink` | P95 < 500 мс | Revoke должен вступать в силу на следующий public request |
| `dashboard.getTrainerSummary` | P95 < 1000 мс | Список карточек/статусы для тренера |
| Логирование audit-oriented события | Не блокирует бизнес-ответ API сверх целевого P95 | Structured logs через ADR-006 |

## Rate limits и ограничения

| Область | Лимит MVP | Примечание |
|---------|-----------|------------|
| Приватные чтения тренера | 600 запросов/минуту на trainer user | Общий API gateway limit |
| Приватные записи тренера | 120 запросов/минуту на trainer user | Кроме специальных лимитов ниже |
| Создание ClientCard | 100 карточек/сутки на тренера | Пилотный антиспам лимит |
| Создание TrainingPlan | 200 планов/сутки на тренера | Без шаблонов/импорта |
| Generate public link | 100 ссылок/сутки на тренера и 10/минуту burst | Защита от массовой генерации |
| Public open by token | Rate limit по IP/fingerprint/token hash | Конкретные значения уточнить до пилота |
| Public mark completion | Rate limit по IP/fingerprint/token hash; idempotency | Защита от дублей и abuse |

## Надёжность и соответствие

1. Доступность публичного API MVP: 99,5% в месяц.
2. Потеря подтверждённых `CompletionMark`: 0 допустимых случаев.
3. Время вступления закрытия ссылки в силу: на следующий public request, без кэша allow decision.
4. Raw token, request body public endpoints, содержимое плана и комментарии клиента не пишутся в логи.
5. Scope уведомлений MVP ограничен pull-model UI статусами; push/email provider и отдельный `Notification` API — Phase 2.
6. Scope аудита MVP ограничен infrastructure audit-oriented logging по ADR-006; продуктовый `AuditEvent` API и отдельная audit entity — Phase 2.
