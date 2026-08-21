import { test, expect } from '@playwright/test'

// FitBridge frontend E2E navigation check
test('visits the app root url and loads page', async ({ page }) => {
  await page.goto('/')
  // Keycloak or app root is loaded
  await expect(page).toHaveTitle(/FitBridge|Sign in/i)
})
