import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ShopProductResponse } from '../models/inventory.model';
import { InventoryService } from './inventory.service';

describe('InventoryService', () => {
  let service: InventoryService;
  let httpTestingController: HttpTestingController;

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

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(InventoryService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('creates a legacy shop product', () => {
    const payload = {
      masterProductId: 5,
      priceAmount: 12.95,
      currency: 'EUR',
      stockQuantity: 3,
      physicalCondition: 'NEW' as const
    };

    service.createShopProduct(9, payload).subscribe((response) => {
      expect(response).toEqual(product);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/shops/9/products');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(product);
  });

  it('creates an editorial shop product', () => {
    const payload = {
      masterProductId: null,
      catalogItemId: 31,
      catalogItemEditionId: 41,
      priceAmount: 15,
      stockQuantity: 1,
      physicalCondition: 'NEW' as const
    };
    service.createShopProduct(9, payload).subscribe();
    const request = httpTestingController.expectOne('http://localhost:8080/api/shops/9/products');
    expect(request.request.body).toEqual(payload);
    request.flush(product);
  });

  it('updates a legacy shop product', () => {
    const payload = {
      masterProductId: 5,
      catalogItemId: null,
      catalogItemEditionId: null,
      priceAmount: 10,
      currency: 'EUR',
      stockQuantity: 1,
      commercialStatus: 'AVAILABLE' as const
    };

    service.updateShopProduct(9, 11, payload).subscribe((response) => {
      expect(response.id).toBe(11);
    });

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/shops/9/products/11'
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush(product);
  });

  it('updates an editorial shop product', () => {
    const payload = {
      masterProductId: null,
      catalogItemId: 31,
      catalogItemEditionId: 41
    };
    service.updateShopProduct(9, 11, payload).subscribe();
    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/shops/9/products/11'
    );
    expect(request.request.body).toEqual(payload);
    request.flush(product);
  });

  it('loads internal shop inventory', () => {
    service.getMyShopProducts(9).subscribe((response) => {
      expect(response).toEqual([product]);
    });

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/shops/9/products/my'
    );
    expect(request.request.method).toBe('GET');
    request.flush([product]);
  });

  it('loads a public shop product', () => {
    const { notes: _internalNotes, ...publicProduct } = product;
    service.getPublicShopProduct(11).subscribe((response) => {
      expect(response).toEqual(publicProduct);
      expect(response).not.toHaveProperty('notes');
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/shop-products/11');
    expect(request.request.method).toBe('GET');
    request.flush(publicProduct);
  });
});
