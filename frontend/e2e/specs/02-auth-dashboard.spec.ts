import { expect, test } from '@playwright/test';
import { loginUser, logoutIfVisible, registerUser } from '../helpers/auth.e2e-helper';
import { createScenarioData } from '../helpers/test-data';

test('registers a user, reaches dashboard, logs out and logs in again', async ({ page }) => {
  const data = createScenarioData();

  await registerUser(page, data.user);
  await expect(page.getByText(data.user.email)).toBeVisible();

  await logoutIfVisible(page);
  await loginUser(page, data.user);

  await page.goto('/shops');
  await expect(page.getByRole('heading', { name: 'Mis tiendas' })).toBeVisible();
});
