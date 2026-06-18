import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CollectionItemResponse,
  CollectionResponse,
  CollectionSearchFilters,
  CreateCollectionItemRequest,
  CreateCollectionRequest,
  UpdateCollectionItemRequest,
  UpdateCollectionRequest
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

  getCollectionItems(collectionId: number): Observable<CollectionItemResponse[]> {
    return this.http.get<CollectionItemResponse[]>(
      `${this.apiBaseUrl}/api/collections/${collectionId}/items`
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
}
