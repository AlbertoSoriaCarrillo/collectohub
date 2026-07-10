import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  BackfillMasterProductCatalogLinksResponse,
  CatalogFranchiseResponse,
  CatalogItemEditionResponse,
  CatalogItemCreatorResponse,
  CatalogItemResponse,
  CatalogSeriesResponse,
  CreateEditorialAdminItemRelationshipRequest,
  CreateMasterProductCatalogLinkRequest,
  CreateCatalogItemCreatorRequest,
  CreateCatalogFranchiseRequest,
  CreateCatalogItemEditionRequest,
  CreateCatalogItemRequest,
  CreateCatalogSeriesRequest,
  CreateCreatorRequest,
  CreatePublisherRequest,
  CreatorResponse,
  EditorialAdminCreatorSearchParams,
  EditorialAdminEditionSearchParams,
  EditorialAdminItemRelationshipResponse,
  EditorialAdminItemSearchParams,
  EditorialAdminSearchParams,
  EditorialAdminSeriesSearchParams,
  EditorialLegacyBridgeResponse,
  EditorialDataQualityReportResponse,
  EditorialDataQualityScope,
  MasterProductCatalogLinkResponse,
  MasterProductCatalogLinkSearchParams,
  PageResponse,
  PublisherResponse,
  UpdateEditorialAdminItemRelationshipRequest,
  UpdateMasterProductCatalogLinkRequest,
  UpdateCatalogItemCreatorRequest,
  UpdateCatalogFranchiseRequest,
  UpdateCatalogItemEditionRequest,
  UpdateCatalogItemRequest,
  UpdateCatalogSeriesRequest,
  UpdateCreatorRequest,
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

  searchItems(
    seriesId: number,
    params: EditorialAdminItemSearchParams = {}
  ): Observable<PageResponse<CatalogItemResponse>> {
    return this.http.get<PageResponse<CatalogItemResponse>>(
      `${this.baseUrl}/series/${seriesId}/items`,
      { params: this.toParams(params, 'sortOrder,asc') }
    );
  }

  getItem(id: number): Observable<CatalogItemResponse> {
    return this.http.get<CatalogItemResponse>(`${this.baseUrl}/items/${id}`);
  }

  createItem(seriesId: number, request: CreateCatalogItemRequest): Observable<CatalogItemResponse> {
    return this.http.post<CatalogItemResponse>(`${this.baseUrl}/series/${seriesId}/items`, request);
  }

  updateItem(id: number, request: UpdateCatalogItemRequest): Observable<CatalogItemResponse> {
    return this.http.put<CatalogItemResponse>(`${this.baseUrl}/items/${id}`, request);
  }

  searchEditions(
    itemId: number,
    params: EditorialAdminEditionSearchParams = {}
  ): Observable<PageResponse<CatalogItemEditionResponse>> {
    return this.http.get<PageResponse<CatalogItemEditionResponse>>(
      `${this.baseUrl}/items/${itemId}/editions`,
      { params: this.toParams(params, 'publicationYear,asc') }
    );
  }

  getEdition(id: number): Observable<CatalogItemEditionResponse> {
    return this.http.get<CatalogItemEditionResponse>(`${this.baseUrl}/editions/${id}`);
  }

  createEdition(
    itemId: number,
    request: CreateCatalogItemEditionRequest
  ): Observable<CatalogItemEditionResponse> {
    return this.http.post<CatalogItemEditionResponse>(
      `${this.baseUrl}/items/${itemId}/editions`,
      request
    );
  }

  updateEdition(
    id: number,
    request: UpdateCatalogItemEditionRequest
  ): Observable<CatalogItemEditionResponse> {
    return this.http.put<CatalogItemEditionResponse>(`${this.baseUrl}/editions/${id}`, request);
  }

  searchCreators(
    params: EditorialAdminCreatorSearchParams = {}
  ): Observable<PageResponse<CreatorResponse>> {
    return this.http.get<PageResponse<CreatorResponse>>(`${this.baseUrl}/creators`, {
      params: this.toParams(params, 'name,asc')
    });
  }

  getCreator(id: number): Observable<CreatorResponse> {
    return this.http.get<CreatorResponse>(`${this.baseUrl}/creators/${id}`);
  }

  createCreator(request: CreateCreatorRequest): Observable<CreatorResponse> {
    return this.http.post<CreatorResponse>(`${this.baseUrl}/creators`, request);
  }

  updateCreator(id: number, request: UpdateCreatorRequest): Observable<CreatorResponse> {
    return this.http.put<CreatorResponse>(`${this.baseUrl}/creators/${id}`, request);
  }

  deleteCreator(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/creators/${id}`);
  }

  listItemCreatorCredits(itemId: number): Observable<CatalogItemCreatorResponse[]> {
    return this.http.get<CatalogItemCreatorResponse[]>(
      `${this.baseUrl}/items/${itemId}/creators`
    );
  }

  createItemCreatorCredit(
    itemId: number,
    request: CreateCatalogItemCreatorRequest
  ): Observable<CatalogItemCreatorResponse> {
    return this.http.post<CatalogItemCreatorResponse>(
      `${this.baseUrl}/items/${itemId}/creators`,
      request
    );
  }

  updateItemCreatorCredit(
    itemId: number,
    creditId: number,
    request: UpdateCatalogItemCreatorRequest
  ): Observable<CatalogItemCreatorResponse> {
    return this.http.put<CatalogItemCreatorResponse>(
      `${this.baseUrl}/items/${itemId}/creators/${creditId}`,
      request
    );
  }

  deleteItemCreatorCredit(itemId: number, creditId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/items/${itemId}/creators/${creditId}`);
  }

  getItemRelationships(
    itemId: number,
    recordStatus?: string | null
  ): Observable<EditorialAdminItemRelationshipResponse[]> {
    let params = new HttpParams();
    const normalized = recordStatus?.trim();
    if (normalized) {
      params = params.set('recordStatus', normalized);
    }
    return this.http.get<EditorialAdminItemRelationshipResponse[]>(
      `${this.baseUrl}/items/${itemId}/relationships`,
      { params }
    );
  }

  createItemRelationship(
    itemId: number,
    request: CreateEditorialAdminItemRelationshipRequest
  ): Observable<EditorialAdminItemRelationshipResponse> {
    return this.http.post<EditorialAdminItemRelationshipResponse>(
      `${this.baseUrl}/items/${itemId}/relationships`,
      request
    );
  }

  updateItemRelationship(
    itemId: number,
    relationshipId: number,
    request: UpdateEditorialAdminItemRelationshipRequest
  ): Observable<EditorialAdminItemRelationshipResponse> {
    return this.http.put<EditorialAdminItemRelationshipResponse>(
      `${this.baseUrl}/items/${itemId}/relationships/${relationshipId}`,
      request
    );
  }

  deleteItemRelationship(itemId: number, relationshipId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/items/${itemId}/relationships/${relationshipId}`
    );
  }

  searchMasterProductLinks(
    params: MasterProductCatalogLinkSearchParams = {}
  ): Observable<PageResponse<MasterProductCatalogLinkResponse>> {
    return this.http.get<PageResponse<MasterProductCatalogLinkResponse>>(
      `${this.baseUrl}/master-product-links`,
      { params: this.toParams(params, 'createdAt,desc') }
    );
  }

  getMasterProductLink(id: number): Observable<MasterProductCatalogLinkResponse> {
    return this.http.get<MasterProductCatalogLinkResponse>(`${this.baseUrl}/master-product-links/${id}`);
  }

  createMasterProductLink(
    request: CreateMasterProductCatalogLinkRequest
  ): Observable<MasterProductCatalogLinkResponse> {
    return this.http.post<MasterProductCatalogLinkResponse>(`${this.baseUrl}/master-product-links`, request);
  }

  updateMasterProductLink(
    id: number,
    request: UpdateMasterProductCatalogLinkRequest
  ): Observable<MasterProductCatalogLinkResponse> {
    return this.http.put<MasterProductCatalogLinkResponse>(`${this.baseUrl}/master-product-links/${id}`, request);
  }

  verifyMasterProductLink(id: number): Observable<MasterProductCatalogLinkResponse> {
    return this.http.put<MasterProductCatalogLinkResponse>(`${this.baseUrl}/master-product-links/${id}/verify`, {});
  }

  rejectMasterProductLink(id: number): Observable<MasterProductCatalogLinkResponse> {
    return this.http.put<MasterProductCatalogLinkResponse>(`${this.baseUrl}/master-product-links/${id}/reject`, {});
  }

  backfillMasterProductLinks(): Observable<BackfillMasterProductCatalogLinksResponse> {
    return this.http.post<BackfillMasterProductCatalogLinksResponse>(`${this.baseUrl}/master-product-links/backfill`, {});
  }

  getEditorialLegacyBridge(masterProductId: number): Observable<EditorialLegacyBridgeResponse> {
    return this.http.get<EditorialLegacyBridgeResponse>(
      `${this.baseUrl}/editorial/master-products/${masterProductId}/link`
    );
  }

  getEditorialDataQualityReport(scope: EditorialDataQualityScope = 'ALL', limit = 50): Observable<EditorialDataQualityReportResponse> {
    return this.http.get<EditorialDataQualityReportResponse>(`${this.baseUrl}/admin/data-quality/report`, {
      params: new HttpParams().set('scope', scope).set('limit', String(limit))
    });
  }

  private toParams(
    params:
      | EditorialAdminSearchParams
      | EditorialAdminSeriesSearchParams
      | EditorialAdminItemSearchParams
      | EditorialAdminEditionSearchParams
      | EditorialAdminCreatorSearchParams
      | MasterProductCatalogLinkSearchParams,
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
