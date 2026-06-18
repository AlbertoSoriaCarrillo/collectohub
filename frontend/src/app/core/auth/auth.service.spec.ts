import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { TokenStorageService } from './token-storage.service';
import { AuthResponse } from '../models/auth-response.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;
  let tokenStorage: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  it('calls login and stores the returned session', () => {
    const response: AuthResponse = {
      id: 7,
      email: 'user@example.com',
      displayName: 'Test User',
      preferredInterfaceLanguage: 'es',
      roles: ['USER'],
      accessToken: 'access-token',
      refreshToken: 'refresh-token'
    };

    service.login({ email: 'user@example.com', password: 'Password123!' }).subscribe((authResponse) => {
      expect(authResponse.email).toBe('user@example.com');
    });

    const request = httpTestingController.expectOne('http://localhost:8080/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      email: 'user@example.com',
      password: 'Password123!'
    });
    request.flush(response);

    expect(tokenStorage.getAccessToken()).toBe('access-token');
    expect(tokenStorage.getRefreshToken()).toBe('refresh-token');
    expect(tokenStorage.getRoles()).toEqual(['USER']);
  });
});
