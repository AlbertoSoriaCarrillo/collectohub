import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ShopResponse } from '../models/shop.model';
import { ShopService } from './shop.service';

describe('ShopService', () => {
  let service: ShopService;
  let httpTestingController: HttpTestingController;

  const shop: ShopResponse = {
    id: 12,
    name: 'Akihabara Store',
    description: 'Figuras y manga',
    contactEmail: 'shop@example.com',
    contactPhone: null,
    country: 'ES',
    currency: 'EUR',
    defaultReservationExpirationHours: 48,
    logoUrl: null,
    status: 'ACTIVE',
    currentUserMembership: {
      id: 2,
      userId: 7,
      role: 'OWNER',
      status: 'ACTIVE'
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ShopService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('creates a shop through the backend API', () => {
    service.createShop({ name: 'Akihabara Store', currency: 'EUR' }).subscribe((response) => {
      expect(response.id).toBe(12);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/shops');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ name: 'Akihabara Store', currency: 'EUR' });
    request.flush(shop);
  });

  it('loads shops associated with the authenticated user', () => {
    service.getMyShops().subscribe((response) => {
      expect(response).toEqual([shop]);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/shops/my');
    expect(request.request.method).toBe('GET');
    request.flush([shop]);
  });
});
