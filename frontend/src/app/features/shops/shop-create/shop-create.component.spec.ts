import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { ShopService } from '../../../core/services/shop.service';
import { ShopCreateComponent } from './shop-create.component';

describe('ShopCreateComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShopCreateComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ShopService,
          useValue: {
            createShop: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('validates the required shop name', () => {
    const fixture = TestBed.createComponent(ShopCreateComponent);
    const component = fixture.componentInstance;

    component.submit();

    expect(component.form.invalid).toBe(true);
    expect(component.form.controls.name.hasError('required')).toBe(true);
  });
});
