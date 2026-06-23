import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ProfileComponent } from './profile.component';

describe('ProfileComponent', () => {
  let httpTestingController: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('loads the authenticated user profile', () => {
    const fixture = TestBed.createComponent(ProfileComponent);
    fixture.detectChanges();

    const request = httpTestingController.expectOne('http://localhost:8080/api/users/me');
    request.flush({
      id: 1,
      email: 'reader@example.com',
      displayName: 'Reader',
      preferredInterfaceLanguage: 'es',
      roles: ['USER']
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('reader@example.com');
  });
});
