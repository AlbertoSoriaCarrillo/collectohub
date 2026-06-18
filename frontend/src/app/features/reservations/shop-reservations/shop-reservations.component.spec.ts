import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { ReservationResponse } from '../../../core/models/reservation.model';
import { ReservationService } from '../../../core/services/reservation.service';
import { ShopReservationsComponent } from './shop-reservations.component';

describe('ShopReservationsComponent', () => {
  const reservation: ReservationResponse = {
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
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShopReservationsComponent],
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
          provide: ReservationService,
          useValue: {
            getShopReservations: vi.fn(() => of([reservation])),
            updateShopReservationStatus: vi.fn(() => of({ ...reservation, status: 'ACCEPTED' }))
          }
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

  it('renders shop reservations', async () => {
    const fixture = TestBed.createComponent(ShopReservationsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('Ada Collectora');
    expect(compiled.textContent).toMatch(/Aceptar|Accept/);
    expect(compiled.textContent).toMatch(/Rechazar|Reject/);
  });

  it('only exposes actions for valid statuses', () => {
    const fixture = TestBed.createComponent(ShopReservationsComponent);
    const component = fixture.componentInstance;

    expect(component.availableActions(reservation).map((action) => action.status)).toEqual([
      'ACCEPTED',
      'REJECTED'
    ]);
    expect(component.availableActions({ ...reservation, status: 'COMPLETED' })).toEqual([]);
  });
});
