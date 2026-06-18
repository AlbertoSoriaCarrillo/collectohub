export type ShopProductCommercialStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'HIDDEN';

export type PhysicalCondition = 'NEW' | 'LIKE_NEW' | 'GOOD' | 'ACCEPTABLE' | 'DAMAGED';

export interface ShopProductResponse {
  id: number;
  shopId: number;
  masterProductId: number;
  masterProductName: string;
  masterProductCategoryCode: string;
  masterProductFranchise: string | null;
  masterProductCollectionName: string | null;
  masterProductVolumeNumber: string | null;
  priceAmount: number;
  currency: string;
  stockQuantity: number;
  commercialStatus: ShopProductCommercialStatus;
  physicalCondition: PhysicalCondition;
  visible: boolean;
  unitNumber: string | null;
  totalLimitedUnits: number | null;
  notes: string | null;
}

export interface CreateShopProductRequest {
  masterProductId: number;
  priceAmount: number;
  currency?: string | null;
  stockQuantity: number;
  commercialStatus?: ShopProductCommercialStatus | null;
  physicalCondition: PhysicalCondition;
  visible?: boolean | null;
  unitNumber?: string | null;
  totalLimitedUnits?: number | null;
  notes?: string | null;
}

export interface UpdateShopProductRequest {
  priceAmount?: number | null;
  currency?: string | null;
  stockQuantity?: number | null;
  commercialStatus?: ShopProductCommercialStatus | null;
  physicalCondition?: PhysicalCondition | null;
  visible?: boolean | null;
  unitNumber?: string | null;
  totalLimitedUnits?: number | null;
  notes?: string | null;
}

export interface ShopProductSearchFilters {
  masterProductId?: number | null;
  categoryCode?: string | null;
  name?: string | null;
  franchise?: string | null;
  collectionName?: string | null;
  physicalCondition?: PhysicalCondition | null;
  commercialStatus?: ShopProductCommercialStatus | null;
}

export const SHOP_PRODUCT_COMMERCIAL_STATUSES: ShopProductCommercialStatus[] = [
  'AVAILABLE',
  'RESERVED',
  'SOLD',
  'HIDDEN'
];

export const PHYSICAL_CONDITIONS: PhysicalCondition[] = [
  'NEW',
  'LIKE_NEW',
  'GOOD',
  'ACCEPTABLE',
  'DAMAGED'
];
