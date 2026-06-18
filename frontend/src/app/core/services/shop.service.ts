import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateShopRequest, ShopResponse, UpdateShopRequest } from '../models/shop.model';

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  createShop(request: CreateShopRequest): Observable<ShopResponse> {
    return this.http.post<ShopResponse>(`${this.apiBaseUrl}/api/shops`, request);
  }

  getMyShops(): Observable<ShopResponse[]> {
    return this.http.get<ShopResponse[]>(`${this.apiBaseUrl}/api/shops/my`);
  }

  getPublicShop(shopId: number): Observable<ShopResponse> {
    return this.http.get<ShopResponse>(`${this.apiBaseUrl}/api/shops/${shopId}`);
  }

  updateShop(shopId: number, request: UpdateShopRequest): Observable<ShopResponse> {
    return this.http.put<ShopResponse>(`${this.apiBaseUrl}/api/shops/${shopId}`, request);
  }
}
