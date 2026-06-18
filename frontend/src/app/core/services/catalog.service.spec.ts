import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MasterProductResponse, ProductCategoryResponse } from '../models/catalog.model';
import { CatalogService } from './catalog.service';

describe('CatalogService', () => {
  let service: CatalogService;
  let httpTestingController: HttpTestingController;

  const category: ProductCategoryResponse = {
    id: 1,
    code: 'MANGA_COMIC',
    name: 'Manga / Comic',
    parentId: null
  };

  const product: MasterProductResponse = {
    id: 5,
    name: 'One Piece 1',
    description: null,
    category,
    franchise: 'One Piece',
    collectionName: 'One Piece',
    volumeNumber: '1',
    publisher: 'Planeta',
    isbn: '9780000000001',
    ean: null,
    releaseDate: null,
    editionStartDate: null,
    editionEndDate: null,
    language: 'es',
    limitedEdition: false,
    limitedEditionTotalUnits: null,
    publicationCountries: ['ES'],
    coverImageUrl: null,
    status: 'ACTIVE',
    attributes: {}
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CatalogService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('loads public product categories', () => {
    service.getCategories().subscribe((response) => {
      expect(response).toEqual([category]);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/product-categories');
    expect(request.request.method).toBe('GET');
    request.flush([category]);
  });

  it('searches master products with filters', () => {
    service
      .searchMasterProducts({
        categoryCode: 'MANGA_COMIC',
        name: 'One Piece',
        franchise: '',
        status: 'ACTIVE'
      })
      .subscribe((response) => {
        expect(response).toEqual([product]);
      });

    const request = httpTestingController.expectOne((candidate) => {
      return (
        candidate.url === 'http://localhost:8080/api/master-products' &&
        candidate.params.get('categoryCode') === 'MANGA_COMIC' &&
        candidate.params.get('name') === 'One Piece' &&
        candidate.params.get('status') === 'ACTIVE' &&
        !candidate.params.has('franchise')
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush([product]);
  });

  it('creates a master product', () => {
    const payload = {
      name: 'One Piece 1',
      categoryCode: 'MANGA_COMIC',
      isbn: '9780000000001',
      attributes: {}
    };

    service.createMasterProduct(payload).subscribe((response) => {
      expect(response.id).toBe(5);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/master-products');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(product);
  });
});
