import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import {
  CollectionItemResponse,
  CollectionResponse,
  CollectionSeriesProgressSummaryResponse
} from '../../../core/models/collection.model';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionDetailComponent } from './collection-detail.component';

describe('CollectionDetailComponent', () => {
  const item: CollectionItemResponse = {
    id: 7,
    collectionId: 3,
    masterProductId: 5,
    masterProductName: 'One Piece 1',
    masterProductCategoryCode: 'MANGA_COMIC',
    masterProductFranchise: 'One Piece',
    masterProductCollectionName: 'One Piece',
    masterProductVolumeNumber: '1',
    manualTitle: null,
    manualDescription: null,
    manualType: null,
    collectionStatus: 'MISSING',
    physicalCondition: 'NEW',
    unitNumber: null,
    totalLimitedUnits: null,
    notes: 'Missing item',
    acquiredAt: null
  };

  const ownedItem = { ...item, id: 8, collectionStatus: 'OWNED' as const };
  const wantedItem = { ...item, id: 9, collectionStatus: 'WANTED' as const };
  const collection: CollectionResponse = {
    id: 3,
    userId: 2,
    name: 'Manga pendientes',
    description: 'Lista personal',
    visibility: 'PRIVATE',
    categoryCode: 'MANGA_COMIC',
    categoryName: 'Manga / Comic',
    items: [item, ownedItem, wantedItem]
  };
  const progress: CollectionSeriesProgressSummaryResponse[] = [{
    seriesId: 10,
    seriesTitle: 'One Piece',
    totalCatalogItems: 4,
    ownedItems: 1,
    wantedItems: 1,
    missingItems: 2,
    completionPercentage: 25
  }];

  let currentUser: ReturnType<typeof signal>;
  let routeValue: { snapshot: { paramMap: ReturnType<typeof convertToParamMap>; queryParamMap: ReturnType<typeof convertToParamMap> } };
  let service: {
    getCollection: ReturnType<typeof vi.fn>;
    getCollectionItems: ReturnType<typeof vi.fn>;
    getCollectionSeriesProgressSummary: ReturnType<typeof vi.fn>;
    deleteCollectionItem: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    currentUser = signal({ id: 2, email: 'user@example.com', displayName: 'Ada', preferredInterfaceLanguage: 'es', roles: ['USER'] });
    routeValue = {
      snapshot: {
        paramMap: convertToParamMap({ collectionId: '3' }),
        queryParamMap: convertToParamMap({})
      }
    };
    service = {
      getCollection: vi.fn(() => of(collection)),
      getCollectionItems: vi.fn(() => of([item, ownedItem, wantedItem])),
      getCollectionSeriesProgressSummary: vi.fn(() => of(progress)),
      deleteCollectionItem: vi.fn(() => of(null))
    };

    await TestBed.configureTestingModule({
      imports: [CollectionDetailComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: routeValue },
        { provide: CollectionService, useValue: service },
        {
          provide: AuthService,
          useValue: {
            currentUser,
            hasToken: vi.fn(() => true),
            getMe: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('renders persisted counts, legacy warning, filtered items and owner progress independently', async () => {
    const fixture = TestBed.createComponent(CollectionDetailComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Manga pendientes');
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.querySelector('[data-testid="collection-persisted-summary"]')?.textContent).toMatch(/3/);
    expect(compiled.querySelector('[data-testid="collection-legacy-missing-warning"]')).toBeTruthy();
    expect(compiled.querySelector('[data-testid="collection-series-progress-summary"]')?.textContent).toContain('25%');
    expect(service.getCollectionSeriesProgressSummary).toHaveBeenCalledWith(3);
  });

  it('hydrates valid filters from query params and sends them to the list endpoint', () => {
    routeValue.snapshot.queryParamMap = convertToParamMap({
      q: 'dragon',
      status: ['OWNED', 'WANTED'],
      referenceKind: ['DIRECT_CATALOG', 'MANUAL'],
      seriesId: '10',
      sort: 'TITLE_DESC'
    });

    const fixture = TestBed.createComponent(CollectionDetailComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.filterForm.getRawValue()).toEqual({
      q: 'dragon',
      status: ['OWNED', 'WANTED'],
      referenceKind: ['DIRECT_CATALOG', 'MANUAL'],
      seriesId: 10,
      sort: 'TITLE_DESC'
    });
    expect(service.getCollectionItems).toHaveBeenCalledWith(3, {
      q: 'dragon',
      status: ['OWNED', 'WANTED'],
      referenceKind: ['DIRECT_CATALOG', 'MANUAL'],
      seriesId: 10,
      sort: 'TITLE_DESC'
    });
  });

  it('applies and clears filters while keeping route query params shareable', () => {
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(CollectionDetailComponent);
    fixture.detectChanges();
    service.getCollectionItems.mockClear();

    fixture.componentInstance.filterForm.setValue({
      q: '  dragon ',
      status: ['OWNED'],
      referenceKind: ['DIRECT_CATALOG'],
      seriesId: 10,
      sort: 'NEWEST_ENTRY'
    });
    fixture.componentInstance.applyFilters();

    expect(service.getCollectionItems).toHaveBeenCalledWith(3, {
      q: 'dragon', status: ['OWNED'], referenceKind: ['DIRECT_CATALOG'], seriesId: 10, sort: 'NEWEST_ENTRY'
    });
    expect(navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: {
        q: 'dragon', status: ['OWNED'], referenceKind: ['DIRECT_CATALOG'], seriesId: 10, sort: 'NEWEST_ENTRY'
      }
    }));

    fixture.componentInstance.clearFilters();
    expect(fixture.componentInstance.filterForm.getRawValue()).toEqual({
      q: '', status: [], referenceKind: [], seriesId: null, sort: 'CATALOG_ORDER'
    });
    expect(navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { q: null, status: null, referenceKind: null, seriesId: null, sort: null }
    }));
  });

  it('does not request or render personal progress for a public reader', () => {
    currentUser.set({ id: 99, email: 'reader@example.com', displayName: 'Reader', preferredInterfaceLanguage: 'en', roles: ['ADMIN'] });
    const fixture = TestBed.createComponent(CollectionDetailComponent);
    fixture.detectChanges();

    expect(service.getCollectionSeriesProgressSummary).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('[data-testid="collection-series-progress-section"]')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('[data-testid="add-collection-item-link"]')).toBeFalsy();
  });

  it('keeps the persisted summary when the filtered list fails', () => {
    service.getCollectionItems.mockReturnValueOnce(throwError(() => new Error('list failed')));
    const fixture = TestBed.createComponent(CollectionDetailComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.persistedSummary()).toEqual({
      total: 3, owned: 1, wanted: 1, other: 1, legacyMissing: 1
    });
    expect(fixture.componentInstance.itemsError()).toBeTruthy();
    expect(fixture.componentInstance.collection()).toEqual(collection);
  });

  it('prefers editorial metadata and keeps reference fallbacks', () => {
    const component = TestBed.createComponent(CollectionDetailComponent).componentInstance;

    expect(component.itemTitle({ ...item, catalogItemTitle: 'Editorial title' })).toBe('Editorial title');
    expect(component.itemTitle(item)).toBe('One Piece 1');
    expect(component.referenceLabel({ ...item, referenceKind: 'DIRECT_CATALOG' })).toMatch(/directa|direct/i);
    expect(component.referenceLabel({ ...item, referenceKind: 'MANUAL' })).toMatch(/manual/i);
    expect(component.referenceLabel({ ...item, editorialReferenceSource: 'MANUAL' })).toMatch(/manual/i);
  });
});
