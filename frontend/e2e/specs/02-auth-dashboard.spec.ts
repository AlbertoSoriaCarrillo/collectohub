import { expect, test } from '@playwright/test';
import { loginUser, logoutIfVisible, registerUser } from '../helpers/auth.e2e-helper';
import { createScenarioData } from '../helpers/test-data';

test('registers a collector, reaches collections, opens profile and logs in again', async ({ page }) => {
  const data = createScenarioData();

  await registerUser(page, data.user);
  await page.goto('/profile');
  await expect(page.getByTestId('profile-page')).toBeVisible();
  await expect(page.getByText(data.user.email).first()).toBeVisible();

  await logoutIfVisible(page);
  await loginUser(page, data.user);

  await page.goto('/wanted');
  await expect(page.getByRole('heading', { name: /Buscados|Wanted/i })).toBeVisible();
});
