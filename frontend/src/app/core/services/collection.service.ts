import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CollectionItemResponse,
  CollectionItemListFilters,
  CollectionSeriesProgressSummaryResponse,
  CollectionSeriesProgressResponse,
  CollectionResponse,
  CollectionSearchFilters,
  CreateCollectionItemRequest,
  CreateCollectionRequest,
  UpdateCollectionItemRequest,
  UpdateCollectionRequest,
  LinkManualCollectionItemRequest
} from '../models/collection.model';

@Injectable({
  providedIn: 'root'
})
export class CollectionService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  createCollection(request: CreateCollectionRequest): Observable<CollectionResponse> {
    return this.http.post<CollectionResponse>(`${this.apiBaseUrl}/api/collections`, request);
  }

  getMyCollections(filters: CollectionSearchFilters = {}): Observable<CollectionResponse[]> {
    return this.http.get<CollectionResponse[]>(`${this.apiBaseUrl}/api/collections/my`, {
      params: this.toParams(filters)
    });
  }

  getCollection(collectionId: number): Observable<CollectionResponse> {
    return this.http.get<CollectionResponse>(`${this.apiBaseUrl}/api/collections/${collectionId}`);
  }

  updateCollection(
    collectionId: number,
    request: UpdateCollectionRequest
  ): Observable<CollectionResponse> {
    return this.http.put<CollectionResponse>(
      `${this.apiBaseUrl}/api/collections/${collectionId}`,
      request
    );
  }

  deleteCollection(collectionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/collections/${collectionId}`);
  }

  addCollectionItem(
    collectionId: number,
    request: CreateCollectionItemRequest
  ): Observable<CollectionItemResponse> {
    return this.http.post<CollectionItemResponse>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/items`,
      request
    );
  }

  getCollectionItems(
    collectionId: number,
    filters: CollectionItemListFilters = {}
  ): Observable<CollectionItemResponse[]> {
    return this.http.get<CollectionItemResponse[]>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/items`,
      { params: this.toItemListParams(filters) }
    );
  }

  getCollectionSeriesProgressSummary(
    collectionId: number
  ): Observable<CollectionSeriesProgressSummaryResponse[]> {
    return this.http.get<CollectionSeriesProgressSummaryResponse[]>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/series-progress`
    );
  }

  getCollectionSeriesProgress(
    collectionId: number,
    seriesId: number
  ): Observable<CollectionSeriesProgressResponse> {
    return this.http.get<CollectionSeriesProgressResponse>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/series/${seriesId}/progress`
    );
  }

  updateCollectionItem(
    collectionId: number,
    itemId: number,
    request: UpdateCollectionItemRequest
  ): Observable<CollectionItemResponse> {
    return this.http.put<CollectionItemResponse>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/items/${itemId}`,
      request
    );
  }

  linkManualCollectionItemToCatalog(
    collectionId: number,
    itemId: number,
    request: LinkManualCollectionItemRequest
  ): Observable<CollectionItemResponse> {
    return this.http.put<CollectionItemResponse>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/items/${itemId}/catalog-reference`, request
    );
  }

  deleteCollectionItem(collectionId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/items/${itemId}`
    );
  }

  private toParams(filters: CollectionSearchFilters): HttpParams {
    return Object.entries(filters).reduce((params, [key, value]) => {
      const normalizedValue = value?.trim();
      return normalizedValue ? params.set(key, normalizedValue) : params;
    }, new HttpParams());
  }

  private toItemListParams(filters: CollectionItemListFilters): HttpParams {
    let params = new HttpParams();
    const query = filters.q?.trim();
    if (query) {
      params = params.set('q', query);
    }
    for (const status of filters.status ?? []) {
      params = params.append('status', status);
    }
    for (const referenceKind of filters.referenceKind ?? []) {
      params = params.append('referenceKind', referenceKind);
    }
    if (filters.seriesId != null) {
      params = params.set('seriesId', filters.seriesId);
    }
    if (filters.sort && filters.sort !== 'CATALOG_ORDER') {
      params = params.set('sort', filters.sort);
    }
    return params;
  }
}
