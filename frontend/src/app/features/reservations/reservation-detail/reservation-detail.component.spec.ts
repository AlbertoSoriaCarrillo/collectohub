import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { ReservationResponse } from '../../../core/models/reservation.model';
import { ReservationService } from '../../../core/services/reservation.service';
import { ReservationDetailComponent } from './reservation-detail.component';

describe('ReservationDetailComponent', () => {
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
      imports: [ReservationDetailComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ reservationId: '21' })
            }
          }
        },
        {
          provide: ReservationService,
          useValue: {
            getReservation: vi.fn(() => of(reservation)),
            cancelMyReservation: vi.fn(() => of({ ...reservation, status: 'CANCELLED' }))
          }
        },
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({
              id: 7,
              email: 'ada@example.com',
              displayName: 'Ada',
              preferredInterfaceLanguage: 'es',
              roles: ['USER']
            }),
            hasToken: vi.fn(() => true),
            getMe: vi.fn()
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

  it('renders reservation details', async () => {
    const fixture = TestBed.createComponent(ReservationDetailComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('Akihabara Store');
    expect(compiled.textContent).toContain('Please reserve it.');
    expect(compiled.textContent).toContain('Cancelar reserva');
  });
});
