import { expect, test } from '@playwright/test';

test.describe('frontend i18n', () => {
  test('switches language on login and persists the selection', async ({ page }) => {
    await page.goto('/login');

    await page.getByRole('main').getByTestId('language-en').click();
    await expect(page.getByText('Return to your books, comics and manga collections.').first()).toBeVisible();

    await page.reload();
    await expect(page.getByText('Return to your books, comics and manga collections.').first()).toBeVisible();

    await page.getByRole('main').getByTestId('language-es').click();
    await expect(page.getByText(/Vuelve a tus colecciones de libros/i).first()).toBeVisible();
  });
});
