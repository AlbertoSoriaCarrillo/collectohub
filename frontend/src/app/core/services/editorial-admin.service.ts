import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CatalogFranchiseResponse,
  CatalogSeriesResponse,
  CreateCatalogFranchiseRequest,
  CreateCatalogSeriesRequest,
  CreatePublisherRequest,
  EditorialAdminSearchParams,
  EditorialAdminSeriesSearchParams,
  PageResponse,
  PublisherResponse,
  UpdateCatalogFranchiseRequest,
  UpdateCatalogSeriesRequest,
  UpdatePublisherRequest
} from '../models/editorial-admin.model';

@Injectable({
  providedIn: 'root'
})
export class EditorialAdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/catalog`;

  searchPublishers(
    params: EditorialAdminSearchParams = {}
  ): Observable<PageResponse<PublisherResponse>> {
    return this.http.get<PageResponse<PublisherResponse>>(`${this.baseUrl}/publishers`, {
      params: this.toParams(params, 'name,asc')
    });
  }

  getPublisher(id: number): Observable<PublisherResponse> {
    return this.http.get<PublisherResponse>(`${this.baseUrl}/publishers/${id}`);
  }

  createPublisher(request: CreatePublisherRequest): Observable<PublisherResponse> {
    return this.http.post<PublisherResponse>(`${this.baseUrl}/publishers`, request);
  }

  updatePublisher(id: number, request: UpdatePublisherRequest): Observable<PublisherResponse> {
    return this.http.put<PublisherResponse>(`${this.baseUrl}/publishers/${id}`, request);
  }

  searchFranchises(
    params: EditorialAdminSearchParams = {}
  ): Observable<PageResponse<CatalogFranchiseResponse>> {
    return this.http.get<PageResponse<CatalogFranchiseResponse>>(`${this.baseUrl}/franchises`, {
      params: this.toParams(params, 'name,asc')
    });
  }

  getFranchise(id: number): Observable<CatalogFranchiseResponse> {
    return this.http.get<CatalogFranchiseResponse>(`${this.baseUrl}/franchises/${id}`);
  }

  createFranchise(
    request: CreateCatalogFranchiseRequest
  ): Observable<CatalogFranchiseResponse> {
    return this.http.post<CatalogFranchiseResponse>(`${this.baseUrl}/franchises`, request);
  }

  updateFranchise(
    id: number,
    request: UpdateCatalogFranchiseRequest
  ): Observable<CatalogFranchiseResponse> {
    return this.http.put<CatalogFranchiseResponse>(`${this.baseUrl}/franchises/${id}`, request);
  }

  searchSeries(
    params: EditorialAdminSeriesSearchParams = {}
  ): Observable<PageResponse<CatalogSeriesResponse>> {
    return this.http.get<PageResponse<CatalogSeriesResponse>>(`${this.baseUrl}/series`, {
      params: this.toParams(params, 'title,asc')
    });
  }

  getSeries(id: number): Observable<CatalogSeriesResponse> {
    return this.http.get<CatalogSeriesResponse>(`${this.baseUrl}/series/${id}`);
  }

  createSeries(request: CreateCatalogSeriesRequest): Observable<CatalogSeriesResponse> {
    return this.http.post<CatalogSeriesResponse>(`${this.baseUrl}/series`, request);
  }

  updateSeries(id: number, request: UpdateCatalogSeriesRequest): Observable<CatalogSeriesResponse> {
    return this.http.put<CatalogSeriesResponse>(`${this.baseUrl}/series/${id}`, request);
  }

  private toParams(
    params: EditorialAdminSearchParams | EditorialAdminSeriesSearchParams,
    defaultSort: string
  ): HttpParams {
    const withDefaults = {
      page: 0,
      size: 20,
      sort: defaultSort,
      ...params
    };

    return Object.entries(withDefaults).reduce((httpParams, [key, value]) => {
      if (value === null || value === undefined) {
        return httpParams;
      }
      const normalized = typeof value === 'string' ? value.trim() : String(value);
      return normalized ? httpParams.set(key, normalized) : httpParams;
    }, new HttpParams());
  }
}
