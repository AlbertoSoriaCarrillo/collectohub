import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  RecommendationFilters,
  UserRecommendationResponse,
  UserRecommendationSummaryResponse
} from '../models/recommendation.model';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  getMyRecommendations(filters: RecommendationFilters = {}): Observable<UserRecommendationResponse> {
    return this.http.get<UserRecommendationResponse>(`${this.apiBaseUrl}/api/recommendations/my`, {
      params: this.toParams(filters)
    });
  }

  getMyRecommendationSummary(
    filters: RecommendationFilters = {}
  ): Observable<UserRecommendationSummaryResponse> {
    return this.http.get<UserRecommendationSummaryResponse>(
      `${this.apiBaseUrl}/api/recommendations/my/summary`,
      {
        params: this.toParams(filters)
      }
    );
  }

  private toParams(filters: RecommendationFilters): HttpParams {
    return Object.entries(filters).reduce((params, [key, value]) => {
      if (value === null || value === undefined || value === '') {
        return params;
      }

      return params.set(key, String(value));
    }, new HttpParams());
  }
}
