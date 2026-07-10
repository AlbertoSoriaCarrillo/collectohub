export type CatalogRecordStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';
export type CatalogSeriesType = 'BOOK' | 'COMIC' | 'MANGA';
export type CatalogPublicationStatus =
  | 'ONGOING'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'HIATUS'
  | 'UNKNOWN';
export type CatalogItemEditionFormat =
  | 'HARDCOVER'
  | 'PAPERBACK'
  | 'SOFTCOVER'
  | 'DIGITAL'
  | 'OMNIBUS'
  | 'BOX_SET'
  | 'SINGLE_ISSUE'
  | 'OTHER';

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface PublisherResponse {
  id: number;
  name: string;
  country: string | null;
  recordStatus: CatalogRecordStatus;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreatePublisherRequest {
  name: string;
  country: string | null;
  recordStatus: CatalogRecordStatus;
}

export interface UpdatePublisherRequest extends CreatePublisherRequest {}

export interface CatalogFranchiseResponse {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  recordStatus: CatalogRecordStatus;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateCatalogFranchiseRequest {
  name: string;
  slug: string;
  description: string | null;
  recordStatus: CatalogRecordStatus;
}

export interface UpdateCatalogFranchiseRequest extends CreateCatalogFranchiseRequest {}

export interface CatalogSeriesResponse {
  id: number;
  franchiseId: number | null;
  franchiseName: string | null;
  primaryPublisherId: number | null;
  primaryPublisherName: string | null;
  title: string;
  originalTitle: string | null;
  type: CatalogSeriesType;
  publicationStatus: CatalogPublicationStatus;
  description: string | null;
  originCountry: string | null;
  originalLanguage: string | null;
  startYear: number | null;
  endYear: number | null;
  recordStatus: CatalogRecordStatus;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateCatalogSeriesRequest {
  franchiseId: number | null;
  primaryPublisherId: number | null;
  title: string;
  originalTitle: string | null;
  type: CatalogSeriesType;
  publicationStatus: CatalogPublicationStatus;
  description: string | null;
  originCountry: string | null;
  originalLanguage: string | null;
  startYear: number | null;
  endYear: number | null;
  recordStatus: CatalogRecordStatus;
}

export interface UpdateCatalogSeriesRequest extends CreateCatalogSeriesRequest {}

export interface CatalogItemResponse {
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
  recordStatus: CatalogRecordStatus;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateCatalogItemRequest {
  title: string;
  originalTitle: string | null;
  sequenceLabel: string | null;
  sortOrder: number | null;
  description: string | null;
  firstPublicationDate: string | null;
  firstPublicationYear: number | null;
  originalLanguage: string | null;
  originCountry: string | null;
  recordStatus: CatalogRecordStatus;
}

export interface UpdateCatalogItemRequest extends CreateCatalogItemRequest {}

export interface CatalogItemEditionResponse {
  id: number;
  catalogItemId: number;
  catalogItemTitle: string;
  publisherId: number | null;
  publisherName: string | null;
  isbn: string | null;
  ean: string | null;
  format: CatalogItemEditionFormat;
  editionName: string | null;
  publicationDate: string | null;
  publicationYear: number | null;
  language: string | null;
  country: string | null;
  pageCount: number | null;
  coverImageUrl: string | null;
  recordStatus: CatalogRecordStatus;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateCatalogItemEditionRequest {
  publisherId: number | null;
  isbn: string | null;
  ean: string | null;
  format: CatalogItemEditionFormat;
  editionName: string | null;
  publicationDate: string | null;
  publicationYear: number | null;
  language: string | null;
  country: string | null;
  pageCount: number | null;
  coverImageUrl: string | null;
  recordStatus: CatalogRecordStatus;
}

export interface UpdateCatalogItemEditionRequest extends CreateCatalogItemEditionRequest {}

export interface EditorialAdminSearchParams {
  q?: string | null;
  recordStatus?: CatalogRecordStatus | null;
  page?: number | null;
  size?: number | null;
  sort?: string | null;
}

export interface EditorialAdminSeriesSearchParams extends EditorialAdminSearchParams {
  franchiseId?: number | null;
  type?: CatalogSeriesType | null;
  publicationStatus?: CatalogPublicationStatus | null;
  publisherId?: number | null;
  language?: string | null;
  country?: string | null;
}

export interface EditorialAdminItemSearchParams extends EditorialAdminSearchParams {
  publicationYear?: number | null;
  language?: string | null;
  country?: string | null;
}

export interface EditorialAdminEditionSearchParams extends EditorialAdminSearchParams {
  publisherId?: number | null;
  isbn?: string | null;
  ean?: string | null;
  format?: CatalogItemEditionFormat | null;
  language?: string | null;
  country?: string | null;
  publicationYear?: number | null;
}
