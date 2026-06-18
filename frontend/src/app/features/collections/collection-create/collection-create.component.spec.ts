import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionCreateComponent } from './collection-create.component';

describe('CollectionCreateComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CollectionCreateComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: CatalogService,
          useValue: {
            getCategories: vi.fn(() => of([]))
          }
        },
        {
          provide: CollectionService,
          useValue: {
            createCollection: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('validates required name', () => {
    const fixture = TestBed.createComponent(CollectionCreateComponent);
    const component = fixture.componentInstance;

    component.submit();

    expect(component.form.controls.name.hasError('required')).toBe(true);
  });
});
