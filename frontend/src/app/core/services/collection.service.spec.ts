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

  it('adds a collection item', () => {
    const payload = {
      masterProductId: 5,
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
