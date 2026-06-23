import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import {
  RecommendedShopProductResponse,
  UserRecommendationSummaryResponse
} from '../../../core/models/recommendation.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { RecommendationService } from '../../../core/services/recommendation.service';
import { RecommendationsComponent } from './recommendations.component';

describe('RecommendationsComponent', () => {
  const categories: ProductCategoryResponse[] = [
    { id: 1, code: 'MANGA_COMIC', name: 'Manga / Comic', parentId: null }
  ];

  const recommendation: RecommendedShopProductResponse = {
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
  };

  const summary: UserRecommendationSummaryResponse = {
    missingCollectionItems: 2,
    wantedCollectionItems: 1,
    recommendedProducts: 1,
    matchedShops: 1,
    matchedCategoryCodes: ['MANGA_COMIC']
  };

  let recommendationService: {
    getMyRecommendations: ReturnType<typeof vi.fn>;
    getMyRecommendationSummary: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    recommendationService = {
      getMyRecommendations: vi.fn(() =>
        of({ recommendations: [recommendation], totalRecommendations: 1 })
      ),
      getMyRecommendationSummary: vi.fn(() => of(summary))
    };

    await TestBed.configureTestingModule({
      imports: [RecommendationsComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: CatalogService,
          useValue: {
            getCategories: vi.fn(() => of(categories))
          }
        },
        {
          provide: RecommendationService,
          useValue: recommendationService
        },
        {
          provide: ErrorMessageService,
          useValue: {
            toMessage: vi.fn(() => 'Error')
          }
        }
      ]
    }).compileComponents();
  });

  it('renders summary and recommendations', async () => {
    const fixture = TestBed.createComponent(RecommendationsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('Manga pendientes');
    expect(compiled.textContent).toMatch(/Me falta|Missing/);
    expect(compiled.textContent).toContain('MANGA_COMIC');
  });

  it('renders no collections empty state', async () => {
    recommendationService.getMyRecommendations.mockReturnValue(
      of({ recommendations: [], totalRecommendations: 0 })
    );
    recommendationService.getMyRecommendationSummary.mockReturnValue(
      of({
        missingCollectionItems: 0,
        wantedCollectionItems: 0,
        recommendedProducts: 0,
        matchedShops: 0,
        matchedCategoryCodes: []
      })
    );

    const fixture = TestBed.createComponent(RecommendationsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toMatch(
      /No hay elementos buscados o faltantes|No wanted or missing items/
    );
  });

  it('sends filters when searching', async () => {
    const fixture = TestBed.createComponent(RecommendationsComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;
    component.filters.setValue({
      categoryCode: 'MANGA_COMIC',
      maxPrice: 20,
      currency: 'EUR',
      physicalCondition: 'NEW'
    });
    component.loadRecommendations();

    expect(recommendationService.getMyRecommendations).toHaveBeenLastCalledWith({
      categoryCode: 'MANGA_COMIC',
      maxPrice: 20,
      currency: 'EUR',
      physicalCondition: 'NEW',
      shopId: null
    });
    expect(recommendationService.getMyRecommendationSummary).toHaveBeenLastCalledWith({
      categoryCode: 'MANGA_COMIC',
      maxPrice: 20,
      currency: 'EUR',
      physicalCondition: 'NEW',
      shopId: null
    });
  });
});
