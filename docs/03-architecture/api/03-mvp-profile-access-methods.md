# Методы Trainer, ClientCard и Public Link API MVP

Канонические методы Gate 1 для тренера, клиентских карточек и lifecycle публичной ссылки. Все приватные методы используют стиль `domain.action` через `POST` + JSON.

## Общие правила

- Авторизация приватного API: см. [Security Architecture](../SECURITY_ARCHITECTURE.md). Domain API MVP — `TRAINER` only, без in-product `ADMIN`/support bypass.
- Клиент не регистрируется и не получает JWT; public-link методы работают только по token.
- Public token: raw token не хранить и не логировать, в БД только hash; обязательны TTL, revoke/close и rate limiting.
- `AccessGrant`, `Invite`, клиентское подтверждение доступа и отзыв доступа клиентом не входят в MVP публичного доступа к плану.

## Приватные бизнес-функции тренера

1. **`trainerProfile.createOrUpdate`** — создать или обновить минимальный профиль тренера.
   - *Бизнес-правило*: доступно только зарегистрированному тренеру с валидным JWT.
   - *Валидация*: `publicName` 2-80 символов; не хранить лишние персональные/медицинские данные клиента в профиле тренера.

2. **`trainerProfile.readOwn`** — получить собственный профиль тренера.
   - *Бизнес-правило*: возвращает только профиль caller'а.

3. **`clientCard.create`** — создать минимальную клиентскую карточку.
   - *Бизнес-правило*: карточка принадлежит тренеру и не создаёт клиентский аккаунт.
   - *Валидация*: `displayName` 1-120 символов; медданные, фото/видео, body metrics и расширенные profile fields запрещены.

4. **`clientCard.read`** — прочитать карточку клиента.
   - *Бизнес-правило*: доступна только тренеру-владельцу.

5. **`clientCard.update`** — изменить карточку клиента.
   - *Бизнес-правило*: изменения не должны попадать в публичный payload без whitelist.

6. **`clientCard.archive`** — архивировать карточку клиента.
   - *Бизнес-правило*: новые планы/ссылки для архивной карточки не создаются; активные public links должны быть закрыты или стать недоступными.

7. **`clientCard.list`** — список карточек тренера.
   - *Бизнес-правило*: список фильтруется по `trainerUserId`; показывает статус последнего/активного плана и completion status.

8. **`trainingPlan.generatePublicLink`** — сгенерировать публичную ссылку к плану.
   - *Бизнес-правило*: доступно только тренеру-владельцу плана.
   - *Security*: backend генерирует raw token, сохраняет только hash, выставляет TTL и `publicAccessStatus=ACTIVE`; raw token возвращается один раз.

9. **`trainingPlan.closePublicLink`** — закрыть публичный доступ по ссылке.
   - *Бизнес-правило*: после закрытия клиент больше не видит план и не может оставлять новые отметки по этой ссылке.
   - *Security*: `publicAccessStatus=REVOKED`, `publicAccessRevokedAt=now`; public endpoint проверяет статус на каждый запрос.

10. **`dashboard.getTrainerSummary`** — получить сводку кабинета тренера.
    - *Бизнес-правило*: показывает только карточки и планы caller'а: число активных карточек, активных ссылок, последних отметок выполнения.

## Публичные функции по ссылке

Публичные функции детализированы в [04-mvp-diary-plan-methods.md](./04-mvp-diary-plan-methods.md). Здесь фиксируется общий guardrail: public API принимает только token и не принимает `clientId`, `planId`, `trainerId` как основание доступа.

## Ссылки без дублирования

- Статусы и ограничения данных: [ERD](../ERD.md).
- Token lifecycle и threat model: [Security Architecture](../SECURITY_ARCHITECTURE.md).
- Rate limits и SLO: [06-metrics-and-limits.md](./06-metrics-and-limits.md).
