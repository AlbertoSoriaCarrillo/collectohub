import { PhysicalCondition } from './inventory.model';

export type CollectionVisibility = 'PRIVATE' | 'PUBLIC';

export type CollectionItemStatus =
  | 'OWNED'
  | 'WANTED'
  | 'MISSING'
  | 'DUPLICATED'
  | 'SELLABLE'
  | 'TRADABLE';

export type CollectionEditorialReferenceSource = 'LEGACY' | 'VERIFIED_BRIDGE' | 'MANUAL_EDITORIAL' | 'MANUAL';
export type CollectionItemReferenceKind =
  | 'DIRECT_CATALOG'
  | 'VERIFIED_BRIDGE'
  | 'LEGACY_UNRESOLVED'
  | 'MANUAL'
  | 'INVALID_REFERENCE';

export interface CollectionItemResponse {
  id: number;
  collectionId: number;
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
  editorialReferenceSource?: CollectionEditorialReferenceSource;
  referenceKind?: CollectionItemReferenceKind;
  manualTitle: string | null;
  manualDescription: string | null;
  manualType: string | null;
  collectionStatus: CollectionItemStatus;
  physicalCondition: PhysicalCondition | null;
  unitNumber: string | null;
  totalLimitedUnits: number | null;
  notes: string | null;
  acquiredAt: string | null;
}

export interface CollectionResponse {
  id: number;
  userId: number;
  name: string;
  description: string | null;
  visibility: CollectionVisibility;
  categoryCode: string | null;
  categoryName: string | null;
  items: CollectionItemResponse[];
}

export interface CreateCollectionRequest {
  name: string;
  description?: string | null;
  visibility?: CollectionVisibility | null;
  categoryCode?: string | null;
}

export interface UpdateCollectionRequest {
  name?: string | null;
  description?: string | null;
  visibility?: CollectionVisibility | null;
  categoryCode?: string | null;
}

export interface CreateCollectionItemRequest {
  masterProductId?: number | null;
  catalogItemId?: number | null;
  catalogItemEditionId?: number | null;
  manualTitle?: string | null;
  manualDescription?: string | null;
  manualType?: string | null;
  collectionStatus: CollectionItemStatus;
  physicalCondition?: PhysicalCondition | null;
  unitNumber?: string | null;
  totalLimitedUnits?: number | null;
  notes?: string | null;
  acquiredAt?: string | null;
}

export interface UpdateCollectionItemRequest {
  masterProductId?: number | null;
  catalogItemId?: number | null;
  catalogItemEditionId?: number | null;
  manualTitle?: string | null;
  manualDescription?: string | null;
  manualType?: string | null;
  collectionStatus?: CollectionItemStatus | null;
  physicalCondition?: PhysicalCondition | null;
  unitNumber?: string | null;
  totalLimitedUnits?: number | null;
  notes?: string | null;
  acquiredAt?: string | null;
}

export interface CollectionSearchFilters {
  visibility?: CollectionVisibility | null;
  categoryCode?: string | null;
}

export const COLLECTION_VISIBILITIES: CollectionVisibility[] = ['PRIVATE', 'PUBLIC'];

export const COLLECTION_ITEM_STATUSES: CollectionItemStatus[] = [
  'OWNED',
  'WANTED',
  'MISSING',
  'DUPLICATED'
];
