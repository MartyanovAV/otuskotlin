import { chromium, expect, test as setup } from '@playwright/test'
import { TEST_CONFIG } from '../playwright.config'

/**
 * Один раз проходит полный login-флоу через Keycloak
 * и сохраняет storageState для остальных тестов.
 *
 * Идемпотентен: можно перезапускать локально, не мешает dev-сессии.
 */
setup('логин через Keycloak и сохранение storageState', async ({ playwright }) => {
  const userDir = 'tests/fixtures/.auth'
  const storagePath = `${userDir}/user.json`

  // Создаём контекст с нуля — нам нужна «чистая» аутентификация
  const browser = await playwright.chromium.launch({ headless: true })
  const context = await browser.newContext({ baseURL: TEST_CONFIG.baseURL })
  const page = await context.newPage()

  // Открываем защищённый URL — Keycloak перехватит и покажет форму
  await page.goto('/plans')

  // Ждём форму логина Keycloak. Селекторы стабильны для стандартной темы.
  const usernameInput = page.locator('#username')
  const passwordInput = page.locator('#password')
  const submitButton = page.locator('input[name="login"], button[name="login"]').first()

  await expect(usernameInput, 'должна появиться форма логина Keycloak').toBeVisible({
    timeout: 15_000,
  })
  await usernameInput.fill(TEST_CONFIG.username)
  await passwordInput.fill(TEST_CONFIG.password)
  await submitButton.click()

  // После успешной аутентификации Keycloak редиректит обратно в SPA.
  // Сторонний Origin: ждём URL без "/realms/fit-bridge".
  await page.waitForURL(
    (url) => !url.toString().includes('/realms/fit-bridge'),
    { timeout: 15_000 },
  )

  // Дополнительно — ждём, что приложение реально авторизовано
  // (виден аватар пользователя в TopBar)
  await expect(
    page.getByText('Иван Тренеров').or(page.getByText(/Тренер/i).first()),
    'SPA не показала имя пользователя после логина',
  ).toBeVisible({ timeout: 10_000 })

  // Сохраняем state (cookies + localStorage — там keycloak-js хранит токены)
  await context.storageState({ path: storagePath })

  await context.close()
  await browser.close()
})
