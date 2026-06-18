import { expect, test } from '@playwright/test';
import { e2eApiBaseUrl } from '../helpers/test-data';
import { expectAppLoaded, waitForNoAngularError } from '../helpers/navigation.e2e-helper';

test('loads the frontend shell and public navigation', async ({ page, request }) => {
  const health = await request.get(`${e2eApiBaseUrl()}/api/health`);
  expect(health.ok()).toBeTruthy();

  await page.goto('/');
  await expectAppLoaded(page);
  await waitForNoAngularError(page);
  await expect(page.getByTestId('login-link').or(page.getByTestId('session-label'))).toBeVisible();
});
