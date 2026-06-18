import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ShopService } from '../../../core/services/shop.service';
import { MyShopsComponent } from './my-shops.component';

describe('MyShopsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyShopsComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ShopService,
          useValue: {
            getMyShops: vi.fn(() => of([]))
          }
        }
      ]
    }).compileComponents();
  });

  it('renders the empty state when the user has no shops', async () => {
    const fixture = TestBed.createComponent(MyShopsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Aun no tienes tiendas');
  });
});
