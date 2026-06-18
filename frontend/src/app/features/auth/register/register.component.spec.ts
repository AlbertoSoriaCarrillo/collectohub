import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();
  });

  it('validates required fields', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    const component = fixture.componentInstance;

    component.submit();

    expect(component.form.invalid).toBe(true);
    expect(component.form.controls.email.hasError('required')).toBe(true);
    expect(component.form.controls.password.hasError('required')).toBe(true);
    expect(component.form.controls.displayName.hasError('required')).toBe(true);
  });
});
