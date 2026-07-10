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

  it('searches, creates and updates items without empty params', () => {
    service.searchItems(4, {
      q: 'chapter',
      publicationYear: 1988,
      language: '',
      country: 'JP',
      recordStatus: 'ACTIVE'
    }).subscribe();

    const search = httpTestingController.expectOne((candidate) =>
      candidate.url === 'http://localhost:8080/api/catalog/series/4/items' &&
      candidate.params.get('q') === 'chapter' &&
      candidate.params.get('publicationYear') === '1988' &&
      candidate.params.get('country') === 'JP' &&
      candidate.params.get('recordStatus') === 'ACTIVE' &&
      candidate.params.get('sort') === 'sortOrder,asc' &&
      !candidate.params.has('language')
    );
    expect(search.request.method).toBe('GET');
    search.flush(emptyPage());

    const request = {
      title: 'Chapter 1',
      originalTitle: null,
      sequenceLabel: '1',
      sortOrder: 1,
      description: null,
      firstPublicationDate: null,
      firstPublicationYear: 1988,
      originalLanguage: 'ja',
      originCountry: 'JP',
      recordStatus: 'DRAFT' as const
    };

    service.createItem(4, request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/series/4/items').request.method).toBe('POST');

    service.updateItem(10, request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/items/10').request.method).toBe('PUT');
  });

  it('searches, creates and updates editions without empty params', () => {
    service.searchEditions(5, {
      publisherId: 2,
      isbn: ' 123 ',
      ean: '',
      format: 'PAPERBACK',
      language: 'es',
      country: 'ES',
      publicationYear: 1999,
      recordStatus: 'ACTIVE'
    }).subscribe();

    const search = httpTestingController.expectOne((candidate) =>
      candidate.url === 'http://localhost:8080/api/catalog/items/5/editions' &&
      candidate.params.get('publisherId') === '2' &&
      candidate.params.get('isbn') === '123' &&
      candidate.params.get('format') === 'PAPERBACK' &&
      candidate.params.get('language') === 'es' &&
      candidate.params.get('country') === 'ES' &&
      candidate.params.get('publicationYear') === '1999' &&
      candidate.params.get('recordStatus') === 'ACTIVE' &&
      candidate.params.get('sort') === 'publicationYear,asc' &&
      !candidate.params.has('ean')
    );
    expect(search.request.method).toBe('GET');
    search.flush(emptyPage());

    const request = {
      publisherId: 2,
      isbn: '123',
      ean: null,
      format: 'PAPERBACK' as const,
      editionName: 'First',
      publicationDate: null,
      publicationYear: 1999,
      language: 'es',
      country: 'ES',
      pageCount: 200,
      coverImageUrl: 'https://example.test/cover.jpg',
      recordStatus: 'ACTIVE' as const
    };

    service.createEdition(5, request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/items/5/editions').request.method).toBe('POST');

    service.updateEdition(11, request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/editions/11').request.method).toBe('PUT');
  });

  it('searches, creates, updates and deletes creators without empty params', () => {
    service.searchCreators({ q: '  otomo  ', recordStatus: 'ACTIVE', sort: '' }).subscribe();

    const search = httpTestingController.expectOne((candidate) =>
      candidate.url === 'http://localhost:8080/api/catalog/creators' &&
      candidate.params.get('q') === 'otomo' &&
      candidate.params.get('recordStatus') === 'ACTIVE' &&
      candidate.params.get('page') === '0' &&
      candidate.params.get('size') === '20' &&
      !candidate.params.has('sort')
    );
    expect(search.request.method).toBe('GET');
    search.flush(emptyPage());

    const request = {
      name: 'Katsuhiro Otomo',
      slug: 'katsuhiro-otomo',
      sortName: 'Otomo, Katsuhiro',
      biography: null,
      country: 'JP',
      birthYear: 1954,
      deathYear: null,
      recordStatus: 'ACTIVE' as const
    };

    service.createCreator(request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/creators').request.method).toBe('POST');

    service.updateCreator(12, request).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/creators/12').request.method).toBe('PUT');

    service.deleteCreator(12).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/creators/12').request.method).toBe('DELETE');
  });

  it('lists, creates, updates and deletes item creator credits', () => {
    service.listItemCreatorCredits(10).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/items/10/creators').request.method).toBe('GET');

    service.createItemCreatorCredit(10, {
      creatorId: 12,
      creditRole: 'AUTHOR',
      creditOrder: 1,
      creditLabel: null
    }).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/items/10/creators').request.method).toBe('POST');

    service.updateItemCreatorCredit(10, 30, {
      creditRole: 'ARTIST',
      creditOrder: 2,
      creditLabel: 'Pencils'
    }).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/items/10/creators/30').request.method).toBe('PUT');

    service.deleteItemCreatorCredit(10, 30).subscribe();
    expect(httpTestingController.expectOne('http://localhost:8080/api/catalog/items/10/creators/30').request.method).toBe('DELETE');
  });
});

function emptyPage() {
  return { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true };
}
