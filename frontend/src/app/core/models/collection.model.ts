import { PhysicalCondition } from './inventory.model';

export type CollectionVisibility = 'PRIVATE' | 'PUBLIC';

export type CollectionItemStatus =
  | 'OWNED'
  | 'WANTED'
  | 'MISSING'
  | 'DUPLICATED'
  | 'SELLABLE'
  | 'TRADABLE';

export interface CollectionItemResponse {
  id: number;
  collectionId: number;
  masterProductId: number;
  masterProductName: string;
  masterProductCategoryCode: string;
  masterProductFranchise: string | null;
  masterProductCollectionName: string | null;
  masterProductVolumeNumber: string | null;
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
  masterProductId: number;
  collectionStatus: CollectionItemStatus;
  physicalCondition?: PhysicalCondition | null;
  unitNumber?: string | null;
  totalLimitedUnits?: number | null;
  notes?: string | null;
  acquiredAt?: string | null;
}

export interface UpdateCollectionItemRequest {
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
  'DUPLICATED',
  'SELLABLE',
  'TRADABLE'
];
