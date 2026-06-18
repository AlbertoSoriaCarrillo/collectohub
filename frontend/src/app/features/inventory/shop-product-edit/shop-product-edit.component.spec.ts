import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopProductEditComponent } from './shop-product-edit.component';

describe('ShopProductEditComponent', () => {
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
    await TestBed.configureTestingModule({
      imports: [ShopProductEditComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
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
          useValue: {
            getMyShopProducts: vi.fn(() => of([product])),
            updateShopProduct: vi.fn()
          }
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
});
