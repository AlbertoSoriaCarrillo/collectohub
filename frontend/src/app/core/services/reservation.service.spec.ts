import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ReservationResponse } from '../models/reservation.model';
import { ReservationService } from './reservation.service';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpTestingController: HttpTestingController;

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

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ReservationService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('creates a reservation', () => {
    const payload = { shopProductId: 11, quantity: 1, userMessage: 'Please reserve it.' };

    service.createReservation(payload).subscribe((response) => {
      expect(response).toEqual(reservation);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/reservations');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(reservation);
  });

  it('loads my reservations with filters', () => {
    service.getMyReservations({ status: 'PENDING', shopId: 9 }).subscribe((response) => {
      expect(response).toEqual([reservation]);
    });

    const request = httpTestingController.expectOne((candidate) => {
      return (
        candidate.url === 'http://localhost:8080/api/reservations/my' &&
        candidate.params.get('status') === 'PENDING' &&
        candidate.params.get('shopId') === '9'
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush([reservation]);
  });

  it('loads a reservation detail', () => {
    service.getReservation(21).subscribe((response) => {
      expect(response.id).toBe(21);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/reservations/21');
    expect(request.request.method).toBe('GET');
    request.flush(reservation);
  });

  it('loads shop reservations with filters', () => {
    service
      .getShopReservations(9, { status: 'PENDING', userId: 7, shopProductId: 11 })
      .subscribe((response) => {
        expect(response).toEqual([reservation]);
      });

    const request = httpTestingController.expectOne((candidate) => {
      return (
        candidate.url === 'http://localhost:8080/api/shops/9/reservations' &&
        candidate.params.get('status') === 'PENDING' &&
        candidate.params.get('userId') === '7' &&
        candidate.params.get('shopProductId') === '11'
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush([reservation]);
  });

  it('updates shop reservation status', () => {
    const payload = { status: 'ACCEPTED' as const, shopResponse: 'Accepted.' };

    service.updateShopReservationStatus(9, 21, payload).subscribe((response) => {
      expect(response.status).toBe('PENDING');
    });

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/shops/9/reservations/21/status'
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush(reservation);
  });

  it('cancels my reservation', () => {
    service.cancelMyReservation(21).subscribe((response) => {
      expect(response.id).toBe(21);
    });

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/reservations/21/cancel'
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({});
    request.flush(reservation);
  });
});
