export type ShopProductCommercialStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'HIDDEN';

export type PhysicalCondition = 'NEW' | 'LIKE_NEW' | 'GOOD' | 'ACCEPTABLE' | 'DAMAGED';

export interface ShopProductResponse {
  id: number;
  shopId: number;
  masterProductId: number | null;
  masterProductName: string | null;
  masterProductCategoryCode: string | null;
  masterProductFranchise: string | null;
  masterProductCollectionName: string | null;
  masterProductVolumeNumber: string | null;
  catalogItemId?: number | null;
  catalogItemTitle?: string | null;
  catalogItemSequenceLabel?: string | null;
  catalogSeriesId?: number | null;
  catalogSeriesTitle?: string | null;
  catalogItemEditionId?: number | null;
  catalogItemEditionName?: string | null;
  catalogItemEditionFormat?: string | null;
  catalogItemEditionIsbn?: string | null;
  catalogItemEditionEan?: string | null;
  catalogItemEditionCoverImageUrl?: string | null;
  catalogPublisherName?: string | null;
  catalogFranchiseName?: string | null;
  editorialReferenceSource?: 'LEGACY' | 'VERIFIED_BRIDGE' | 'MANUAL_EDITORIAL';
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
  masterProductId?: number | null;
  catalogItemId?: number | null;
  catalogItemEditionId?: number | null;
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
  masterProductId?: number | null;
  catalogItemId?: number | null;
  catalogItemEditionId?: number | null;
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
