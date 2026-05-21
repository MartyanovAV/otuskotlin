# FitBridge Risk Register

## 1. Реестр рисков
| ID | Риск | Вероятность (1-5) | Влияние (1-5) | Score | Owner | Mitigation | Timeline | Early Warning Indicator | Contingency |
|---|---|---:|---:|---:|---|---|---|---|---|
| R-01 | Низкая конверсия тренеров в оплату | 3 | 5 | 15 | PO/Growth | пересборка onboarding и pricing prompts | 0–3 мес | activation<40% | ручной success-onboarding |
| R-02 | Высокая стоимость free-слоя | 3 | 4 | 12 | Product | лимиты на heavy-usage | 3–6 мес | infra cost/user растёт > план | урезание free feature-set |
| R-03 | Низкий retention клиентов без тренера | 3 | 3 | 9 | Product | reminders, streaks, progress loops | 3–6 мес | D30 retention < 20% | перенос фокуса на trainer-led activation |
| R-04 | Недоверие к персональным данным | 4 | 5 | 20 | CEO/Legal | privacy-by-design, consent flows, audit logs | 0–3 мес | вопросы в саппорт по privacy > норма | публичный trust-center и юр. аудит |
| R-05 | Нарушение требований 152-ФЗ | 2 | 5 | 10 | Legal, Architect | legal review, локализация данных, DPIA-подход, шифрование БД, аудит-логи доступа | 0–3 мес и постоянно | замечания юриста/партнёров | временное ограничение чувствительных данных |
| R-06 | Атака конкурентов в нишу solo-тренеров | 3 | 4 | 12 | CEO/Growth | усилить client-owned data positioning | 6–12 мес | рост copycat messaging | ускорить партнёрства и бренд |
| R-07 | Перегрузка roadmap | 4 | 3 | 12 | Product | stage-gate на фичи | постоянно | рост WIP и delay | freeze неприоритетных фич |
| R-08 | Слабый adoption ролей в small teams | 2 | 4 | 8 | Sales/Product | пилоты на 2–3 студиях | 6–9 мес | team feature usage < 30% | перенести expansion на позже |
| R-09 | Сложная миграция из текущих инструментов | 4 | 4 | 16 | Product/CS | импорт CSV, шаблоны, migration help | 0–6 мес | drop-off на шаге импорта | concierge migration |
| R-10 | Ошибочное позиционирование как medical product | 2 | 4 | 8 | Legal/Marketing | жёсткие product claims guidelines | 0–3 мес | вопросы партнёров о диагнозах | пересмотр messaging |
| R-11 | Концентрация лидов в 1–2 партнёрах/школах | 3 | 4 | 12 | Growth | cap на долю одного партнёра, параллельный pipeline | 3–9 мес | >35% новых лидов из одного партнёра | перераспределить бюджет в owned channels |
| R-12 | Referral abuse и фиктивные приглашения | 3 | 3 | 9 | Product/Growth | reward только после оплаты или 30 дней активности, anti-fraud review | 0–6 мес | anomalous invite-to-pay ratio | заморозка наград и ручная проверка |
| R-13 | Сбои платежей и involuntary churn | 3 | 5 | 15 | Product/Finance | retries, dunning, уведомления об истечении карты/метода оплаты | 0–3 мес | failed payments > 4% | grace period и ручное восстановление |
| R-14 | Слабая конверсия client-to-trainer invite | 3 | 4 | 12 | Growth/Product | не закладывать канал как основной, тестировать value prop отдельно | 3–9 мес | client invite acceptance < 20% | усилить trainer-led и partnership channels |

## 2. Критические риски
Критическими считаются риски со score 15+:
- R-01,
- R-04,
- R-09,
- R-13.

## 3. Процесс контроля
1. Еженедельный просмотр leading indicators.
2. Ежемесячный risk review на уровне продукта и роста.
3. Ежеквартальный legal/privacy review.

## 4. Источники
1. КонсультантПлюс, 152-ФЗ — https://www.consultant.ru/document/cons_doc_LAW_61801/
2. BUSINESS_VISION.md.
