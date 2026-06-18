import { PhysicalCondition } from './inventory.model';

export interface RecommendationReasonResponse {
  code: string;
  message: string;
}

export interface RecommendedShopProductResponse {
  shopProductId: number;
  shopId: number;
  shopName: string;
  masterProductId: number;
  productName: string;
  categoryCode: string;
  franchise: string | null;
  collectionName: string | null;
  volumeNumber: string | null;
  coverImageUrl: string | null;
  priceAmount: number;
  currency: string;
  stockQuantity: number;
  physicalCondition: PhysicalCondition;
  commercialStatus: string;
  recommendationReason: RecommendationReasonResponse;
  matchedCollectionId: number;
  matchedCollectionName: string;
  matchedCollectionItemStatus: 'MISSING' | 'WANTED';
}

export interface UserRecommendationResponse {
  recommendations: RecommendedShopProductResponse[];
  totalRecommendations: number;
}

export interface UserRecommendationSummaryResponse {
  missingCollectionItems: number;
  wantedCollectionItems: number;
  recommendedProducts: number;
  matchedShops: number;
  matchedCategoryCodes: string[];
}

export interface RecommendationFilters {
  categoryCode?: string | null;
  maxPrice?: number | null;
  currency?: string | null;
  physicalCondition?: PhysicalCondition | null;
  shopId?: number | null;
}
