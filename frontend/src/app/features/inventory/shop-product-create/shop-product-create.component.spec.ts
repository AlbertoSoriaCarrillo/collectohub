import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopProductCreateComponent } from './shop-product-create.component';

describe('ShopProductCreateComponent', () => {
  let inventoryService: { createShopProduct: ReturnType<typeof vi.fn> };
  const category: ProductCategoryResponse = {
    id: 1,
    code: 'MANGA_COMIC',
    name: 'Manga / Comic',
    parentId: null
  };

  beforeEach(async () => {
    inventoryService = { createShopProduct: vi.fn(() => of({})) };
    await TestBed.configureTestingModule({
      imports: [ShopProductCreateComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: Router, useValue: { navigate: vi.fn(() => Promise.resolve(true)) } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ shopId: '9' })
            }
          }
        },
        {
          provide: CatalogService,
          useValue: {
            searchMasterProducts: vi.fn(() =>
              of([
                {
                  id: 5,
                  name: 'One Piece 1',
                  description: null,
                  category,
                  franchise: 'One Piece',
                  collectionName: 'One Piece',
                  volumeNumber: '1',
                  publisher: null,
                  isbn: null,
                  ean: null,
                  releaseDate: null,
                  editionStartDate: null,
                  editionEndDate: null,
                  language: 'es',
                  limitedEdition: false,
                  limitedEditionTotalUnits: null,
                  publicationCountries: [],
                  coverImageUrl: null,
                  status: 'ACTIVE',
                  attributes: {}
                }
              ])
            )
          }
        },
        {
          provide: InventoryService,
          useValue: inventoryService
        },
        {
          provide: EditorialCatalogService,
          useValue: {
            search: vi.fn(() => of({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true }))
          }
        }
      ]
    }).compileComponents();
  });

  function fillCommercialFields(component: ShopProductCreateComponent): void {
    component.form.patchValue({
      priceAmount: 12,
      stockQuantity: 1,
      physicalCondition: 'NEW'
    });
  }

  it('validates required product, price, stock and condition fields', () => {
    const fixture = TestBed.createComponent(ShopProductCreateComponent);
    const component = fixture.componentInstance;

    component.form.patchValue({
      masterProductId: null,
      priceAmount: null,
      stockQuantity: null,
      physicalCondition: ''
    });
    component.submit();

    expect(component.errorMessage()).toBeTruthy();
    expect(component.form.controls.priceAmount.hasError('required')).toBe(true);
    expect(component.form.controls.stockQuantity.hasError('required')).toBe(true);
    expect(component.form.controls.physicalCondition.hasError('required')).toBe(true);
  });

  it('keeps the legacy creation flow', () => {
    const fixture = TestBed.createComponent(ShopProductCreateComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    fillCommercialFields(component);
    component.form.patchValue({ referenceMode: 'LEGACY', masterProductId: 5 });

    component.submit();

    expect(inventoryService.createShopProduct).toHaveBeenCalledWith(
      9,
      expect.objectContaining({ masterProductId: 5, catalogItemId: null, catalogItemEditionId: null })
    );
  });

  it.each([
    ['ITEM', null],
    ['EDITION', 41]
  ] as const)('creates inventory from an editorial %s', (resultType, editionId) => {
    const fixture = TestBed.createComponent(ShopProductCreateComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    fillCommercialFields(component);
    component.form.patchValue({ referenceMode: 'EDITORIAL' });
    component.selectEditorial({
      resultType,
      seriesId: 2,
      seriesTitle: 'One Piece',
      itemId: 31,
      itemTitle: 'One Piece 1',
      editionId,
      editionName: editionId ? 'Spanish edition' : null,
      publisherName: null,
      franchiseName: 'One Piece',
      type: 'MANGA',
      language: 'es',
      country: 'ES',
      publicationYear: 2025,
      coverImageUrl: null,
      linkedMasterProductId: null,
      linkedMasterProductName: null
    });

    component.submit();

    expect(inventoryService.createShopProduct).toHaveBeenCalledWith(
      9,
      expect.objectContaining({ masterProductId: null, catalogItemId: 31, catalogItemEditionId: editionId })
    );
  });

  it('rejects a series as inventory reference', () => {
    const component = TestBed.createComponent(ShopProductCreateComponent).componentInstance;
    component.selectEditorial({
      resultType: 'SERIES', seriesId: 2, seriesTitle: 'One Piece', itemId: null,
      itemTitle: null, editionId: null, editionName: null, publisherName: null,
      franchiseName: null, type: 'MANGA', language: null, country: null,
      publicationYear: null, coverImageUrl: null, linkedMasterProductId: null,
      linkedMasterProductName: null
    });
    expect(component.selectedEditorial()).toBeNull();
    expect(component.errorMessage()).toBeTruthy();
  });
});
