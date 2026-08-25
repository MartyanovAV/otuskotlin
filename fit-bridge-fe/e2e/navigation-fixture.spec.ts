// Изолированный e2e-тест навигации в кабинете тренера.
//
// Запускается против локально поднятого Vite dev-сервера (npm run dev),
// но НЕ требует Keycloak, backend, БД и Toast-компонента —
// всё это вырезано в e2e/fixtures/nav-only.html и main.ts.
//
// Тест проверяет три сценария, которые покрывают регрессию «редиректа»:
//
//  1. Клик по любому пункту Sidebar обновляет currentRoute без полной
//     перезагрузки страницы (никаких framenavigated у mainFrame, кроме
//     самого первого goto).
//  2. Активный пункт получает визуальный сигнал (!bg-primary) и
//     aria-current="page"; неактивный — НЕ имеет aria-current="page".
//  3. Маршрут реально меняется внутри router.currentRoute.

import { test, expect, type Frame, type Page } from '@playwright/test'

const FIXTURE_URL = '/e2e/fixtures/nav-only.html'

/**
 * Считает, сколько раз main frame перешёл на новый URL. Первый переход —
 * это сам page.goto() (он нам не интересен), все последующие — потенциальные
 * full-page reload. В нашей фикстуре router использует createMemoryHistory,
 * поэтому router.push обновляет только currentRoute, но не window.location.
 * Реальный браузерный URL не меняется, что для теста и хорошо: если бы
 * фикс не сработал, мы увидели бы здесь изменение pathname/hash.
 */
const trackMainFrameNavigations = (page: Page) => {
  let navigations = 0
  page.on('framenavigated', (frame: Frame) => {
    if (frame === page.mainFrame() && frame.url() !== 'about:blank') {
      navigations += 1
    }
  })
  return () => navigations
}

// Sidebar вёрстка обёрнута в <aside>, MobileNav — в <nav> с классом fixed bottom-0.
// Используем эти структурные различия, чтобы Playwright не путал два nav
// (оба имеют aria-label="Основная навигация" по дизайну).
const sidebarLink = (page: Page, href: string) =>
  page.locator(`aside nav[aria-label="Основная навигация"] a[href="${href}"]`)
const mobileNavLink = (page: Page, href: string) =>
  page.locator(`nav.fixed.bottom-0[aria-label="Основная навигация"] a[href="${href}"]`)

const currentRoutePath = (page: Page) =>
  page.evaluate(() => {
    const f = (window as unknown as {
      __fixture?: { router: { currentRoute: { value: { path: string } } } }
    }).__fixture
    return f?.router.currentRoute.value.path
  })

test.describe('FitBridge nav (fixture)', () => {
  test('sidebar кликает по /clients без full reload и подсвечивает активный пункт', async ({ page }) => {
    const getNavs = trackMainFrameNavigations(page)
    await page.goto(FIXTURE_URL)

    // Дожидаемся монтирования Sidebar — у него есть «FitBridge» brand-метка.
    await expect(page.getByText('FitBridge', { exact: true })).toBeVisible()
    const navsAfterLoad = getNavs()

    const clientsLink = sidebarLink(page, '/clients')
    const dashboardLink = sidebarLink(page, '/')
    await expect(clientsLink).toBeVisible()

    // Активным изначально является /dashboard (точное совпадение пути).
    await expect(dashboardLink).toHaveAttribute('aria-current', 'page')
    await expect(dashboardLink).toHaveClass(/!bg-primary/)

    // Кликаем «Клиенты».
    await clientsLink.click()
    expect(await currentRoutePath(page)).toBe('/clients')

    // Активный пункт переехал на /clients; dashboard больше не активный.
    await expect(clientsLink).toHaveAttribute('aria-current', 'page')
    await expect(clientsLink).toHaveClass(/!bg-primary/)
    // У RouterLink на неактивном маршруте атрибут aria-current снимается → null.
    await expect(dashboardLink).not.toHaveAttribute('aria-current', 'page')

    // Никаких новых навигаций у main frame — это и есть отсутствие full reload.
    // (Если бы фикс не сработал, ссылка триггерила бы default-action,
    // и pathname сменился бы — это дало бы +1 к счётчику.)
    expect(getNavs()).toBe(navsAfterLoad)
  })

  test('все три пункта навигации работают последовательно', async ({ page }) => {
    const getNavs = trackMainFrameNavigations(page)
    await page.goto(FIXTURE_URL)
    await expect(page.getByText('FitBridge', { exact: true })).toBeVisible()
    const baseline = getNavs()

    const items = [
      { href: '/', name: 'Дашборд' },
      { href: '/plans', name: 'Планы' },
      { href: '/clients', name: 'Клиенты' },
      { href: '/', name: 'Дашборд' },
    ]

    for (const item of items) {
      const link = sidebarLink(page, item.href)
      await link.click()
      expect(await currentRoutePath(page), `path after clicking ${item.name}`).toBe(item.href)
      await expect(link, `aria-current on ${item.name}`).toHaveAttribute('aria-current', 'page')
    }

    // Полных перезагрузок не было — все три клика прошли как SPA-навигация.
    expect(getNavs()).toBe(baseline)
  })

  test('мобильная навигация (MobileNav) тоже не делает full reload', async ({ page }) => {
    const getNavs = trackMainFrameNavigations(page)
    await page.goto(FIXTURE_URL)
    await expect(page.getByText('FitBridge', { exact: true })).toBeVisible()
    const baseline = getNavs()

    const mobileClients = mobileNavLink(page, '/clients')
    await expect(mobileClients).toBeVisible()
    await mobileClients.click()

    expect(await currentRoutePath(page)).toBe('/clients')
    expect(getNavs()).toBe(baseline)
  })
})
