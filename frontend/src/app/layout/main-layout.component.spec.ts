import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { UserMeResponse } from '../core/models/user-me-response.model';
import { MainLayoutComponent } from './main-layout.component';

describe('MainLayoutComponent', () => {
  const currentUser = signal<UserMeResponse | null>(null);
  const authService = {
    currentUser: computed(() => currentUser()),
    isAuthenticated: computed(() => Boolean(currentUser())),
    hasRole: vi.fn((role: string) => currentUser()?.roles.includes(role) ?? false),
    hasEditorialAdminAccess: vi.fn(() =>
      currentUser()?.roles.some((role) => role === 'ADMIN' || role === 'EDITORIAL_ADMIN') ?? false
    ),
    logout: vi.fn()
  };

  beforeEach(async () => {
    currentUser.set(null);
    authService.hasRole.mockImplementation((role: string) => currentUser()?.roles.includes(role) ?? false);
    authService.hasEditorialAdminAccess.mockImplementation(() =>
      currentUser()?.roles.some((role) => role === 'ADMIN' || role === 'EDITORIAL_ADMIN') ?? false
    );
    authService.logout.mockClear();

    await TestBed.configureTestingModule({
      imports: [MainLayoutComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: AuthService,
          useValue: authService
        }
      ]
    }).compileComponents();
  });

  it('shows public header actions without a register CTA', () => {
    const fixture = TestBed.createComponent(MainLayoutComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('[data-testid="login-header-link"]')).toBeTruthy();
    expect(compiled.querySelector('[data-testid="language-selector"]')).toBeTruthy();
    expect(compiled.querySelector('[data-testid="register-link"]')).toBeFalsy();
    expect(compiled.querySelector('[data-testid="user-menu-button"]')).toBeFalsy();
    expect(compiled.querySelector('[data-testid="admin-editorial-nav-link"]')).toBeFalsy();
    expect(compiled.querySelector('[data-testid="admin-editorial-sidebar-link"]')).toBeFalsy();
    expect(visibleRoutes(compiled)).toContain('/catalog/editorial');
    expect(visibleRoutes(compiled)).not.toContain('/shops');
    expect(visibleRoutes(compiled)).not.toContain('/reservations');
  });

  it('shows the authenticated user menu instead of login', () => {
    currentUser.set({
      id: 1,
      email: 'reader@example.com',
      displayName: 'Reader Example',
      preferredInterfaceLanguage: 'es',
      roles: ['USER']
    });

    const fixture = TestBed.createComponent(MainLayoutComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('[data-testid="user-menu-button"]')).toBeTruthy();
    expect(compiled.querySelector('[data-testid="login-header-link"]')).toBeFalsy();
    expect(compiled.querySelector('[data-testid="admin-editorial-nav-link"]')).toBeFalsy();
    expect(compiled.querySelector('[data-testid="admin-editorial-sidebar-link"]')).toBeFalsy();
    expect(visibleRoutes(compiled)).toContain('/catalog/editorial');
    expect(visibleRoutes(compiled)).not.toContain('/shops');
    expect(visibleRoutes(compiled)).not.toContain('/reservations');
  });

  it.each([
    ['ADMIN', ['USER', 'ADMIN']],
    ['EDITORIAL_ADMIN', ['USER', 'EDITORIAL_ADMIN']],
    ['both editorial roles', ['ADMIN', 'EDITORIAL_ADMIN']]
  ])('shows one editorial admin link for %s users', (_role, roles) => {
    currentUser.set({
      id: 2,
      email: 'admin@example.com',
      displayName: 'Admin Example',
      preferredInterfaceLanguage: 'es',
      roles
    });

    const fixture = TestBed.createComponent(MainLayoutComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('[data-testid="admin-editorial-nav-link"]')).toBeTruthy();
    expect(compiled.querySelector('[data-testid="admin-editorial-sidebar-link"]')).toBeTruthy();
    expect(compiled.querySelectorAll('[data-testid="admin-editorial-nav-link"]')).toHaveLength(1);
    expect(visibleRoutes(compiled)).toContain('/admin/editorial');
  });

  it.each(['SHOP_OWNER', 'CONTENT_CREATOR'])('hides editorial admin navigation from %s users', (role) => {
    currentUser.set({
      id: 3,
      email: 'user@example.com',
      displayName: 'User Example',
      preferredInterfaceLanguage: 'es',
      roles: ['USER', role]
    });

    const fixture = TestBed.createComponent(MainLayoutComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-testid="admin-editorial-nav-link"]')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('[data-testid="admin-editorial-sidebar-link"]')).toBeFalsy();
  });
});

function visibleRoutes(element: HTMLElement): Array<string | null> {
  return Array.from(element.querySelectorAll<HTMLAnchorElement>('a')).map((link) =>
    link.getAttribute('href')
  );
}
