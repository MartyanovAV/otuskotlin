import { expect, test } from '@playwright/test'

/**
 * Logout выделен в отдельный файл с собственным browser context,
 * потому что он инвалидирует shared storageState. Все остальные тесты
 * используют один и тот же залогиненный storageState.
 *
 * Примечание: Keycloak SSO может автоматически залогинить пользователя,
 * если в Keycloak уже есть активная сессия. Тест проверяет только
 * post-logout состояние (редирект + форма логина), это устойчиво к
 * SSO-вариациям.
 */
test.describe('logout (изолированный контекст)', () => {
  test('после «Выйти» редиректит на Keycloak и показывает форму логина', async ({
    browser,
  }) => {
    const context = await browser.newContext({ baseURL: 'http://localhost:8080' })
    const page = await context.newPage()

    // Открываем SPA и ждём стабилизации. Может быть два сценария:
    // a) keycloak ведёт на форму логина — `#username` появляется
    // b) SSO автоматически логинит — появляется `#create-plan-btn`
    await page.goto('/plans')
    await page.waitForLoadState('domcontentloaded')

    // Один из двух селекторов должен появиться
    const usernameVisible = await page
      .locator('#username')
      .waitFor({ state: 'visible', timeout: 20_000 })
      .then(() => true)
      .catch(() => false)

    if (usernameVisible) {
      // Сценарий a: логинимся
      await page.locator('#username').fill('fitbridge-test')
      await page.locator('#password').fill('fitbridge')
      await page.locator('input[name="login"], button[name="login"]').first().click()
      await page.waitForURL(
        (url) => !url.toString().includes('/realms/fit-bridge'),
        { timeout: 30_000 },
      )
    }
    // Сценарий b: уже залогинены через SSO, ничего не делаем

    // Должны быть в SPA
    await expect(page.locator('#create-plan-btn')).toBeVisible({ timeout: 15_000 })

    // === Главное: проверяем logout ===
    await page.getByRole('button', { name: 'Выйти' }).click()
    await page.waitForURL(/realms\/fit-bridge/, { timeout: 30_000 })
    // Форма логина Keycloak отобразилась
    await expect(page.locator('#username')).toBeVisible({ timeout: 30_000 })

    await context.close()
  })
})
