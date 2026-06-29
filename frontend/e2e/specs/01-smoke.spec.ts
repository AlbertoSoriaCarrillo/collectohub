import { expect, test } from '@playwright/test';
import { e2eApiBaseUrl } from '../helpers/test-data';
import { expectAppLoaded, waitForNoAngularError } from '../helpers/navigation.e2e-helper';

test('loads the frontend shell and public navigation', async ({ page, request }) => {
  const health = await request.get(`${e2eApiBaseUrl()}/api/health`);
  expect(health.ok()).toBeTruthy();

  await page.goto('/');
  await expectAppLoaded(page);
  await waitForNoAngularError(page);
  await expect(page).toHaveURL(/\/home$/);
  await expect(page.getByTestId('home-page')).toBeVisible();
  await expect(page.getByTestId('login-header-link')).toBeVisible();
  await expect(page.getByTestId('register-link')).toHaveCount(0);
  await expect(page.getByTestId('language-selector')).toBeVisible();
  await expect(page.getByRole('link', { name: /catalogo|catalog/i }).first()).toBeVisible();
});
