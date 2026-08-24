import { expect, test } from '@playwright/test'

/**
 * Smoke-тест: залогиненный пользователь попадает на /plans
 * и видит заголовок страницы.
 */
test('залогиненный пользователь видит список планов', async ({ page }) => {
  await page.goto('/plans')

  await expect(page.getByRole('heading', { name: 'Тренировочные планы' })).toBeVisible()
  // Кнопка создания плана доступна
  await expect(page.locator('#create-plan-btn')).toBeVisible()
  // Подзаголовок-описание
  await expect(page.getByText('Программы тренировок')).toBeVisible()
})

/**
 * Создание DRAFT-плана через UI.
 * После фикса вакансии v0.0.3 в БД есть enum 'DRAFT'.
 */
test('можно создать DRAFT-план через «Сохранить как черновик»', async ({ page }) => {
  const uniqueTitle = `E2E DRAFT ${Date.now()}`

  await page.goto('/plans')
  await page.locator('#create-plan-btn').click()

  // Заполняем форму
  await page.locator('#plan-name').fill(uniqueTitle)
  // Клиент — выбираем первого доступного «Александра Смирнова»
  await page.locator('#plan-client-select').selectOption({ index: 1 })
  // Упражнение
  await page.locator('input[placeholder^="Название упражнения"]').fill('Приседания E2E')
  await page.getByRole('button', { name: '+ Добавить' }).click()

  // Сохраняем как черновик
  await page.getByRole('button', { name: /Сохранить как черновик/i }).click()

  // Диалог должен закрыться
  await expect(page.getByRole('dialog', { name: 'Новый план' })).toBeHidden({
    timeout: 10_000,
  })

  // Фильтруем по DRAFT — в десктопной вьюхе это строка таблицы
  await page.locator('#plan-status-filter').selectOption('DRAFT')

  // Ждём появления строки таблицы с уникальным заголовком.
  // Используем именно <tr>, потому что в mobile-разметке <article> с md:hidden
  // при десктопном viewport (1280x720 по умолчанию) — она не видна.
  const row = page.locator('tr').filter({ hasText: uniqueTitle })
  await expect(row).toBeVisible()
  await expect(row.getByText('Черновик')).toBeVisible()
  await expect(row.getByRole('button', { name: /Активировать/ })).toBeVisible()
})

/**
 * Confirm-диалог: при попытке активации DRAFT-плана появляется
 * наш самописный диалог подтверждения (вместо нативного confirm).
 */
test('активация DRAFT открывает confirm-диалог', async ({ page }) => {
  // Создаём DRAFT в этом тесте, чтобы не зависеть от порядка
  const uniqueTitle = `E2E confirm ${Date.now()}`

  await page.goto('/plans')
  await page.locator('#create-plan-btn').click()
  await page.locator('#plan-name').fill(uniqueTitle)
  await page.locator('#plan-client-select').selectOption({ index: 1 })
  await page.locator('input[placeholder^="Название упражнения"]').fill('Подтягивания')
  await page.getByRole('button', { name: '+ Добавить' }).click()
  await page.getByRole('button', { name: /Сохранить как черновик/i }).click()
  await expect(page.getByRole('dialog', { name: 'Новый план' })).toBeHidden()

  await page.locator('#plan-status-filter').selectOption('DRAFT')
  const row = page.locator('tr').filter({ hasText: uniqueTitle })
  await expect(row).toBeVisible()

  // Кликаем «Активировать» в строке таблицы
  await row.getByRole('button', { name: /Активировать/ }).click()

  // Появился наш confirm-диалог (с заголовком «Активировать план?»)
  const confirm = page.getByRole('dialog')
  await expect(confirm.getByText('Активировать план?')).toBeVisible()
  await expect(
    confirm.getByText(/План .* станет доступен клиенту/),
  ).toBeVisible()

  // Закрываем через «Отмена», чтобы не зависеть от бэкенда
  await confirm.getByRole('button', { name: 'Отмена' }).click()
  await expect(confirm).toBeHidden()
})

/**
 * Toast-система: успешный логаут (или ошибка) показывает уведомление.
 * Логаут — самый стабильный сценарий для проверки.
 * Изолирован в отдельный файл (logout.spec.ts) с собственным контекстом.
 */

/**
 * Навигация между разделами работает без потери авторизации.
 * Используем прямой переход, а не клик по линкам — это устойчивее,
 * когда в DOM есть несколько «Клиенты» (sidebar + mobile nav).
 * Проверяем URL и присутствие ключевого текста без уровня heading
 * (Vue-router ленивая загрузка делает появление H1 нестабильным).
 */
test('переход между Планы ↔ Клиенты сохраняет сессию', async ({ page }) => {
  await page.goto('/plans')
  await expect(page.getByText('Тренировочные планы').first()).toBeVisible()
  await expect(page.locator('#create-plan-btn')).toBeVisible()

  await page.goto('/clients')
  await expect(page).toHaveURL(/\/clients/)
  // Список клиентов должен появиться (заголовок или хотя бы кнопка добавления)
  await expect(page.locator('#add-client-btn')).toBeVisible({ timeout: 10_000 })

  await page.goto('/plans')
  await expect(page).toHaveURL(/\/plans/)
  await expect(page.locator('#create-plan-btn')).toBeVisible()
})
