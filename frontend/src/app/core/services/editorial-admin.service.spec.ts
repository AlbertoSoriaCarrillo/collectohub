import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EditorialAdminService } from './editorial-admin.service';

describe('EditorialAdminService', () => {
  let service: EditorialAdminService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EditorialAdminService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('searches publishers without empty params and with defaults', () => {
    service.searchPublishers({ q: '  panini  ', recordStatus: 'ACTIVE', sort: '' }).subscribe();

    const request = httpTestingController.expectOne((candidate) => {
      return (
        candidate.url === 'http://localhost:8080/api/catalog/publishers' &&
        candidate.params.get('q') === 'panini' &&
        candidate.params.get('recordStatus') === 'ACTIVE' &&
        candidate.params.get('page') === '0' &&
        candidate.params.get('size') === '20' &&
        !candidate.params.has('sort')
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush(emptyPage());
  });

  it('creates and updates publishers', () => {
    service.createPublisher({ name: 'Planeta', country: 'ES', recordStatus: 'DRAFT' }).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/publishers').request.method).toBe('POST');

    service.updatePublisher(7, { name: 'Planeta', country: null, recordStatus: 'ACTIVE' }).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/publishers/7').request.method).toBe('PUT');
  });

  it('searches, creates and updates franchises', () => {
    service.searchFranchises({ q: 'akira' }).subscribe();
    const search = httpTestingController.expectOne((candidate) =>
      candidate.url === 'http://localhost:8080/api/catalog/franchises' &&
      candidate.params.get('sort') === 'name,asc'
    );
    expect(search.request.method).toBe('GET');
    search.flush(emptyPage());

    service.createFranchise({ name: 'Akira', slug: 'akira', description: null, recordStatus: 'ACTIVE' }).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/franchises').request.method).toBe('POST');

    service.updateFranchise(8, { name: 'Akira', slug: 'akira', description: 'Neo Tokyo', recordStatus: 'ARCHIVED' }).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/franchises/8').request.method).toBe('PUT');
  });

  it('searches series with supported filters and creates and updates them', () => {
    service.searchSeries({
      q: 'nausicaa',
      recordStatus: 'ACTIVE',
      franchiseId: 2,
      type: 'MANGA',
      publicationStatus: 'COMPLETED',
      publisherId: 3,
      language: '',
      country: 'JP'
    }).subscribe();

    const search = httpTestingController.expectOne((candidate) =>
      candidate.url === 'http://localhost:8080/api/catalog/series' &&
      candidate.params.get('q') === 'nausicaa' &&
      candidate.params.get('franchiseId') === '2' &&
      candidate.params.get('type') === 'MANGA' &&
      candidate.params.get('publicationStatus') === 'COMPLETED' &&
      candidate.params.get('publisherId') === '3' &&
      candidate.params.get('country') === 'JP' &&
      candidate.params.get('sort') === 'title,asc' &&
      !candidate.params.has('language')
    );
    expect(search.request.method).toBe('GET');
    search.flush(emptyPage());

    const request = {
      franchiseId: null,
      primaryPublisherId: null,
      title: 'Nausicaa',
      originalTitle: null,
      type: 'MANGA' as const,
      publicationStatus: 'COMPLETED' as const,
      description: null,
      originCountry: 'JP',
      originalLanguage: 'ja',
      startYear: 1982,
      endYear: 1994,
      recordStatus: 'ACTIVE' as const
    };

    service.createSeries(request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/series').request.method).toBe('POST');

    service.updateSeries(9, request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/series/9').request.method).toBe('PUT');
  });
});

function emptyPage() {
  return { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true };
}
