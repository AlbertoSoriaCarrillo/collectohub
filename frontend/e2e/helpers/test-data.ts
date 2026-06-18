export interface E2eUserData {
  email: string;
  password: string;
  displayName: string;
}

export interface E2eShopData {
  name: string;
  contactEmail: string;
}

export interface E2eMasterProductData {
  name: string;
  franchise: string;
  collectionName: string;
  isbn: string;
  ean: string;
}

export interface E2eCollectionData {
  name: string;
}

export interface E2eScenarioData {
  suffix: string;
  user: E2eUserData;
  shop: E2eShopData;
  masterProduct: E2eMasterProductData;
  collection: E2eCollectionData;
}

export function uniqueSuffix(): string {
  return `${Date.now()}${Math.floor(Math.random() * 10000)}`;
}

export function uniqueEmail(suffix = uniqueSuffix()): string {
  return `e2e-${suffix}@collectohub.local`;
}

export function createScenarioData(): E2eScenarioData {
  const suffix = uniqueSuffix();
  const numericTail = suffix.slice(-10).padStart(10, '0');

  return {
    suffix,
    user: {
      email: uniqueEmail(suffix),
      password: 'Password123!',
      displayName: `E2E User ${suffix}`
    },
    shop: {
      name: `E2E Shop ${suffix}`,
      contactEmail: `shop-${suffix}@collectohub.local`
    },
    masterProduct: {
      name: `E2E Manga Volume ${suffix}`,
      franchise: `E2E Franchise ${suffix}`,
      collectionName: `E2E Series ${suffix}`,
      isbn: `978${numericTail}`,
      ean: `20${suffix.slice(-11).padStart(11, '0')}`
    },
    collection: {
      name: `E2E Collection ${suffix}`
    }
  };
}

export function e2eApiBaseUrl(): string {
  return process.env['E2E_API_BASE_URL'] || 'http://localhost:8080';
}
