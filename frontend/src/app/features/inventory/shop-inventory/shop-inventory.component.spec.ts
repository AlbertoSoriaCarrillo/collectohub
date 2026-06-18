import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { ShopResponse } from '../../../core/models/shop.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopService } from '../../../core/services/shop.service';
import { ShopInventoryComponent } from './shop-inventory.component';

describe('ShopInventoryComponent', () => {
  const shop: ShopResponse = {
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

  async function configure(products: ShopProductResponse[]): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ShopInventoryComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ shopId: '9' })
            }
          }
        },
        {
          provide: ShopService,
          useValue: {
            getPublicShop: vi.fn(() => of(shop))
          }
        },
        {
          provide: InventoryService,
          useValue: {
            getMyShopProducts: vi.fn(() => of(products))
          }
        }
      ]
    }).compileComponents();
  }

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('renders an empty state when the shop has no inventory', async () => {
    await configure([]);
    const fixture = TestBed.createComponent(ShopInventoryComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toMatch(/aun no tiene inventario|does not have inventory yet/);
  });

  it('renders internal inventory products', async () => {
    await configure([product]);
    const fixture = TestBed.createComponent(ShopInventoryComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('12.95 EUR');
  });
});
