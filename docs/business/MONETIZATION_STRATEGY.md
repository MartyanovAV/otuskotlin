# FitBridge Monetization Strategy

## 1. База и логика документа
Документ развивает бизнес-гипотезы из `BUSINESS_VISION.md` и использует тот же шаблонный каркас: продуктовая основа, бизнес-цели, рынок и метрики монетизации.

## 2. Pricing Rationale
FitBridge должен стоить дешевле тяжёлых международных решений на старте входа, но не слишком дёшево, чтобы не обесценить ROI тренера.

### Сравнение ориентиров по рынку
| Продукт | Публичный ориентир цены | Комментарий |
|---|---:|---|
| TrueCoach | $26.34 / $57.99 / $136.99 в мес | понятная лестница по числу активных клиентов |
| PT Distinction | $19.90 / $59.90 / $89.90 в мес | сильный value stack и кастомизация |
| CoachAccountable | от $20 до $4000 в мес | price scales with clients |
| FitBridge | ₽1 490 / ₽2 990 / ₽5 990 | адаптация к русскоязычной покупательной способности |

### Почему такой price band
1. Для solo-тренера цена должна быть ниже воспринимаемой ежемесячной потери от 1 ушедшего клиента.
2. Для микро-студии цена должна быть ниже стоимости одного администраторского часа в день.
3. Для апгрейда нужен заметный value gap: лимиты, роли, аналитика, automation.

## 3. Revenue Streams
1. Подписки тренеров.
2. Командные тарифы для студий.
3. Add-ons: расширенная аналитика, брендирование, экспорт, интеграции, биллинг.
4. Партнёрские revenue-share программы с фитнес-школами и эквайрингом.
5. Позже — сервисные onboarding-пакеты для small teams.

## 4. LTV/CAC Model
### Базовые допущения
| Показатель | Start | Pro | Team |
|---|---:|---:|---:|
| ARPU/мес | ₽1 490 | ₽2 990 | ₽5 990 |
| Валовая маржа | 82% | 84% | 86% |
| Средний срок жизни | 14 мес | 18 мес | 24 мес |

### Примерный LTV
- Coach Start: ₽1 490 × 0.82 × 14 = **₽17 115**
- Coach Pro: ₽2 990 × 0.84 × 18 = **₽45 209**
- Studio Team: ₽5 990 × 0.86 × 24 = **₽123 634**

### CAC-гипотеза
| Канал | CAC цель |
|---|---:|
| PLG/referral | ₽1 500–2 500 |
| Контент/SEO | ₽3 000–5 000 |
| Партнёрства со школами | ₽4 000–6 000 |
| Assisted sales для студий | ₽10 000–18 000 |

Цель: LTV/CAC не ниже 3,0 на solo-сегменте и 5,0 на team-сегменте.

## 5. Free Tier Economics
### Роль free-слоя
- снижает CAC;
- формирует вирусный loop;
- повышает switching cost через историю клиента.

### Риски free-слоя
- storage cost фото и истории;
- саппорт пользователей без выручки;
- низкая конверсия без быстро видимой пользы.

### Ограничители экономики
- лимиты на heavy-media;
- базовая аналитика только в free;
- расширенные отчёты, automation и командная работа — платно;
- self-service support и knowledge base.

## 6. Churn Analysis and Retention Strategy
### Причины churn тренера
1. слабый time-to-value;
2. клиенты не подключились;
3. тренер не встроил сервис в weekly workflow;
4. не увидел влияния на retention клиентов.

### Контрмеры
- onboarding mission: 1 тренер → 3 клиента → 1 программа → 1 отчёт;
- e-mail/Telegram nudges по неактивным клиентам;
- quarterly ROI-summary;
- playbooks для сегментов solo / online / studio.

## 7. Expansion Revenue
1. Team seats и роли.
2. Advanced analytics.
3. Брендированные отчёты и white-label mobile experience.
4. Интеграции с оплатой и wearable-данными.
5. Дополнительные роли специалистов.

## 8. Основные метрики монетизации
- conversion free coach → paid;
- average clients per paid coach;
- expansion MRR;
- downgrade rate;
- logo churn;
- gross margin after support and infra.

## 9. Источники
1. TrueCoach Pricing — https://truecoach.co/pricing/
2. PT Distinction Pricing — https://www.ptdistinction.com/pricing
3. CoachAccountable Pricing — https://www.coachaccountable.com/pricing
4. BUSINESS_VISION.md в текущем репозитории.
