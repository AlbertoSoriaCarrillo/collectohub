import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { UserMeResponse } from '../../core/models/user-me-response.model';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  const user: UserMeResponse = {
    id: 3,
    email: 'collector@example.com',
    displayName: 'Ada Collectora',
    preferredInterfaceLanguage: 'es',
    roles: ['USER', 'SHOP_OWNER']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            currentUser: signal<UserMeResponse | null>(null),
            getMe: vi.fn(() => of(user)),
            logout: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('renders basic user data', async () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Ada Collectora');
    expect(compiled.textContent).toContain('collector@example.com');
    expect(compiled.textContent).toMatch(/Propietario de tienda|Shop owner/);
  });

  it('links wanted items from dashboard sections', async () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const recommendationLink = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('a')
    ).find((link) => link.getAttribute('href') === '/wanted');

    expect(recommendationLink).toBeTruthy();
  });

  it('links profile from dashboard sections', async () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const profileLink = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('a')
    ).find((link) => link.getAttribute('href') === '/profile');

    expect(profileLink).toBeTruthy();
  });
});
