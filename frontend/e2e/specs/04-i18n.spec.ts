import { expect, test } from '@playwright/test';

test.describe('frontend i18n', () => {
  test('switches language on login and persists the selection', async ({ page }) => {
    await page.goto('/login');

    await page.getByTestId('language-en').click();
    await expect(page.getByText('Access your CollectoHub space')).toBeVisible();

    await page.reload();
    await expect(page.getByText('Access your CollectoHub space')).toBeVisible();

    await page.getByTestId('language-es').click();
    await expect(page.getByText('Accede a tu espacio CollectoHub')).toBeVisible();
  });
});
