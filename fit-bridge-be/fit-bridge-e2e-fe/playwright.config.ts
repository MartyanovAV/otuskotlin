import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright config для FitBridge SPA.
 *
 * Запускать локально (при поднятом стеке deploy/docker-compose.local.yml):
 *   npx playwright test
 *   HEADED=1 npx playwright test --headed
 *   UI=1    npx playwright test --ui
 *
 * Переменные окружения:
 *   BASE_URL        — origin фронта (по умолчанию http://localhost:8080)
 *   KEYCLOAK_URL    — origin Keycloak (по умолчанию = BASE_URL)
 *   TEST_USERNAME   — логин (по умолчанию fitbridge-test)
 *   TEST_PASSWORD   — пароль (по умолчанию fitbridge)
 *   CI              — '1' включает retries и forbid-only
 */
const BASE_URL = process.env.BASE_URL ?? 'http://localhost:8080'
const KEYCLOAK_URL = process.env.KEYCLOAK_URL ?? BASE_URL
const HEADLESS = process.env.HEADED !== '1'
const CI = process.env.CI === '1'

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: CI,
  retries: CI ? 1 : 0,
  workers: CI ? 2 : undefined,
  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
  ],
  outputDir: 'test-results',

  use: {
    baseURL: BASE_URL,
    headless: HEADLESS,
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
    actionTimeout: 10_000,
    navigationTimeout: 30_000,
  },

  expect: {
    timeout: 5_000,
  },

  timeout: 60_000,

  // Auth setup: один раз логинимся, сохраняем storageState,
  // остальные тесты стартуют уже залогиненными.
  projects: [
    {
      name: 'setup',
      testMatch: /auth\.setup\.ts/,
    },
    {
      name: 'chromium',
      dependencies: ['setup'],
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'tests/fixtures/.auth/user.json',
      },
    },
  ],
})

export const TEST_CONFIG = {
  baseURL: BASE_URL,
  keycloakURL: KEYCLOAK_URL,
  username: process.env.TEST_USERNAME ?? 'fitbridge-test',
  password: process.env.TEST_PASSWORD ?? 'fitbridge',
}
