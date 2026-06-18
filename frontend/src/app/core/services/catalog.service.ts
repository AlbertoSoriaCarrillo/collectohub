import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateMasterProductRequest,
  MasterProductResponse,
  MasterProductSearchFilters,
  ProductCategoryResponse,
  UpdateMasterProductRequest
} from '../models/catalog.model';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  getCategories(): Observable<ProductCategoryResponse[]> {
    return this.http.get<ProductCategoryResponse[]>(`${this.apiBaseUrl}/api/product-categories`);
  }

  searchMasterProducts(filters: MasterProductSearchFilters = {}): Observable<MasterProductResponse[]> {
    return this.http.get<MasterProductResponse[]>(`${this.apiBaseUrl}/api/master-products`, {
      params: this.toParams(filters)
    });
  }

  getMasterProduct(id: number): Observable<MasterProductResponse> {
    return this.http.get<MasterProductResponse>(`${this.apiBaseUrl}/api/master-products/${id}`);
  }

  createMasterProduct(request: CreateMasterProductRequest): Observable<MasterProductResponse> {
    return this.http.post<MasterProductResponse>(`${this.apiBaseUrl}/api/master-products`, request);
  }

  updateMasterProduct(
    id: number,
    request: UpdateMasterProductRequest
  ): Observable<MasterProductResponse> {
    return this.http.put<MasterProductResponse>(
      `${this.apiBaseUrl}/api/master-products/${id}`,
      request
    );
  }

  private toParams(filters: MasterProductSearchFilters): HttpParams {
    return Object.entries(filters).reduce((params, [key, value]) => {
      const normalizedValue = value?.trim();
      return normalizedValue ? params.set(key, normalizedValue) : params;
    }, new HttpParams());
  }
}
