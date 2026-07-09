export type CatalogRecordStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';
export type CatalogSeriesType = 'BOOK' | 'COMIC' | 'MANGA';
export type CatalogPublicationStatus =
  | 'ONGOING'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'HIATUS'
  | 'UNKNOWN';

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
