import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionItemCreateComponent } from './collection-item-create.component';

describe('CollectionItemCreateComponent', () => {
  const category: ProductCategoryResponse = {
    id: 1,
    code: 'MANGA_COMIC',
    name: 'Manga / Comic',
    parentId: null
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CollectionItemCreateComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ collectionId: '3' })
            }
          }
        },
        {
          provide: CatalogService,
          useValue: {
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
                  isbn: null,
                  ean: null,
                  releaseDate: null,
                  editionStartDate: null,
                  editionEndDate: null,
                  language: 'es',
                  limitedEdition: false,
                  limitedEditionTotalUnits: null,
                  publicationCountries: [],
                  coverImageUrl: null,
                  status: 'ACTIVE',
                  attributes: {}
                }
              ])
            )
          }
        },
        {
          provide: CollectionService,
          useValue: {
            addCollectionItem: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('validates required product and status fields', () => {
    const fixture = TestBed.createComponent(CollectionItemCreateComponent);
    const component = fixture.componentInstance;

    component.submit();

    expect(component.form.controls.masterProductId.hasError('required')).toBe(true);
    expect(component.form.controls.collectionStatus.hasError('required')).toBe(true);
  });

  it('offers only the four collection states in MVP 1', () => {
    const fixture = TestBed.createComponent(CollectionItemCreateComponent);

    expect(fixture.componentInstance.statuses).toEqual([
      'OWNED',
      'WANTED',
      'MISSING',
      'DUPLICATED'
    ]);
  });
});
