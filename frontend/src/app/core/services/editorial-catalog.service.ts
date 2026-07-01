import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EditorialCatalogEditionDetail,
  EditorialCatalogItemDetail,
  EditorialCatalogSearchItem,
  EditorialCatalogSearchParams,
  EditorialCatalogSeriesDetail,
  EditorialLegacyBridge,
  PageResponse
} from '../models/editorial-catalog.model';

@Injectable({
  providedIn: 'root'
})
export class EditorialCatalogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/catalog/editorial`;

  search(
    params: EditorialCatalogSearchParams = {}
  ): Observable<PageResponse<EditorialCatalogSearchItem>> {
    return this.http.get<PageResponse<EditorialCatalogSearchItem>>(`${this.baseUrl}/search`, {
      params: this.toParams(params)
    });
  }

  getSeriesDetail(seriesId: number): Observable<EditorialCatalogSeriesDetail> {
    return this.http.get<EditorialCatalogSeriesDetail>(
      `${this.baseUrl}/series/${seriesId}/detail`
    );
  }

  getItemDetail(itemId: number): Observable<EditorialCatalogItemDetail> {
    return this.http.get<EditorialCatalogItemDetail>(`${this.baseUrl}/items/${itemId}/detail`);
  }

  getEditionDetail(editionId: number): Observable<EditorialCatalogEditionDetail> {
    return this.http.get<EditorialCatalogEditionDetail>(
      `${this.baseUrl}/editions/${editionId}/detail`
    );
  }

  getMasterProductLink(masterProductId: number): Observable<EditorialLegacyBridge> {
    return this.http.get<EditorialLegacyBridge>(
      `${this.baseUrl}/master-products/${masterProductId}/link`
    );
  }

  private toParams(params: EditorialCatalogSearchParams): HttpParams {
    return Object.entries(params).reduce((httpParams, [key, value]) => {
      if (value === null || value === undefined) {
        return httpParams;
      }
      const normalized = typeof value === 'string' ? value.trim() : String(value);
      return normalized ? httpParams.set(key, normalized) : httpParams;
    }, new HttpParams());
  }
}
