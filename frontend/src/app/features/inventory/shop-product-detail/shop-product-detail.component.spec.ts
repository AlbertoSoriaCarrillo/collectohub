import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ReservationService } from '../../../core/services/reservation.service';
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

  let reservationService: {
    createReservation: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    reservationService = {
      createReservation: vi.fn(() =>
        of({
          id: 21,
          userId: 7,
          userDisplayName: 'Ada Collectora',
          shopId: 9,
          shopName: 'Akihabara Store',
          shopProductId: 11,
          masterProductId: 5,
          productName: 'One Piece 1',
          quantity: 1,
          status: 'PENDING',
          userMessage: 'Please reserve it.',
          shopResponse: null,
          expiresAt: '2026-06-20T10:00:00Z',
          completedAt: null,
          createdAt: '2026-06-18T10:00:00Z'
        })
      )
    };

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
          provide: ReservationService,
          useValue: reservationService
        },
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: signal(true)
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

  it('creates a reservation for authenticated user', async () => {
    const fixture = TestBed.createComponent(ShopProductDetailComponent);
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    fixture.componentInstance.reservationForm.setValue({
      quantity: 2,
      userMessage: 'Please reserve it.'
    });
    fixture.componentInstance.createReservation(product);

    expect(reservationService.createReservation).toHaveBeenCalledWith({
      shopProductId: 11,
      quantity: 2,
      userMessage: 'Please reserve it.'
    });
    expect(navigateSpy).toHaveBeenCalledWith(['/reservations', 21], {
      state: {
        successMessage: expect.stringMatching(
          /Reserva creada correctamente\.|Reservation created successfully\./
        )
      }
    });
  });
});
