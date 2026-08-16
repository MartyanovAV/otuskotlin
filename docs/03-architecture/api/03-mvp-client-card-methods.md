# Методы ClientCard API MVP Trainer Diary

Все методы используют POST Full API, требуют валидный trainer JWT и вычисляют owner из `sub`.

1. **`clientCard.create`** — создать минимальную карточку.
   - Backend игнорирует любые попытки передать owner и присваивает `ownerId = principal.userId`.
   - `displayName`: 1–120 символов; медданные, media и body metrics запрещены.

2. **`clientCard.read`** — прочитать карточку текущего owner.

3. **`clientCard.update`** — изменить карточку текущего owner с optimistic lock.

4. **`clientCard.archive`** — убрать карточку из активного списка; новые планы для неё запрещены.

5. **`clientCard.search`** — искать только карточки текущего owner по `searchString`, `status`, `pageSize`, `pageNumber`.

Identity profile не имеет FitBridge endpoints: username, имя и email UI получает через Keycloak ID Token/UserInfo.
