import { test, expect } from '@playwright/test'

test('authenticates and opens the protected trainer cabinet', async ({ page }) => {
  await page.goto('/')

  await expect(page.locator('#username')).toBeVisible()
  await page.locator('#username').fill(process.env.E2E_AUTH_USERNAME ?? 'fitbridge-test')
  await page.locator('#password').fill(process.env.E2E_AUTH_PASSWORD ?? 'fitbridge')
  await page.locator('input[type="submit"]').click()

  await expect(page.getByText('FitBridge', { exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Выйти' })).toBeVisible()
})
