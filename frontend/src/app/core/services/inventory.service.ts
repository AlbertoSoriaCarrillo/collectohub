import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateShopProductRequest,
  PublicShopProductResponse,
  ShopProductResponse,
  ShopProductSearchFilters,
  UpdateShopProductRequest
} from '../models/inventory.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  createShopProduct(
    shopId: number,
    request: CreateShopProductRequest
  ): Observable<ShopProductResponse> {
    return this.http.post<ShopProductResponse>(
      `${this.apiBaseUrl}/api/shops/${shopId}/products`,
      request
    );
  }

  updateShopProduct(
    shopId: number,
    shopProductId: number,
    request: UpdateShopProductRequest
  ): Observable<ShopProductResponse> {
    return this.http.put<ShopProductResponse>(
      `${this.apiBaseUrl}/api/shops/${shopId}/products/${shopProductId}`,
      request
    );
  }

  getMyShopProducts(shopId: number): Observable<ShopProductResponse[]> {
    return this.http.get<ShopProductResponse[]>(
      `${this.apiBaseUrl}/api/shops/${shopId}/products/my`
    );
  }

  getPublicShopProducts(
    shopId: number,
    filters: ShopProductSearchFilters = {}
  ): Observable<PublicShopProductResponse[]> {
    return this.http.get<PublicShopProductResponse[]>(
      `${this.apiBaseUrl}/api/shops/${shopId}/products`,
      {
        params: this.toParams(filters)
      }
    );
  }

  getPublicShopProduct(shopProductId: number): Observable<PublicShopProductResponse> {
    return this.http.get<PublicShopProductResponse>(
      `${this.apiBaseUrl}/api/shop-products/${shopProductId}`
    );
  }

  private toParams(filters: ShopProductSearchFilters): HttpParams {
    return Object.entries(filters).reduce((params, [key, value]) => {
      if (value === null || value === undefined || value === '') {
        return params;
      }
      return params.set(key, String(value).trim());
    }, new HttpParams());
  }
}
