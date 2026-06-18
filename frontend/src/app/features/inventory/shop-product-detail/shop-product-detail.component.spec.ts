import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopService } from '../../../core/services/shop.service';
import { ShopProductDetailComponent } from './shop-product-detail.component';

describe('ShopProductDetailComponent', () => {
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
      imports: [ShopProductDetailComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ shopProductId: '11' })
            }
          }
        },
        {
          provide: InventoryService,
          useValue: {
            getPublicShopProduct: vi.fn(() => of(product))
          }
        },
        {
          provide: ShopService,
          useValue: {
            getPublicShop: vi.fn(() =>
              of({
                id: 9,
                name: 'Akihabara Store',
                description: null,
                contactEmail: null,
                contactPhone: null,
                country: 'ES',
                currency: 'EUR',
                defaultReservationExpirationHours: 48,
                logoUrl: null,
                status: 'ACTIVE',
                currentUserMembership: null
              })
            )
          }
        }
      ]
    }).compileComponents();
  });

  it('renders public shop product data', async () => {
    const fixture = TestBed.createComponent(ShopProductDetailComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('Akihabara Store');
    expect(compiled.textContent).toContain('12.95 EUR');
  });
});
