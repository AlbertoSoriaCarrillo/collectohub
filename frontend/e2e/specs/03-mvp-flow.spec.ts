import { expect, test } from '@playwright/test';
import { loginUser, logoutIfVisible, registerUser } from '../helpers/auth.e2e-helper';
import {
  addMissingItemToCollection,
  createCollection,
  createMasterProduct,
  createReservation,
  createShop,
  createShopProduct
} from '../helpers/mvp-flow.e2e-helper';
import { createScenarioData } from '../helpers/test-data';

test('validates the main MVP flow from the UI', async ({ page }) => {
  const data = createScenarioData();

  await registerUser(page, data.user);
  const shopId = await createShop(page, data.shop);

  await logoutIfVisible(page);
  await loginUser(page, data.user);

  await createMasterProduct(page, data.masterProduct);
  const shopProductId = await createShopProduct(page, shopId, data.masterProduct);

  const collectionId = await createCollection(page, data.collection);
  await addMissingItemToCollection(page, collectionId, data.masterProduct);

  await page.goto('/recommendations');
  await expect(page.getByTestId('recommendations-list')).toBeVisible();
  await expect(page.getByTestId('recommendation-card').filter({ hasText: data.masterProduct.name })).toBeVisible();

  await createReservation(page, shopProductId);
  await page.goto('/reservations');
  await expect(page.getByTestId('reservations-list')).toBeVisible();
  await expect(page.getByTestId('reservation-card').filter({ hasText: data.masterProduct.name })).toBeVisible();
});
