import { expect, test } from '@playwright/test';
import { registerUser } from '../helpers/auth.e2e-helper';
import {
  createCollection
} from '../helpers/mvp-flow.e2e-helper';
import { createScenarioData } from '../helpers/test-data';

test('validates the collection-focused MVP flow from the UI', async ({ page }) => {
  const data = createScenarioData();

  await registerUser(page, data.user);

  const collectionId = await createCollection(page, data.collection);
  await expect(page.getByRole('heading', { name: data.collection.name })).toBeVisible();
  await page.goto(`/collections/${collectionId}`);
  await expect(page.getByRole('heading', { name: data.collection.name })).toBeVisible();

  await page.goto('/catalog');
  await expect(page.getByRole('heading', { name: /Catalogo|Catalog|Obras|Works/i })).toBeVisible();

  await page.goto('/wanted');
  await expect(page.getByRole('heading', { name: /Buscados|Wanted/i })).toBeVisible();

  await page.goto('/profile');
  await expect(page.getByTestId('profile-page')).toBeVisible();
  await expect(page.getByText(data.user.email).first()).toBeVisible();
});
