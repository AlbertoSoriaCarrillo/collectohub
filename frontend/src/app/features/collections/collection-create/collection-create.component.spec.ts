import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { Router, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionCreateComponent } from './collection-create.component';

describe('CollectionCreateComponent', () => {
  const categories: ProductCategoryResponse[] = [
    { id: 1, code: 'MANGA_COMIC', name: 'Manga / Comic', parentId: null }
  ];
  const collection = {
    id: 9, userId: 2, name: 'Manga', description: null, visibility: 'PRIVATE' as const,
    categoryCode: null, categoryName: null, items: []
  };
  const catalogService = { getCategories: vi.fn(() => of(categories)) };
  const collectionService = { createCollection: vi.fn(() => of(collection)) };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CollectionCreateComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: CatalogService, useValue: catalogService },
        { provide: CollectionService, useValue: collectionService }
      ]
    }).compileComponents();
    catalogService.getCategories.mockReset();
    catalogService.getCategories.mockReturnValue(of(categories));
    collectionService.createCollection.mockReset();
    collectionService.createCollection.mockReturnValue(of(collection));
  });

  afterEach(() => TestBed.resetTestingModule());

  it('validates empty, whitespace-only and oversized fields', () => {
    const component = TestBed.createComponent(CollectionCreateComponent).componentInstance;

    component.form.controls.name.setValue('   ');
    component.form.controls.description.setValue('x'.repeat(4001));
    component.submit();

    expect(component.form.controls.name.invalid).toBe(true);
    expect(component.form.controls.name.touched).toBe(true);
    expect(component.form.controls.description.hasError('maxlength')).toBe(true);
    expect(collectionService.createCollection).not.toHaveBeenCalled();
  });

  it('loads categories and sends trimmed optional values as null', () => {
    const fixture = TestBed.createComponent(CollectionCreateComponent);
    const component = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    component.form.setValue({ name: '  Manga  ', description: '   ', visibility: 'PRIVATE', categoryCode: '' });

    component.submit();

    expect(component.categories()).toEqual(categories);
    expect(collectionService.createCollection).toHaveBeenCalledWith({
      name: 'Manga', description: null, visibility: 'PRIVATE', categoryCode: null
    });
    expect(navigate).toHaveBeenCalledWith(['/collections', 9]);
  });

  it('blocks duplicate submits while saving and retains an API error', () => {
    const pending = new Subject<typeof collection>();
    collectionService.createCollection.mockReturnValueOnce(pending);
    const component = TestBed.createComponent(CollectionCreateComponent).componentInstance;
    component.form.setValue({ name: 'Manga', description: '', visibility: 'PRIVATE', categoryCode: '' });

    component.submit();
    component.submit();
    expect(collectionService.createCollection).toHaveBeenCalledTimes(1);

    pending.error(new Error('request failed'));
    expect(component.saving()).toBe(false);
    expect(component.errorMessage()).toBeTruthy();
  });

  it('keeps a translated categories error visible', () => {
    catalogService.getCategories.mockReturnValueOnce(throwError(() => new Error('categories failed')));
    const fixture = TestBed.createComponent(CollectionCreateComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.errorMessage()).toMatch(/categor/i);
  });
});
