import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { TokenStorageService } from './token-storage.service';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let tokenStorage: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  it('adds a Bearer token when one exists', () => {
    tokenStorage.saveSession({
      id: 1,
      email: 'user@example.com',
      displayName: 'Test User',
      preferredInterfaceLanguage: 'es',
      roles: ['USER'],
      accessToken: 'access-token',
      refreshToken: 'refresh-token'
    });

    httpClient.get('http://localhost:8080/api/users/me').subscribe();

    const request = httpTestingController.expectOne('http://localhost:8080/api/users/me');
    expect(request.request.headers.get('Authorization')).toBe('Bearer access-token');
    request.flush({});
  });
});
