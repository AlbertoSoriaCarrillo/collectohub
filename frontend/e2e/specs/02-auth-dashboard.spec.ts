import { expect, test } from '@playwright/test';
import { loginUser, logoutIfVisible } from '../helpers/auth.e2e-helper';
import { createScenarioData } from '../helpers/test-data';

test('registers a collector, reaches collections, opens profile and logs in again', async ({ page }) => {
  const data = createScenarioData();

  await page.goto('/home');
  await expect(page.getByTestId('login-header-link')).toBeVisible();
  await expect(page.getByTestId('register-link')).toHaveCount(0);

  await page.getByTestId('login-header-link').click();
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByTestId('login-register-link')).toBeVisible();

  await page.getByTestId('login-register-link').click();
  await expect(page).toHaveURL(/\/register$/);
  await page.getByTestId('register-email').fill(data.user.email);
  await page.getByTestId('register-password').fill(data.user.password);
  await page.getByTestId('register-confirm-password').fill(data.user.password);
  await page.getByTestId('register-display-name').fill(data.user.displayName);
  await page.getByTestId('register-submit').click();
  await expect(page).toHaveURL(/\/collections$/);
  await expect(page.getByTestId('user-menu-button')).toBeVisible();

  await page.getByTestId('user-menu-button').click();
  await page.getByTestId('user-menu-profile').click();
  await page.goto('/profile');
  await expect(page.getByTestId('profile-page')).toBeVisible();
  await expect(page.getByText(data.user.email).first()).toBeVisible();

  await logoutIfVisible(page);
  await loginUser(page, data.user);

  await page.goto('/wanted');
  await expect(page.getByRole('heading', { name: /Buscados|Wanted/i })).toBeVisible();
});
