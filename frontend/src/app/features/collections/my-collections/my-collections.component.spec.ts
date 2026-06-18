import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CollectionResponse } from '../../../core/models/collection.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { MyCollectionsComponent } from './my-collections.component';

describe('MyCollectionsComponent', () => {
  const category: ProductCategoryResponse = {
    id: 1,
    code: 'MANGA_COMIC',
    name: 'Manga / Comic',
    parentId: null
  };

  const collection: CollectionResponse = {
    id: 3,
    userId: 2,
    name: 'Manga pendientes',
    description: 'Lista personal',
    visibility: 'PRIVATE',
    categoryCode: 'MANGA_COMIC',
    categoryName: 'Manga / Comic',
    items: []
  };

  async function configure(collections: CollectionResponse[]): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [MyCollectionsComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: CatalogService,
          useValue: {
            getCategories: vi.fn(() => of([category]))
          }
        },
        {
          provide: CollectionService,
          useValue: {
            getMyCollections: vi.fn(() => of(collections)),
            deleteCollection: vi.fn(() => of(null))
          }
        }
      ]
    }).compileComponents();
  }

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('renders the empty state', async () => {
    await configure([]);
    const fixture = TestBed.createComponent(MyCollectionsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Aun no tienes colecciones'
    );
  });

  it('renders collection cards', async () => {
    await configure([collection]);
    const fixture = TestBed.createComponent(MyCollectionsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Manga pendientes');
    expect(compiled.textContent).toContain('Manga / Comic');
  });
});
