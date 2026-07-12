import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models/auth-response.model';
import { LoginRequest } from '../models/login-request.model';
import { RegisterRequest } from '../models/register-request.model';
import { UserMeResponse } from '../models/user-me-response.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  readonly currentUser = this.tokenStorage.currentUser;
  readonly roles = computed(() => this.tokenStorage.getRoles());
  readonly isAuthenticated = computed(() => this.tokenStorage.hasAccessToken());

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBaseUrl}/api/auth/register`, request)
      .pipe(tap((response) => this.tokenStorage.saveSession(response)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBaseUrl}/api/auth/login`, request)
      .pipe(tap((response) => this.tokenStorage.saveSession(response)));
  }

  getMe(): Observable<UserMeResponse> {
    return this.http
      .get<UserMeResponse>(`${this.apiBaseUrl}/api/users/me`)
      .pipe(tap((user) => this.tokenStorage.saveUser(user)));
  }

  logout(redirect = true): void {
    this.tokenStorage.clear();
    if (redirect) {
      void this.router.navigateByUrl('/login');
    }
  }

  hasToken(): boolean {
    return this.tokenStorage.hasAccessToken();
  }

  hasRole(role: string): boolean {
    return this.tokenStorage.getRoles().includes(role);
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some((role) => this.hasRole(role));
  }

  hasEditorialAdminAccess(): boolean {
    return this.hasAnyRole(['ADMIN', 'EDITORIAL_ADMIN']);
  }
}
