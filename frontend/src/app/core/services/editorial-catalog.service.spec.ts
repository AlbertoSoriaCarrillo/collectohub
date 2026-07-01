import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EditorialCatalogService } from './editorial-catalog.service';

describe('EditorialCatalogService', () => {
  let service: EditorialCatalogService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EditorialCatalogService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('builds editorial search query params without empty values', () => {
    service
      .search({
        q: '  trigun  ',
        type: 'MANGA',
        resultType: 'EDITION',
        language: '',
        publicationYear: 2004,
        page: 1,
        size: 12,
        sort: 'title,asc'
      })
      .subscribe();

    const request = httpTestingController.expectOne((candidate) => {
      return (
        candidate.url === 'http://localhost:8080/api/catalog/editorial/search' &&
        candidate.params.get('q') === 'trigun' &&
        candidate.params.get('type') === 'MANGA' &&
        candidate.params.get('resultType') === 'EDITION' &&
        candidate.params.get('publicationYear') === '2004' &&
        candidate.params.get('page') === '1' &&
        candidate.params.get('size') === '12' &&
        !candidate.params.has('language')
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush({ content: [], page: 1, size: 12, totalElements: 0, totalPages: 0, first: false, last: true });
  });

  it('loads public details and keeps the admin bridge method out of UI concerns', () => {
    service.getSeriesDetail(3).subscribe();
    httpTestingController
      .expectOne('http://localhost:8080/api/catalog/editorial/series/3/detail')
      .flush({});

    service.getItemDetail(4).subscribe();
    httpTestingController
      .expectOne('http://localhost:8080/api/catalog/editorial/items/4/detail')
      .flush({});

    service.getEditionDetail(5).subscribe();
    httpTestingController
      .expectOne('http://localhost:8080/api/catalog/editorial/editions/5/detail')
      .flush({});

    service.getMasterProductLink(6).subscribe();
    httpTestingController
      .expectOne('http://localhost:8080/api/catalog/editorial/master-products/6/link')
      .flush({});
  });
});
