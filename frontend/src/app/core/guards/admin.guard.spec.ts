import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  const authService = {
    hasToken: vi.fn(),
    hasRole: vi.fn()
  };

  beforeEach(() => {
    authService.hasToken.mockReset();
    authService.hasRole.mockReset();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: authService
        }
      ]
    });
  });

  it('allows access when the user has ADMIN role', () => {
    authService.hasToken.mockReturnValue(true);
    authService.hasRole.mockReturnValue(true);

    const result = runGuard('/admin/editorial');

    expect(result).toBe(true);
    expect(authService.hasRole).toHaveBeenCalledWith('ADMIN');
  });

  it('redirects anonymous users to login with returnUrl', () => {
    authService.hasToken.mockReturnValue(false);

    const result = runGuard('/admin/editorial');
    const router = TestBed.inject(Router);

    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe(
      '/login?returnUrl=%2Fadmin%2Feditorial'
    );
    expect(authService.hasRole).not.toHaveBeenCalled();
  });

  it('redirects authenticated users without ADMIN role to home', () => {
    authService.hasToken.mockReturnValue(true);
    authService.hasRole.mockReturnValue(false);

    const result = runGuard('/admin/editorial');
    const router = TestBed.inject(Router);

    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe('/home');
  });

  it('rejects users with only EDITORIAL_ADMIN role', () => {
    authService.hasToken.mockReturnValue(true);
    authService.hasRole.mockImplementation((role: string) => role === 'EDITORIAL_ADMIN');

    const result = runGuard('/admin/global');
    const router = TestBed.inject(Router);

    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe('/home');
    expect(authService.hasRole).toHaveBeenCalledWith('ADMIN');
  });
});

function runGuard(url: string) {
  return TestBed.runInInjectionContext(() =>
    adminGuard({} as never, { url } as never)
  );
}
