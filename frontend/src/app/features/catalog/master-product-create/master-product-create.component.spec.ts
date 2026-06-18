import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { CatalogService } from '../../../core/services/catalog.service';
import { MasterProductCreateComponent } from './master-product-create.component';

describe('MasterProductCreateComponent', () => {
  const category = {
    id: 1,
    code: 'MANGA_COMIC',
    name: 'Manga / Comic',
    parentId: null
  };

  function configure(canCreate: boolean, createMasterProduct = vi.fn()): Promise<void> {
    return TestBed.configureTestingModule({
      imports: [MasterProductCreateComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            hasAnyRole: vi.fn(() => canCreate)
          }
        },
        {
          provide: CatalogService,
          useValue: {
            getCategories: vi.fn(() => of([category])),
            createMasterProduct
          }
        }
      ]
    }).compileComponents();
  }

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('validates required name and category fields', async () => {
    await configure(true);
    const fixture = TestBed.createComponent(MasterProductCreateComponent);
    const component = fixture.componentInstance;

    component.submit();

    expect(component.form.invalid).toBe(true);
    expect(component.form.controls.name.hasError('required')).toBe(true);
    expect(component.form.controls.categoryCode.hasError('required')).toBe(true);
  });

  it('blocks creation in the UI when the user is not SHOP_OWNER or ADMIN', async () => {
    const createMasterProduct = vi.fn();
    await configure(false, createMasterProduct);
    const fixture = TestBed.createComponent(MasterProductCreateComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    component.submit();

    expect(createMasterProduct).not.toHaveBeenCalled();
    expect(component.errorMessage()).toContain('No tienes permisos');
  });
});
