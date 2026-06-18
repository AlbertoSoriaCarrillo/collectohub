import { expect, type Page } from '@playwright/test';
import type {
  E2eCollectionData,
  E2eMasterProductData,
  E2eShopData
} from './test-data';
import { idFromUrl, selectMatOption } from './navigation.e2e-helper';

export async function createShop(page: Page, shop: E2eShopData): Promise<number> {
  await page.goto('/shops/new');
  await page.getByTestId('create-shop-name').fill(shop.name);
  await page.getByTestId('create-shop-contact-email').fill(shop.contactEmail);
  await page.getByTestId('create-shop-country').fill('ES');
  await page.getByTestId('create-shop-submit').click();
  await expect(page).toHaveURL(/\/shops\/\d+$/);
  await expect(page.locator('mat-card-title').filter({ hasText: shop.name }).first()).toBeVisible();
  return idFromUrl(page, /\/shops\/(\d+)$/);
}

export async function createMasterProduct(
  page: Page,
  product: E2eMasterProductData
): Promise<number> {
  await page.goto('/catalog/new');
  await page.getByTestId('create-master-product-name').fill(product.name);
  await selectMatOption(
    page,
    page.getByTestId('create-master-product-category'),
    /Manga and comic/i
  );
  await page.getByTestId('create-master-product-franchise').fill(product.franchise);
  await page.getByTestId('create-master-product-collection').fill(product.collectionName);
  await page.getByTestId('create-master-product-volume').fill('1');
  await page.getByLabel('ISBN').fill(product.isbn);
  await page.getByLabel('EAN').fill(product.ean);
  await page.getByTestId('create-master-product-language').fill('es');
  await page.getByTestId('create-master-product-countries').fill('ES');
  await page.getByTestId('create-master-product-submit').click();
  await expect(page).toHaveURL(/\/catalog\/\d+$/);
  await expect(page.getByRole('heading', { name: product.name })).toBeVisible();
  return idFromUrl(page, /\/catalog\/(\d+)$/);
}

export async function createShopProduct(
  page: Page,
  shopId: number,
  product: E2eMasterProductData
): Promise<number> {
  await page.goto(`/shops/${shopId}/inventory/new`);
  await page.getByTestId('shop-product-search-master-product').fill(product.name);
  await page.getByTestId('shop-product-search-submit').click();
  await selectMatOption(
    page,
    page.getByTestId('shop-product-master-product'),
    new RegExp(product.name)
  );
  await page.getByTestId('shop-product-price').fill('12.95');
  await page.getByTestId('shop-product-stock').fill('3');
  await selectMatOption(
    page,
    page.getByTestId('shop-product-physical-condition'),
    /Nuevo|New/i
  );
  await page.getByTestId('create-shop-product-submit').click();
  await expect(page).toHaveURL(new RegExp(`/shops/${shopId}/inventory$`));

  await page.goto(`/shops/${shopId}`);
  const publicProduct = page.getByTestId('public-shop-product-link').filter({ hasText: product.name });
  await expect(publicProduct).toBeVisible();
  const href = await publicProduct.first().getAttribute('href');
  const match = href?.match(/\/shop-products\/(\d+)/);
  if (!match?.[1]) {
    throw new Error(`Could not extract shop product id from href: ${href}`);
  }
  return Number(match[1]);
}

export async function createCollection(
  page: Page,
  collection: E2eCollectionData
): Promise<number> {
  await page.goto('/collections/new');
  await page.getByTestId('create-collection-name').fill(collection.name);
  await selectMatOption(
    page,
    page.getByTestId('create-collection-category'),
    /Manga and comic/i
  );
  await page.getByTestId('create-collection-submit').click();
  await expect(page).toHaveURL(/\/collections\/\d+$/);
  await expect(page.getByRole('heading', { name: collection.name })).toBeVisible();
  return idFromUrl(page, /\/collections\/(\d+)$/);
}

export async function addMissingItemToCollection(
  page: Page,
  collectionId: number,
  product: E2eMasterProductData
): Promise<void> {
  await page.goto(`/collections/${collectionId}/items/new`);
  await page.getByTestId('collection-item-search-master-product').fill(product.name);
  await page.getByTestId('collection-item-search-submit').click();
  await selectMatOption(
    page,
    page.getByTestId('collection-item-master-product'),
    new RegExp(product.name)
  );
  await selectMatOption(page, page.getByTestId('collection-item-status'), /Faltante|Missing/i);
  await page.getByTestId('add-collection-item-submit').click();
  await expect(page).toHaveURL(new RegExp(`/collections/${collectionId}$`));
  await expect(
    page.getByTestId('collection-item-card').filter({ hasText: product.name })
  ).toBeVisible();
}

export async function createReservation(page: Page, shopProductId: number): Promise<number> {
  await page.goto(`/shop-products/${shopProductId}`);
  await expect(page.getByTestId('shop-product-detail')).toBeVisible();
  await page.getByTestId('create-reservation-submit').click();
  await expect(page).toHaveURL(/\/reservations\/\d+$/);
  return idFromUrl(page, /\/reservations\/(\d+)$/);
}
