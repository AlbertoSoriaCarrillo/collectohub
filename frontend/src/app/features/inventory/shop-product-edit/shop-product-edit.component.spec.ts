import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { CatalogService } from '../../../core/services/catalog.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { ShopProductEditComponent } from './shop-product-edit.component';

describe('ShopProductEditComponent', () => {
  let inventoryService: {
    getMyShopProducts: ReturnType<typeof vi.fn>;
    updateShopProduct: ReturnType<typeof vi.fn>;
  };
  const product: ShopProductResponse = {
    id: 11,
    shopId: 9,
    masterProductId: 5,
    masterProductName: 'One Piece 1',
    masterProductCategoryCode: 'MANGA_COMIC',
    masterProductFranchise: 'One Piece',
    masterProductCollectionName: 'One Piece',
    masterProductVolumeNumber: '1',
    priceAmount: 12.95,
    currency: 'EUR',
    stockQuantity: 3,
    commercialStatus: 'AVAILABLE',
    physicalCondition: 'NEW',
    visible: true,
    unitNumber: null,
    totalLimitedUnits: null,
    notes: 'Public note'
  };

  beforeEach(async () => {
    product.masterProductId = 5;
    product.masterProductName = 'One Piece 1';
    product.catalogItemId = null;
    product.catalogItemEditionId = null;
    inventoryService = {
      getMyShopProducts: vi.fn(() => of([product])),
      updateShopProduct: vi.fn(() => of(product))
    };
    await TestBed.configureTestingModule({
      imports: [ShopProductEditComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: Router, useValue: { navigate: vi.fn(() => Promise.resolve(true)) } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ shopId: '9', shopProductId: '11' })
            }
          }
        },
        {
          provide: InventoryService,
          useValue: inventoryService
        },
        { provide: CatalogService, useValue: { searchMasterProducts: vi.fn(() => of([])) } },
        {
          provide: EditorialCatalogService,
          useValue: { search: vi.fn(() => of({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true })) }
        }
      ]
    }).compileComponents();
  });

  it('loads mock data and validates editable fields', async () => {
    const fixture = TestBed.createComponent(ShopProductEditComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.product()?.masterProductName).toBe('One Piece 1');
    expect(component.form.controls.priceAmount.value).toBe(12.95);

    component.form.patchValue({
      priceAmount: null,
      stockQuantity: null,
      physicalCondition: ''
    });
    component.submit();

    expect(component.form.controls.priceAmount.hasError('required')).toBe(true);
    expect(component.form.controls.stockQuantity.hasError('required')).toBe(true);
    expect(component.form.controls.physicalCondition.hasError('required')).toBe(true);
  });

  it('preloads legacy mode', async () => {
    const fixture = TestBed.createComponent(ShopProductEditComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.form.controls.referenceMode.value).toBe('LEGACY');
    expect(fixture.componentInstance.form.controls.masterProductId.value).toBe(5);
  });

  it('preloads and preserves the current editorial reference', async () => {
    product.masterProductId = null;
    product.masterProductName = null;
    product.catalogItemId = 31;
    product.catalogItemEditionId = 41;
    const fixture = TestBed.createComponent(ShopProductEditComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(fixture.componentInstance.form.controls.referenceMode.value).toBe('EDITORIAL');
    fixture.componentInstance.submit();

    expect(inventoryService.updateShopProduct).toHaveBeenCalledWith(
      9, 11, expect.objectContaining({ masterProductId: null, catalogItemId: 31, catalogItemEditionId: 41 })
    );
  });

  it('switches an editorial reference back to legacy', async () => {
    product.catalogItemId = 31;
    const fixture = TestBed.createComponent(ShopProductEditComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.componentInstance.form.patchValue({ referenceMode: 'LEGACY', masterProductId: 5 });

    fixture.componentInstance.submit();

    expect(inventoryService.updateShopProduct).toHaveBeenCalledWith(
      9, 11, expect.objectContaining({ masterProductId: 5, catalogItemId: null, catalogItemEditionId: null })
    );
  });
});
