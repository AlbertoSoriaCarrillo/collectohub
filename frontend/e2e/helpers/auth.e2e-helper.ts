import { expect, type Page } from '@playwright/test';
import type { E2eUserData } from './test-data';
import { expectAppLoaded } from './navigation.e2e-helper';

export async function registerUser(page: Page, user: E2eUserData): Promise<void> {
  await page.goto('/register');
  await expectAppLoaded(page);
  await page.getByTestId('register-email').fill(user.email);
  await page.getByTestId('register-password').fill(user.password);
  await page.getByTestId('register-confirm-password').fill(user.password);
  await page.getByTestId('register-display-name').fill(user.displayName);
  await page.getByTestId('register-submit').click();
  await expect(page).toHaveURL(/\/collections$/);
  await expect(page.getByRole('heading', { name: /biblioteca|library/i })).toBeVisible();
  await expect(page.getByTestId('session-label')).toContainText(user.displayName);
}

export async function loginUser(page: Page, user: E2eUserData): Promise<void> {
  await page.goto('/login');
  await expectAppLoaded(page);
  await page.getByTestId('login-email').fill(user.email);
  await page.getByTestId('login-password').fill(user.password);
  await page.getByTestId('login-submit').click();
  await expect(page).toHaveURL(/\/collections$/);
  await expect(page.getByRole('heading', { name: /biblioteca|library/i })).toBeVisible();
  await expect(page.getByTestId('session-label')).toContainText(user.displayName);
}

export async function logoutIfVisible(page: Page): Promise<void> {
  const logout = page.getByTestId('logout-button');
  if (await logout.isVisible().catch(() => false)) {
    await logout.click();
    await expect(page.getByTestId('login-link')).toBeVisible();
  }
}
