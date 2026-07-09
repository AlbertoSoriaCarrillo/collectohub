export type EditorialCatalogResultType = 'SERIES' | 'ITEM' | 'EDITION' | 'MASTER_PRODUCT_LINK';
export type EditorialPublicResultType = Exclude<EditorialCatalogResultType, 'MASTER_PRODUCT_LINK'>;
export type EditorialSeriesType = 'BOOK' | 'COMIC' | 'MANGA';
export type EditorialCatalogRelationshipType =
  | 'ADAPTATION'
  | 'REMAKE'
  | 'REPRINT'
  | 'SAME_WORK'
  | 'SPIN_OFF'
  | 'PREQUEL'
  | 'SEQUEL'
  | 'RELATED';
export type EditorialCatalogRelationshipDirection = 'OUTGOING' | 'INCOMING';

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface EditorialCatalogSearchItem {
  resultType: EditorialCatalogResultType;
  seriesId: number | null;
  seriesTitle: string | null;
  itemId: number | null;
  itemTitle: string | null;
  editionId: number | null;
  editionName: string | null;
  publisherName: string | null;
  franchiseName: string | null;
  type: EditorialSeriesType | null;
  language: string | null;
  country: string | null;
  publicationYear: number | null;
  coverImageUrl: string | null;
  linkedMasterProductId: number | null;
  linkedMasterProductName: string | null;
}

export interface EditorialPublisher {
  id: number;
  name: string;
  country: string | null;
  recordStatus: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface EditorialFranchise {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  recordStatus: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface EditorialSeries {
  id: number;
  franchiseId: number | null;
  franchiseName: string | null;
  primaryPublisherId: number | null;
  primaryPublisherName: string | null;
  title: string;
  originalTitle: string | null;
  type: EditorialSeriesType;
  publicationStatus: string;
  description: string | null;
  originCountry: string | null;
  originalLanguage: string | null;
  startYear: number | null;
  endYear: number | null;
  recordStatus: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface EditorialItem {
  id: number;
  seriesId: number;
  seriesTitle: string;
  title: string;
  originalTitle: string | null;
  sequenceLabel: string | null;
  sortOrder: number | null;
  description: string | null;
  firstPublicationDate: string | null;
  firstPublicationYear: number | null;
  originalLanguage: string | null;
  originCountry: string | null;
  recordStatus: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface EditorialEdition {
  id: number;
  catalogItemId: number;
  catalogItemTitle: string;
  publisherId: number | null;
  publisherName: string | null;
  isbn: string | null;
  ean: string | null;
  format: string;
  editionName: string | null;
  publicationDate: string | null;
  publicationYear: number | null;
  language: string | null;
  country: string | null;
  pageCount: number | null;
  coverImageUrl: string | null;
  recordStatus: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface EditorialCatalogDetail {
  series: EditorialSeries;
  franchise: EditorialFranchise | null;
  primaryPublisher: EditorialPublisher | null;
}

export type EditorialCreatorCreditRole =
  | 'AUTHOR'
  | 'WRITER'
  | 'ARTIST'
  | 'ILLUSTRATOR'
  | 'TRANSLATOR'
  | 'EDITOR'
  | 'OTHER';

export interface EditorialCatalogCreatorCreditResponse {
  id: number;
  creatorId: number;
  creatorName: string;
  creatorSlug: string;
  creditRole: EditorialCreatorCreditRole;
  creditOrder: number;
  creditLabel: string | null;
}

export interface EditorialCatalogItemRelationshipResponse {
  id: number;
  sourceCatalogItemId: number;
  sourceCatalogItemTitle: string;
  sourceCatalogSeriesId: number;
  sourceCatalogSeriesTitle: string;
  targetCatalogItemId: number;
  targetCatalogItemTitle: string;
  targetCatalogSeriesId: number;
  targetCatalogSeriesTitle: string;
  relationshipType: EditorialCatalogRelationshipType;
  relationshipOrder: number;
  description: string | null;
  direction: EditorialCatalogRelationshipDirection;
}

export interface EditorialCatalogItemDetail {
  catalog: EditorialCatalogDetail;
  item: EditorialItem;
  editions: EditorialEdition[];
  creators?: EditorialCatalogCreatorCreditResponse[];
  relationships?: EditorialCatalogItemRelationshipResponse[];
}

export interface EditorialCatalogSeriesDetail {
  catalog: EditorialCatalogDetail;
  items: EditorialCatalogItemDetail[];
}

export interface EditorialCatalogEditionDetail {
  catalog: EditorialCatalogDetail;
  item: EditorialItem;
  edition: EditorialEdition;
  publisher: EditorialPublisher | null;
}

export interface EditorialLegacyBridge {
  linkId: number;
  masterProductId: number;
  masterProductName: string;
  linkStatus: string;
  linkSource: string;
  confidenceScore: number | null;
  matchReason: string | null;
  catalogItemId: number;
  catalogItemTitle: string;
  catalogItemEditionId: number | null;
  catalogItemEditionLabel: string | null;
}

export interface EditorialCatalogSearchParams {
  q?: string | null;
  type?: EditorialSeriesType | null;
  franchiseId?: number | null;
  seriesId?: number | null;
  publisherId?: number | null;
  language?: string | null;
  country?: string | null;
  publicationYear?: number | null;
  resultType?: EditorialPublicResultType | null;
  page?: number;
  size?: number;
  sort?: string;
}
