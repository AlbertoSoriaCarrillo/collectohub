import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  UserRecommendationResponse,
  UserRecommendationSummaryResponse
} from '../models/recommendation.model';
import { RecommendationService } from './recommendation.service';

describe('RecommendationService', () => {
  let service: RecommendationService;
  let httpTestingController: HttpTestingController;

  const response: UserRecommendationResponse = {
    totalRecommendations: 1,
    recommendations: [
      {
        shopProductId: 17,
        shopId: 4,
        shopName: 'Manga Shop',
        masterProductId: 9,
        productName: 'One Piece 1',
        categoryCode: 'MANGA_COMIC',
        franchise: 'One Piece',
        collectionName: 'One Piece',
        volumeNumber: '1',
        coverImageUrl: null,
        priceAmount: 12.95,
        currency: 'EUR',
        stockQuantity: 2,
        physicalCondition: 'NEW',
        commercialStatus: 'AVAILABLE',
        recommendationReason: {
          code: 'COLLECTION_ITEM_MISSING',
          message: 'Product marked as missing in one of your collections'
        },
        matchedCollectionId: 3,
        matchedCollectionName: 'Manga pendientes',
        matchedCollectionItemStatus: 'MISSING'
      }
    ]
  };

  const summary: UserRecommendationSummaryResponse = {
    missingCollectionItems: 2,
    wantedCollectionItems: 1,
    recommendedProducts: 1,
    matchedShops: 1,
    matchedCategoryCodes: ['MANGA_COMIC']
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(RecommendationService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('loads my recommendations with filters', () => {
    service
      .getMyRecommendations({
        categoryCode: 'MANGA_COMIC',
        maxPrice: 20,
        currency: 'EUR',
        physicalCondition: 'NEW',
        shopId: 4
      })
      .subscribe((result) => {
        expect(result).toEqual(response);
      });

    const request = httpTestingController.expectOne((candidate) => {
      const params = candidate.params;
      return (
        candidate.url === 'http://localhost:8080/api/recommendations/my' &&
        params.get('categoryCode') === 'MANGA_COMIC' &&
        params.get('maxPrice') === '20' &&
        params.get('currency') === 'EUR' &&
        params.get('physicalCondition') === 'NEW' &&
        params.get('shopId') === '4'
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush(response);
  });

  it('loads my recommendation summary', () => {
    service.getMyRecommendationSummary({ categoryCode: 'MANGA_COMIC' }).subscribe((result) => {
      expect(result).toEqual(summary);
    });

    const request = httpTestingController.expectOne((candidate) => {
      return (
        candidate.url === 'http://localhost:8080/api/recommendations/my/summary' &&
        candidate.params.get('categoryCode') === 'MANGA_COMIC'
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush(summary);
  });
});
