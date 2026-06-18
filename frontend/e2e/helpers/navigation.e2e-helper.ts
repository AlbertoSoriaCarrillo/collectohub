import { expect, type Locator, type Page } from '@playwright/test';

export async function expectAppLoaded(page: Page): Promise<void> {
  await expect(page.getByTestId('app-toolbar').first()).toBeVisible();
  await expect(page.getByTestId('app-brand').first()).toHaveText('CollectoHub');
}

export async function selectMatOption(
  page: Page,
  trigger: Locator,
  optionName: string | RegExp
): Promise<void> {
  await page
    .locator('.cdk-overlay-backdrop')
    .waitFor({ state: 'detached', timeout: 5_000 })
    .catch(() => undefined);
  const materialTrigger = trigger.locator('.mat-mdc-select-trigger');
  if (await materialTrigger.count()) {
    await materialTrigger.click({ force: true });
  } else {
    await trigger.click({ force: true });
  }
  const option =
    typeof optionName === 'string'
      ? page.getByRole('option', { name: optionName, exact: true })
      : page.getByRole('option', { name: optionName });
  await option.click();
  await page
    .locator('.cdk-overlay-backdrop')
    .waitFor({ state: 'detached', timeout: 5_000 })
    .catch(() => undefined);
}

export function idFromUrl(page: Page, pattern: RegExp): number {
  const match = page.url().match(pattern);
  if (!match?.[1]) {
    throw new Error(`Could not extract id from URL: ${page.url()}`);
  }
  return Number(match[1]);
}

export async function waitForNoAngularError(page: Page): Promise<void> {
  await expect(page.locator('app-root')).toBeVisible();
  await expect(page.locator('body')).not.toContainText('Cannot match any routes');
}
