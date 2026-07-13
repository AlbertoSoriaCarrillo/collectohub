import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { CollectionItemResponse } from '../../../core/models/collection.model';
import { EditorialCatalogSearchItem } from '../../../core/models/editorial-catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { CollectionItemEditComponent } from './collection-item-edit.component';

describe('CollectionItemEditComponent', () => {
  const legacyItem: CollectionItemResponse = {
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
  const editorialResult: EditorialCatalogSearchItem = {
    resultType: 'EDITION',
    seriesId: 10,
    seriesTitle: 'One Piece',
    itemId: 20,
    itemTitle: 'One Piece 1',
    editionId: 30,
    editionName: 'Paperback',
    publisherName: 'Publisher',
    franchiseName: 'One Piece',
    type: 'MANGA',
    language: 'es',
    country: 'ES',
    publicationYear: 2024,
    coverImageUrl: null,
    linkedMasterProductId: null,
    linkedMasterProductName: null
  };
  const seriesResult: EditorialCatalogSearchItem = {
    ...editorialResult,
    resultType: 'SERIES',
    itemId: null,
    itemTitle: null,
    editionId: null,
    editionName: null
  };

  let catalogService: { searchMasterProducts: ReturnType<typeof vi.fn> };
  let collectionService: {
    getCollectionItems: ReturnType<typeof vi.fn>;
    updateCollectionItem: ReturnType<typeof vi.fn>;
  };
  let editorialCatalogService: { search: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    catalogService = {
      searchMasterProducts: vi.fn(() => of([{
        id: 9,
        name: 'One Piece 2',
        franchise: 'One Piece',
        category: { id: 1, code: 'MANGA_COMIC', name: 'Manga', parentId: null }
      }]))
    };
    collectionService = {
      getCollectionItems: vi.fn(() => of([legacyItem])),
      updateCollectionItem: vi.fn(() => of(legacyItem))
    };
    editorialCatalogService = {
      search: vi.fn(() => of({
        content: [seriesResult, editorialResult],
        page: 0,
        size: 30,
        totalElements: 2,
        totalPages: 1,
        first: true,
        last: true
      }))
    };

    await TestBed.configureTestingModule({
      imports: [CollectionItemEditComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ collectionId: '3', itemId: '7' })
            }
          }
        },
        { provide: CatalogService, useValue: catalogService },
        { provide: CollectionService, useValue: collectionService },
        { provide: EditorialCatalogService, useValue: editorialCatalogService }
      ]
    }).compileComponents();
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  it('loads a legacy item and preloads masterProductId', async () => {
    const component = await createComponent();

    expect(component.form.controls.referenceMode.value).toBe('LEGACY');
    expect(component.form.controls.masterProductId.value).toBe(5);
    expect(component.form.controls.collectionStatus.value).toBe('MISSING');
  });

  it('searches and selects a legacy master product', async () => {
    const component = await createComponent();
    component.productSearch.controls.name.setValue('One Piece 2');

    component.searchProducts();

    expect(catalogService.searchMasterProducts).toHaveBeenCalledWith({
      name: 'One Piece 2',
      status: 'ACTIVE'
    });
    expect(component.products()).toHaveLength(1);
    component.form.controls.masterProductId.setValue(9);
    expect(component.form.controls.masterProductId.value).toBe(9);
  });

  it('submits legacy mode with master product and cleared editorial ids', async () => {
    const component = await createComponent();
    component.form.patchValue({ referenceMode: 'LEGACY', masterProductId: 9 });

    component.submit();

    expect(collectionService.updateCollectionItem).toHaveBeenCalledWith(
      3,
      7,
      expect.objectContaining({
        masterProductId: 9,
        catalogItemId: null,
        catalogItemEditionId: null
      })
    );
  });

  it('shows and preserves an existing editorial reference', async () => {
    const editorialItem = {
      ...legacyItem,
      catalogItemId: 20,
      catalogItemTitle: 'One Piece 1',
      catalogItemEditionId: 30,
      catalogItemEditionName: 'Paperback'
    };
    collectionService.getCollectionItems.mockReturnValue(of([editorialItem]));
    const component = await createComponent();

    expect(component.form.controls.referenceMode.value).toBe('EDITORIAL');
    expect(component.item()?.catalogItemTitle).toBe('One Piece 1');

    component.submit();

    const request = collectionService.updateCollectionItem.mock.calls[0][2];
    expect(request).not.toHaveProperty('catalogItemId');
    expect(request).not.toHaveProperty('catalogItemEditionId');
  });

  it('searches editorial references and excludes complete series', async () => {
    const component = await createComponent();
    component.form.controls.referenceMode.setValue('EDITORIAL');
    component.editorialSearch.controls.q.setValue('One Piece');

    component.searchEditorial();

    expect(editorialCatalogService.search).toHaveBeenCalledWith({ q: 'One Piece', size: 30 });
    expect(component.editorialResults()).toEqual([editorialResult]);
  });

  it('submits a selected editorial item and edition', async () => {
    const component = await createComponent();
    component.form.controls.referenceMode.setValue('EDITORIAL');
    component.selectEditorial(editorialResult);

    component.submit();

    expect(collectionService.updateCollectionItem).toHaveBeenCalledWith(
      3,
      7,
      expect.objectContaining({
        masterProductId: null,
        catalogItemId: 20,
        catalogItemEditionId: 30
      })
    );
  });

  it('rejects a new editorial mode without a selection', async () => {
    collectionService.getCollectionItems.mockReturnValue(of([{
      ...legacyItem,
      masterProductId: null,
      masterProductName: null
    }]));
    const component = await createComponent();
    component.form.controls.referenceMode.setValue('EDITORIAL');

    component.submit();

    expect(component.errorMessage()).toBeTruthy();
    expect(collectionService.updateCollectionItem).not.toHaveBeenCalled();
  });

  it('does not allow selecting a complete series', async () => {
    const component = await createComponent();

    component.selectEditorial(seriesResult);

    expect(component.selectedEditorial()).toBeNull();
    expect(component.errorMessage()).toBeTruthy();
  });

  async function createComponent(): Promise<CollectionItemEditComponent> {
    const fixture = TestBed.createComponent(CollectionItemEditComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture.componentInstance;
  }
});
