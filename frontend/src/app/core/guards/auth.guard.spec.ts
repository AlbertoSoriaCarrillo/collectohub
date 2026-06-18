import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authGuard } from './auth.guard';
import { TokenStorageService } from '../auth/token-storage.service';

describe('authGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('blocks dashboard when there is no token', () => {
    const router = TestBed.inject(Router);

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/dashboard' } as RouterStateSnapshot)
    );

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login?returnUrl=%2Fdashboard');
  });

  it('allows navigation when a token exists', () => {
    TestBed.inject(TokenStorageService).saveSession({
      id: 1,
      email: 'user@example.com',
      displayName: 'Test User',
      preferredInterfaceLanguage: 'es',
      roles: ['USER'],
      accessToken: 'access-token',
      refreshToken: 'refresh-token'
    });

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/dashboard' } as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });
});
