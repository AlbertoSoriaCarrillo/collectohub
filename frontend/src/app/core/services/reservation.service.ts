import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateReservationRequest,
  ReservationResponse,
  ReservationSearchFilters,
  ShopReservationSearchFilters,
  UpdateReservationStatusRequest
} from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  createReservation(request: CreateReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.apiBaseUrl}/api/reservations`, request);
  }

  getMyReservations(filters: ReservationSearchFilters = {}): Observable<ReservationResponse[]> {
    return this.http.get<ReservationResponse[]>(`${this.apiBaseUrl}/api/reservations/my`, {
      params: this.toParams(filters)
    });
  }

  getReservation(reservationId: number): Observable<ReservationResponse> {
    return this.http.get<ReservationResponse>(`${this.apiBaseUrl}/api/reservations/${reservationId}`);
  }

  getShopReservations(
    shopId: number,
    filters: ShopReservationSearchFilters = {}
  ): Observable<ReservationResponse[]> {
    return this.http.get<ReservationResponse[]>(`${this.apiBaseUrl}/api/shops/${shopId}/reservations`, {
      params: this.toParams(filters)
    });
  }

  updateShopReservationStatus(
    shopId: number,
    reservationId: number,
    request: UpdateReservationStatusRequest
  ): Observable<ReservationResponse> {
    return this.http.put<ReservationResponse>(
      `${this.apiBaseUrl}/api/shops/${shopId}/reservations/${reservationId}/status`,
      request
    );
  }

  cancelMyReservation(reservationId: number): Observable<ReservationResponse> {
    return this.http.put<ReservationResponse>(
      `${this.apiBaseUrl}/api/reservations/${reservationId}/cancel`,
      {}
    );
  }

  private toParams(filters: ReservationSearchFilters | ShopReservationSearchFilters): HttpParams {
    return Object.entries(filters).reduce((params, [key, value]) => {
      if (value === null || value === undefined || value === '') {
        return params;
      }

      return params.set(key, String(value));
    }, new HttpParams());
  }
}
