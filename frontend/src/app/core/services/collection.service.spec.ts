import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CollectionItemResponse, CollectionResponse } from '../models/collection.model';
import { CollectionService } from './collection.service';

describe('CollectionService', () => {
  let service: CollectionService;
  let httpTestingController: HttpTestingController;

  const item: CollectionItemResponse = {
    id: 7,
    collectionId: 3,
    masterProductId: 5,
    masterProductName: 'One Piece 1',
    masterProductCategoryCode: 'MANGA_COMIC',
    masterProductFranchise: 'One Piece',
    masterProductCollectionName: 'One Piece',
    masterProductVolumeNumber: '1',
    manualTitle: null,
    manualDescription: null,
    manualType: null,
    collectionStatus: 'MISSING',
    physicalCondition: 'NEW',
    unitNumber: null,
    totalLimitedUnits: null,
    notes: 'Missing item',
    acquiredAt: null
  };

  const collection: CollectionResponse = {
    id: 3,
    userId: 2,
    name: 'Manga pendientes',
    description: 'Lista personal',
    visibility: 'PRIVATE',
    categoryCode: 'MANGA_COMIC',
    categoryName: 'Manga / Comic',
    items: [item]
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CollectionService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('creates a collection', () => {
    const payload = { name: 'Manga pendientes', visibility: 'PRIVATE' as const };

    service.createCollection(payload).subscribe((response) => {
      expect(response).toEqual(collection);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/collections');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(collection);
  });

  it('loads my collections with filters', () => {
    service
      .getMyCollections({ visibility: 'PRIVATE', categoryCode: 'MANGA_COMIC' })
      .subscribe((response) => {
        expect(response).toEqual([collection]);
      });

    const request = httpTestingController.expectOne((candidate) => {
      return (
        candidate.url === 'http://localhost:8080/api/collections/my' &&
        candidate.params.get('visibility') === 'PRIVATE' &&
        candidate.params.get('categoryCode') === 'MANGA_COMIC'
      );
    });
    expect(request.request.method).toBe('GET');
    request.flush([collection]);
  });

  it('loads a collection detail', () => {
    service.getCollection(3).subscribe((response) => {
      expect(response).toEqual(collection);
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/collections/3');
    expect(request.request.method).toBe('GET');
    request.flush(collection);
  });

  it('updates a collection with the exact PUT URL and body', () => {
    const payload = { name: 'Updated', description: '', visibility: 'PUBLIC' as const, categoryCode: '' };

    service.updateCollection(3, payload).subscribe();

    const request = httpTestingController.expectOne('http://localhost:8080/api/collections/3');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush({ ...collection, ...payload });
  });

  it('deletes a collection with the exact DELETE URL', () => {
    service.deleteCollection(3).subscribe();

    const request = httpTestingController.expectOne('http://localhost:8080/api/collections/3');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('adds a legacy collection item without editorial residual fields', () => {
    const payload = {
      masterProductId: 5,
      catalogItemId: null,
      catalogItemEditionId: null,
      collectionStatus: 'MISSING' as const,
      physicalCondition: 'NEW' as const
    };

    service.addCollectionItem(3, payload).subscribe((response) => {
      expect(response).toEqual(item);
    });

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/collections/3/items'
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(item);
  });

  it('adds a manual collection item with the exact POST body', () => {
    const payload = {
      masterProductId: null, catalogItemId: null, catalogItemEditionId: null,
      manualTitle: 'Edición promocional', manualDescription: 'Entregada durante un evento', manualType: 'Libro',
      collectionStatus: 'OWNED' as const, physicalCondition: 'GOOD' as const,
      unitNumber: '1', totalLimitedUnits: 100, notes: 'Mi ejemplar', acquiredAt: '2026-07-13'
    };
    const manualItem = { ...item, ...payload, masterProductName: null, manualTitle: payload.manualTitle,
      manualDescription: payload.manualDescription, manualType: payload.manualType, referenceKind: 'MANUAL' as const,
      editorialReferenceSource: 'MANUAL' as const };
    service.addCollectionItem(3, payload).subscribe((response) => expect(response).toEqual(manualItem));
    const request = httpTestingController.expectOne('http://localhost:8080/api/collections/3/items');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(manualItem);
  });

  it('adds an editorial collection item without an edition', () => {
    const payload = {
      masterProductId: null,
      catalogItemId: 11,
      catalogItemEditionId: null,
      collectionStatus: 'OWNED' as const
    };

    service.addCollectionItem(3, payload).subscribe();

    const request = httpTestingController.expectOne('http://localhost:8080/api/collections/3/items');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ ...item, masterProductId: null, catalogItemId: 11 });
  });

  it('adds a collection item with an editorial item and edition', () => {
    const payload = {
      masterProductId: null,
      catalogItemId: 11,
      catalogItemEditionId: 12,
      collectionStatus: 'OWNED' as const
    };

    service.addCollectionItem(3, payload).subscribe();

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/collections/3/items'
    );
    expect(request.request.body).toEqual(payload);
    request.flush({ ...item, masterProductId: null, catalogItemId: 11, catalogItemEditionId: 12 });
  });

  it('updates a collection item', () => {
    const payload = { collectionStatus: 'OWNED' as const };

    service.updateCollectionItem(3, 7, payload).subscribe((response) => {
      expect(response.id).toBe(7);
    });

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/collections/3/items/7'
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush({ ...item, collectionStatus: 'OWNED' });
  });

  it('links a manual collection item to a catalog item without an edition', () => {
    const payload = { catalogItemId: 11 };

    service.linkManualCollectionItemToCatalog(3, 7, payload).subscribe();

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/collections/3/items/7/catalog-reference'
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush({ ...item, catalogItemId: 11 });
  });

  it('links a manual collection item to a catalog item and edition', () => {
    const payload = { catalogItemId: 11, catalogItemEditionId: 12 };

    service.linkManualCollectionItemToCatalog(3, 7, payload).subscribe();

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/collections/3/items/7/catalog-reference'
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush({ ...item, catalogItemId: 11, catalogItemEditionId: 12 });
  });

  it('deletes a collection item', () => {
    service.deleteCollectionItem(3, 7).subscribe((response) => {
      expect(response).toBeNull();
    });

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/collections/3/items/7'
    );
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
