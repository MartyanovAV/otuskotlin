// Full-stack e2e навигации: проверяет, что после логина через Keycloak
// клики по пунктам Sidebar не вызывают full-page reload (покрывает регрессию
// с `navigate()` без MouseEvent).
//
// Запускается ТОЛЬКО при поднятом полном backend-стеке:
//   ./gradlew --no-daemon buildInfra buildImages e2eTests --rerun-tasks
// (CI-команда, поднимает Keycloak + Postgres + backend + dev frontend).
// Локально: `npm run dev` (на :5173) + docker-compose с Keycloak.
//
// Креды — из ENV, по умолчанию dev-фикстура из Caddyfile.local.

import { test, expect, type Frame, type Page } from '@playwright/test'

const trackMainFrameNavigations = (page: Page) => {
  let navigations = 0
  page.on('framenavigated', (frame: Frame) => {
    if (frame === page.mainFrame() && frame.url() !== 'about:blank') {
      navigations += 1
    }
  })
  return () => navigations
}

const login = async (page: Page) => {
  await page.goto('/')
  await expect(page.locator('#username')).toBeVisible({ timeout: 15000 })
  await page.locator('#username').fill(process.env.E2E_AUTH_USERNAME ?? 'fitbridge-test')
  await page.locator('#password').fill(process.env.E2E_AUTH_PASSWORD ?? 'fitbridge')
  await page.locator('input[type="submit"]').click()
  await expect(page.getByText('FitBridge', { exact: true })).toBeVisible({ timeout: 15000 })
  await expect(page.getByRole('button', { name: 'Выйти' })).toBeVisible()
}

test.describe('FitBridge nav (full stack)', () => {
  test('sidebar навигация не делает full-page reload', async ({ page }) => {
    const getNavs = trackMainFrameNavigations(page)
    await login(page)
    const baseline = getNavs()

    // Sidebar: «Клиенты» и «Планы» — отдельные <a href> с router-link.
    const clientsLink = page.locator('aside a[href="/clients"]')
    const plansLink = page.locator('aside a[href="/plans"]')
    await expect(clientsLink).toBeVisible()
    await expect(plansLink).toBeVisible()

    // Сначала /clients.
    await clientsLink.click()
    await expect(page).toHaveURL(/\/clients$/)
    // Активное состояние: aria-current="page" и визуальный класс bg-primary.
    await expect(clientsLink).toHaveAttribute('aria-current', 'page')

    // Потом /plans.
    await plansLink.click()
    await expect(page).toHaveURL(/\/plans$/)
    await expect(plansLink).toHaveAttribute('aria-current', 'page')
    await expect(clientsLink).not.toHaveAttribute('aria-current', 'page')

    // Обратно на дашборд — точный матч.
    const dashboardLink = page.locator('aside a[href="/"]')
    await dashboardLink.click()
    await expect(page).toHaveURL(/\/$/)
    await expect(dashboardLink).toHaveAttribute('aria-current', 'page')

    // Полных перезагрузок не было: фикс `navigate()` с event на месте,
    // router.push обновляет URL через history API без navigation.
    expect(getNavs()).toBe(baseline)
  })

  test('TanStack Query state не теряется при навигации (косвенный признак SPA)', async ({ page }) => {
    await login(page)

    // Переходим в /clients и дожидаемся, что загрузка закончилась
    // (появилась хотя бы одна запись или empty-state).
    await page.locator('aside a[href="/clients"]').click()
    await expect(page).toHaveURL(/\/clients$/)

    // Дожидаемся, что ушла «Загрузка клиентов...».
    await expect(page.getByText('Загрузка клиентов...')).toBeHidden({ timeout: 15000 })
    // И что-то из реального контента отрендерилось — таблица или empty-state.
    await expect(page.getByRole('button', { name: 'Добавить клиента' }).first()).toBeVisible()

    // Запоминаем, какая сейчас первая строка (если есть) или empty-state текст.
    const firstSnapshot = await page.evaluate(() => {
      const row = document.querySelector('[id^="client-row-"]')
      return row?.getAttribute('id') ?? 'empty'
    })

    // Уходим в /plans и возвращаемся в /clients. Если был full reload —
    // TanStack Query-стейт (и lock-значения) затрутся, запрос пойдёт заново,
    // и user увидит «Загрузка клиентов...» на 100-500 мс.
    await page.locator('aside a[href="/plans"]').click()
    await expect(page).toHaveURL(/\/plans$/)
    await page.locator('aside a[href="/clients"]').click()
    await expect(page).toHaveURL(/\/clients$/)

    // Кэш TanStack Query должен быть ещё «тёплый» — сразу видим первую
    // строку или empty-state, без спиннера.
    await expect(page.getByText('Загрузка клиентов...')).toBeHidden()

    const secondSnapshot = await page.evaluate(() => {
      const row = document.querySelector('[id^="client-row-"]')
      return row?.getAttribute('id') ?? 'empty'
    })

    // Навигация не должна была «встряхнуть» список. Один и тот же
    // первый id (или оба empty) — кэш выжил.
    expect(secondSnapshot).toBe(firstSnapshot)
  })
})
