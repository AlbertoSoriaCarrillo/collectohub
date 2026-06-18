export interface ProductCategoryResponse {
  id: number;
  code: string;
  name: string;
  parentId: number | null;
}

export interface MasterProductResponse {
  id: number;
  name: string;
  description: string | null;
  category: ProductCategoryResponse;
  franchise: string | null;
  collectionName: string | null;
  volumeNumber: string | null;
  publisher: string | null;
  isbn: string | null;
  ean: string | null;
  releaseDate: string | null;
  editionStartDate: string | null;
  editionEndDate: string | null;
  language: string | null;
  limitedEdition: boolean;
  limitedEditionTotalUnits: number | null;
  publicationCountries: string[];
  coverImageUrl: string | null;
  status: string;
  attributes: Record<string, unknown>;
}

export interface CreateMasterProductRequest {
  name: string;
  description?: string | null;
  categoryCode: string;
  franchise?: string | null;
  collectionName?: string | null;
  volumeNumber?: string | null;
  publisher?: string | null;
  isbn?: string | null;
  ean?: string | null;
  releaseDate?: string | null;
  editionStartDate?: string | null;
  editionEndDate?: string | null;
  language?: string | null;
  limitedEdition?: boolean | null;
  limitedEditionTotalUnits?: number | null;
  publicationCountries?: string[];
  coverImageUrl?: string | null;
  attributes?: Record<string, unknown>;
}

export type UpdateMasterProductRequest = Partial<CreateMasterProductRequest>;

export interface MasterProductSearchFilters {
  categoryCode?: string | null;
  name?: string | null;
  franchise?: string | null;
  collectionName?: string | null;
  language?: string | null;
  status?: string | null;
}
