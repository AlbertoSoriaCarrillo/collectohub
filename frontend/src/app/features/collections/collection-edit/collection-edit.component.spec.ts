import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CollectionResponse } from '../../../core/models/collection.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionEditComponent } from './collection-edit.component';

describe('CollectionEditComponent', () => {
  const categories: ProductCategoryResponse[] = [{ id: 1, code: 'MANGA_COMIC', name: 'Manga', parentId: null }];
  const collection: CollectionResponse = {
    id: 3, userId: 2, name: 'Manga', description: 'Personal', visibility: 'PUBLIC',
    categoryCode: 'MANGA_COMIC', categoryName: 'Manga', items: []
  };
  const catalogService = { getCategories: vi.fn(() => of(categories)) };
  const collectionService = { getCollection: vi.fn(() => of(collection)), updateCollection: vi.fn(() => of(collection)) };
  const authService = {
    currentUser: signal({ id: 2, email: 'owner@example.com', displayName: 'Owner', preferredInterfaceLanguage: 'es', roles: ['USER'] }),
    hasToken: vi.fn(() => true), getMe: vi.fn(() => of({ id: 2, email: 'owner@example.com', displayName: 'Owner', preferredInterfaceLanguage: 'es', roles: ['USER'] }))
  };

  async function configure(collectionId = '3'): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [CollectionEditComponent],
      providers: [
        provideAnimationsAsync('noop'), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ collectionId }) } } },
        { provide: CatalogService, useValue: catalogService },
        { provide: CollectionService, useValue: collectionService },
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();
    catalogService.getCategories.mockReset(); catalogService.getCategories.mockReturnValue(of(categories));
    collectionService.getCollection.mockReset(); collectionService.getCollection.mockReturnValue(of(collection));
    collectionService.updateCollection.mockReset(); collectionService.updateCollection.mockReturnValue(of(collection));
    authService.currentUser.set({ id: 2, email: 'owner@example.com', displayName: 'Owner', preferredInterfaceLanguage: 'es', roles: ['USER'] });
  }

  afterEach(() => TestBed.resetTestingModule());

  it('loads an owned collection and sends empty description and category as clearing values', async () => {
    await configure();
    const fixture = TestBed.createComponent(CollectionEditComponent);
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.setValue({ name: '  Updated  ', description: '  ', visibility: 'PRIVATE', categoryCode: '' });

    component.submit();

    expect(component.categories()).toEqual(categories);
    expect(collectionService.updateCollection).toHaveBeenCalledWith(3, {
      name: 'Updated', description: '', visibility: 'PRIVATE', categoryCode: ''
    });
    expect(navigate).toHaveBeenCalledWith(['/collections', 3]);
  });

  it('rejects whitespace-only names and never sends an update', async () => {
    await configure();
    const fixture = TestBed.createComponent(CollectionEditComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.controls.name.setValue('  ');
    fixture.componentInstance.submit();

    expect(fixture.componentInstance.form.controls.name.invalid).toBe(true);
    expect(collectionService.updateCollection).not.toHaveBeenCalled();
  });

  it('does not render or submit the form for a public collection owned by another user, including ADMIN', async () => {
    await configure();
    authService.currentUser.set({ id: 7, email: 'admin@example.com', displayName: 'Admin', preferredInterfaceLanguage: 'es', roles: ['ADMIN'] });
    const fixture = TestBed.createComponent(CollectionEditComponent);
    fixture.detectChanges();
    fixture.detectChanges();

    expect(fixture.componentInstance.accessDenied()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).querySelector('form')).toBeNull();
    fixture.componentInstance.submit();
    expect(collectionService.updateCollection).not.toHaveBeenCalled();
  });

  it('handles invalid ids, load errors, save errors and duplicate saves', async () => {
    await configure('bad');
    let fixture = TestBed.createComponent(CollectionEditComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.errorMessage()).toBeTruthy();
    TestBed.resetTestingModule();

    await configure();
    collectionService.getCollection.mockReturnValueOnce(throwError(() => new Error('load failed')));
    fixture = TestBed.createComponent(CollectionEditComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.errorMessage()).toBeTruthy();
  });

  it('prevents duplicate saves and exposes a save error', async () => {
    await configure();
    const pending = new Subject<CollectionResponse>();
    collectionService.updateCollection.mockReturnValueOnce(pending);
    const fixture = TestBed.createComponent(CollectionEditComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.submit(); component.submit();
    expect(collectionService.updateCollection).toHaveBeenCalledTimes(1);
    pending.error(new Error('save failed'));
    expect(component.errorMessage()).toBeTruthy();
  });
});
