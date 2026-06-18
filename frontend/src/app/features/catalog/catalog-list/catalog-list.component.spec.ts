import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CatalogListComponent } from './catalog-list.component';

describe('CatalogListComponent', () => {
  const category: ProductCategoryResponse = {
    id: 1,
    code: 'MANGA_COMIC',
    name: 'Manga / Comic',
    parentId: null
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CatalogListComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: CatalogService,
          useValue: {
            getCategories: vi.fn(() => of([category])),
            searchMasterProducts: vi.fn(() =>
              of([
                {
                  id: 5,
                  name: 'One Piece 1',
                  description: null,
                  category,
                  franchise: 'One Piece',
                  collectionName: 'One Piece',
                  volumeNumber: '1',
                  publisher: null,
                  isbn: '9780000000001',
                  ean: null,
                  releaseDate: null,
                  editionStartDate: null,
                  editionEndDate: null,
                  language: 'es',
                  limitedEdition: false,
                  limitedEditionTotalUnits: null,
                  publicationCountries: ['ES'],
                  coverImageUrl: null,
                  status: 'ACTIVE',
                  attributes: {}
                }
              ])
            )
          }
        },
        {
          provide: AuthService,
          useValue: {
            hasAnyRole: vi.fn(() => false)
          }
        }
      ]
    }).compileComponents();
  });

  it('renders public master products', async () => {
    const fixture = TestBed.createComponent(CatalogListComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('Manga / Comic');
  });
});
