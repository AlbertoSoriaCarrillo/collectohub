import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { ReservationResponse } from '../../../core/models/reservation.model';
import { ReservationService } from '../../../core/services/reservation.service';
import { MyReservationsComponent } from './my-reservations.component';

describe('MyReservationsComponent', () => {
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

  let reservationService: {
    getMyReservations: ReturnType<typeof vi.fn>;
    cancelMyReservation: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    reservationService = {
      getMyReservations: vi.fn(() => of([])),
      cancelMyReservation: vi.fn(() => of({ ...reservation, status: 'CANCELLED' }))
    };

    await TestBed.configureTestingModule({
      imports: [MyReservationsComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ReservationService,
          useValue: reservationService
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

  it('renders empty state', async () => {
    const fixture = TestBed.createComponent(MyReservationsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toMatch(
      /Aun no tienes reservas\.|You do not have reservations yet\./
    );
  });

  it('renders reservations', async () => {
    reservationService.getMyReservations.mockReturnValue(of([reservation]));

    const fixture = TestBed.createComponent(MyReservationsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('Akihabara Store');
    expect(compiled.textContent).toMatch(/Pendiente|Pending/);
    expect(compiled.textContent).toMatch(/Cancelar|Cancel/);
  });
});
