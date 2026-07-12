import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { editorialAdminGuard } from './editorial-admin.guard';

describe('editorialAdminGuard', () => {
  const authService = {
    hasToken: vi.fn(),
    hasEditorialAdminAccess: vi.fn()
  };

  beforeEach(() => {
    authService.hasToken.mockReset();
    authService.hasEditorialAdminAccess.mockReset();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: authService }]
    });
  });

  it('redirects anonymous users to login with returnUrl', () => {
    authService.hasToken.mockReturnValue(false);
    const router = TestBed.inject(Router);

    expect(router.serializeUrl(runGuard('/admin/editorial') as ReturnType<Router['createUrlTree']>)).toBe(
      '/login?returnUrl=%2Fadmin%2Feditorial'
    );
    expect(authService.hasEditorialAdminAccess).not.toHaveBeenCalled();
  });

  it.each(['USER', 'SHOP_OWNER', 'CONTENT_CREATOR'])('redirects %s users without editorial access home', () => {
    authService.hasToken.mockReturnValue(true);
    authService.hasEditorialAdminAccess.mockReturnValue(false);
    const router = TestBed.inject(Router);

    expect(router.serializeUrl(runGuard('/admin/editorial') as ReturnType<Router['createUrlTree']>)).toBe('/home');
  });

  it.each(['ADMIN', 'EDITORIAL_ADMIN', 'ADMIN + EDITORIAL_ADMIN'])('allows %s users', () => {
    authService.hasToken.mockReturnValue(true);
    authService.hasEditorialAdminAccess.mockReturnValue(true);

    expect(runGuard('/admin/editorial')).toBe(true);
  });
});

function runGuard(url: string) {
  return TestBed.runInInjectionContext(() => editorialAdminGuard({} as never, { url } as never));
}
