import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();
  });

  it('renders the public home experience', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('CollectoHub');
  });

  it('routes anonymous secondary action to login instead of register', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    const secondaryLink = fixture.nativeElement.querySelector(
      '[data-testid="home-secondary-link"]'
    ) as HTMLAnchorElement;

    expect(secondaryLink.getAttribute('href')).toBe('/login');
  });
});
