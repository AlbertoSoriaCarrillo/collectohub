import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { EditorialCatalogItemDetail, EditorialCatalogSearchItem } from '../../../core/models/editorial-catalog.model';
import { CollectionResponse } from '../../../core/models/collection.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { CollectionItemCreateComponent } from './collection-item-create.component';

describe('CollectionItemCreateComponent', () => {
  const collection: CollectionResponse = { id: 3, userId: 2, name: 'Manga', description: null, visibility: 'PRIVATE', categoryCode: null, categoryName: null, items: [] };
  const itemCandidate: EditorialCatalogSearchItem = { resultType: 'ITEM', seriesId: 1, seriesTitle: 'Series', itemId: 10, itemTitle: 'Item', editionId: null, editionName: null, publisherName: 'Publisher', franchiseName: null, type: 'MANGA', language: 'es', country: 'ES', publicationYear: 2024, coverImageUrl: null, linkedMasterProductId: 99, linkedMasterProductName: 'Linked' };
  const editionCandidate: EditorialCatalogSearchItem = { ...itemCandidate, resultType: 'EDITION', editionId: 20, editionName: 'Edition match' };
  const detail = {
    catalog: { series: { id: 1, title: 'Series' }, franchise: null, primaryPublisher: null },
    item: { id: 10, title: 'Item', sequenceLabel: '1' },
    editions: [{ id: 20, catalogItemId: 10, catalogItemTitle: 'Item', publisherId: null, publisherName: 'Publisher', isbn: '123', ean: null, format: 'PAPERBACK', editionName: 'Edition', publicationDate: null, publicationYear: 2024, language: 'es', country: 'ES', pageCount: null, coverImageUrl: null, recordStatus: 'ACTIVE', createdAt: '', updatedAt: null }]
  } as unknown as EditorialCatalogItemDetail;
  const authService = { currentUser: signal({ id: 2, email: 'owner@example.com', displayName: 'Owner', preferredInterfaceLanguage: 'es', roles: ['USER'] }), hasToken: vi.fn(() => true), getMe: vi.fn(() => of({ id: 2, email: 'owner@example.com', displayName: 'Owner', preferredInterfaceLanguage: 'es', roles: ['USER'] })) };
  const collectionService = { getCollection: vi.fn(() => of(collection)), addCollectionItem: vi.fn(() => of({})) };
  const editorialService = { search: vi.fn(() => of({ content: [itemCandidate, editionCandidate, { ...itemCandidate, resultType: 'SERIES', itemId: null }], page: 0, size: 30, totalElements: 3, totalPages: 1, first: true, last: true })), getItemDetail: vi.fn(() => of(detail)) };
  const catalogService = { searchMasterProducts: vi.fn(() => of([])) };

  async function configure(collectionId = '3'): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [CollectionItemCreateComponent],
      providers: [provideAnimationsAsync('noop'), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ collectionId }) } } },
        { provide: AuthService, useValue: authService }, { provide: CollectionService, useValue: collectionService },
        { provide: EditorialCatalogService, useValue: editorialService }, { provide: CatalogService, useValue: catalogService }]
    }).compileComponents();
    authService.currentUser.set({ id: 2, email: 'owner@example.com', displayName: 'Owner', preferredInterfaceLanguage: 'es', roles: ['USER'] });
    collectionService.getCollection.mockReset(); collectionService.getCollection.mockReturnValue(of(collection));
    collectionService.addCollectionItem.mockReset(); collectionService.addCollectionItem.mockReturnValue(of({}));
    editorialService.search.mockReset(); editorialService.search.mockReturnValue(of({ content: [itemCandidate, editionCandidate, { ...itemCandidate, resultType: 'SERIES', itemId: null }], page: 0, size: 30, totalElements: 3, totalPages: 1, first: true, last: true }));
    editorialService.getItemDetail.mockReset(); editorialService.getItemDetail.mockReturnValue(of(detail));
    catalogService.searchMasterProducts.mockReset(); catalogService.searchMasterProducts.mockReturnValue(of([]));
  }
  afterEach(() => TestBed.resetTestingModule());

  it('uses editorial mode by default and does not query legacy products on init', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges();
    expect(fixture.componentInstance.form.controls.referenceMode.value).toBe('EDITORIAL');
    expect(catalogService.searchMasterProducts).not.toHaveBeenCalled();
    expect(collectionService.getCollection).toHaveBeenCalledWith(3);
  });

  it('offers only writable collection statuses and never renders MISSING for new items', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges();
    const statuses = fixture.componentInstance.statuses;

    expect(statuses).toContain('OWNED');
    expect(statuses).toContain('WANTED');
    expect(statuses).toContain('DUPLICATED');
    expect(statuses).not.toContain('MISSING');
    expect(fixture.nativeElement.textContent).not.toContain('MISSING');
  });

  it('renders valid form structure and search buttons do not submit the item', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('form form')).toBeNull();
    expect(element.querySelectorAll('form')).toHaveLength(1);
    (element.querySelector('[data-testid="collection-item-editorial-search-submit"]') as HTMLButtonElement).click();
    expect(editorialService.search).toHaveBeenCalledTimes(1);
    expect(collectionService.addCollectionItem).not.toHaveBeenCalled();
  });

  it('rejects invalid collection ids and non-owners without searching or posting', async () => {
    await configure('bad'); let fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges();
    expect(fixture.componentInstance.errorMessage()).toBeTruthy();
    TestBed.resetTestingModule(); await configure(); authService.currentUser.set({ id: 7, email: 'admin@example.com', displayName: 'Admin', preferredInterfaceLanguage: 'es', roles: ['ADMIN'] });
    fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges(); fixture.componentInstance.searchEditorial(); fixture.componentInstance.submit();
    expect(fixture.componentInstance.accessDenied()).toBe(true);
    expect(editorialService.search).not.toHaveBeenCalled(); expect(collectionService.addCollectionItem).not.toHaveBeenCalled();
  });

  it('deduplicates ITEM and EDITION results, then loads detail and optional edition', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges(); const component = fixture.componentInstance;
    component.searchEditorial();
    expect(component.editorialResults()).toHaveLength(1);
    expect(component.editorialResults()[0].resultType).toBe('ITEM');
    component.selectCatalogItem(itemCandidate);
    expect(editorialService.getItemDetail).toHaveBeenCalledWith(10);
    expect(component.selectedCatalogItemDetail()).toBe(detail);
    component.selectEdition(20); expect(component.selectedEditionId()).toBe(20);
    component.selectCatalogItem(itemCandidate); expect(component.selectedEditionId()).toBeNull();
  });

  it('keeps only the newest item detail when A resolves after B', async () => {
    await configure();
    const detailA = new Subject<EditorialCatalogItemDetail>();
    const detailB = new Subject<EditorialCatalogItemDetail>();
    editorialService.getItemDetail.mockImplementation(((id: number) => id === 10 ? detailA : detailB) as never);
    const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges();
    const component = fixture.componentInstance;
    const candidateB = { ...itemCandidate, itemId: 11, itemTitle: 'Item B' };
    const detailForB = { ...detail, item: { ...detail.item, id: 11, title: 'Item B' } } as EditorialCatalogItemDetail;

    component.selectCatalogItem(itemCandidate);
    component.selectCatalogItem(candidateB);
    detailB.next(detailForB); detailB.complete();
    detailA.next(detail); detailA.complete();

    expect(component.selectedCatalogItemDetail()?.item.id).toBe(11);
    expect(component.detailLoading()).toBe(false);
  });

  it('ignores an old detail error after the newer item succeeds', async () => {
    await configure();
    const detailA = new Subject<EditorialCatalogItemDetail>();
    const detailB = new Subject<EditorialCatalogItemDetail>();
    editorialService.getItemDetail.mockImplementation(((id: number) => id === 10 ? detailA : detailB) as never);
    const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges();
    const component = fixture.componentInstance;
    const candidateB = { ...itemCandidate, itemId: 11, itemTitle: 'Item B' };
    const detailForB = { ...detail, item: { ...detail.item, id: 11, title: 'Item B' } } as EditorialCatalogItemDetail;

    component.selectCatalogItem(itemCandidate); component.selectCatalogItem(candidateB);
    detailB.next(detailForB); detailB.complete(); detailA.error(new Error('old error'));

    expect(component.selectedCatalogItemDetail()?.item.id).toBe(11);
    expect(component.errorMessage()).toBeNull();
  });

  it('invalidates a pending detail when switching to legacy mode', async () => {
    await configure(); const pending = new Subject<EditorialCatalogItemDetail>();
    editorialService.getItemDetail.mockReturnValueOnce(pending);
    const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges();
    const component = fixture.componentInstance;
    component.selectCatalogItem(itemCandidate); component.changeReferenceMode('LEGACY');
    pending.next(detail); pending.complete();

    expect(component.selectedCatalogItem()).toBeNull();
    expect(component.selectedCatalogItemDetail()).toBeNull();
    expect(component.detailLoading()).toBe(false);
  });

  it('ignores an editorial search resolved after switching to legacy', async () => {
    await configure(); const pending = new Subject<{ content: EditorialCatalogSearchItem[]; page: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean }>();
    editorialService.search.mockReturnValueOnce(pending);
    const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges(); const component = fixture.componentInstance;
    component.searchEditorial(); component.changeReferenceMode('LEGACY'); pending.next({ content: [itemCandidate], page: 0, size: 30, totalElements: 1, totalPages: 1, first: true, last: true }); pending.complete();
    expect(component.editorialResults()).toEqual([]); expect(component.editorialSearchPerformed()).toBe(false); expect(component.errorMessage()).toBeNull();
  });

  it('ignores a legacy search resolved after switching to editorial', async () => {
    await configure(); const pending = new Subject<[]>();
    catalogService.searchMasterProducts.mockReturnValueOnce(pending);
    const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges(); const component = fixture.componentInstance;
    component.changeReferenceMode('LEGACY'); component.searchProducts(); component.changeReferenceMode('EDITORIAL'); pending.next([]); pending.complete();
    expect(component.products()).toEqual([]); expect(component.form.controls.masterProductId.value).toBeNull();
  });

  it('sends canonical editorial payloads without a silent legacy bridge and navigates on success', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); const router = TestBed.inject(Router); const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true); fixture.detectChanges(); const component = fixture.componentInstance;
    component.selectCatalogItem(itemCandidate);
    component.form.patchValue({ collectionStatus: 'OWNED' }); component.submit();
    expect(collectionService.addCollectionItem).toHaveBeenLastCalledWith(3, expect.objectContaining({ masterProductId: null, catalogItemId: 10, catalogItemEditionId: null }));
    component.selectEdition(20); component.submit();
    expect(collectionService.addCollectionItem).toHaveBeenLastCalledWith(3, expect.objectContaining({ masterProductId: null, catalogItemId: 10, catalogItemEditionId: 20 }));
    expect(navigate).toHaveBeenCalledWith(['/collections', 3]);
  });

  it('cleans obsolete references when changing mode and supports legacy compatibility', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true); fixture.detectChanges(); const component = fixture.componentInstance;
    component.selectCatalogItem(itemCandidate); component.selectEdition(20); component.changeReferenceMode('LEGACY');
    expect(component.selectedCatalogItem()).toBeNull(); expect(component.selectedEditionId()).toBeNull();
    component.form.patchValue({ referenceMode: 'LEGACY', masterProductId: 5, collectionStatus: 'OWNED' }); component.submit();
    expect(collectionService.addCollectionItem).toHaveBeenCalledWith(3, expect.objectContaining({ masterProductId: 5, catalogItemId: null, catalogItemEditionId: null }));
  });

  it('creates a trimmed manual item without legacy or editorial references', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges(); const component = fixture.componentInstance;
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    component.selectCatalogItem(itemCandidate); component.selectEdition(20); component.changeReferenceMode('MANUAL');
    component.form.patchValue({ referenceMode: 'MANUAL', manualTitle: '  Convention item  ', manualDescription: '', manualType: '', collectionStatus: 'OWNED' });
    component.submit();
    expect(collectionService.addCollectionItem).toHaveBeenCalledWith(3, expect.objectContaining({
      masterProductId: null, catalogItemId: null, catalogItemEditionId: null,
      manualTitle: 'Convention item', manualDescription: null, manualType: null
    }));
  });

  it('rejects a manual title made only of spaces', async () => {
    await configure(); const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges(); const component = fixture.componentInstance;
    component.changeReferenceMode('MANUAL');
    component.form.patchValue({ referenceMode: 'MANUAL', manualTitle: '   ', collectionStatus: 'OWNED' });
    component.submit();
    expect(collectionService.addCollectionItem).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBeTruthy();
  });

  it('prevents duplicate submits and surfaces detail/search errors', async () => {
    await configure(); editorialService.getItemDetail.mockReturnValueOnce(throwError(() => new Error('detail'))); const fixture = TestBed.createComponent(CollectionItemCreateComponent); fixture.detectChanges(); const component = fixture.componentInstance;
    component.searchEditorial(); component.selectCatalogItem(itemCandidate); expect(component.errorMessage()).toBeTruthy();
    editorialService.search.mockReturnValueOnce(throwError(() => new Error('search'))); component.searchEditorial(); expect(component.errorMessage()).toBeTruthy();
    const pending = new Subject<{}>(); collectionService.addCollectionItem.mockReturnValueOnce(pending); editorialService.getItemDetail.mockReturnValue(of(detail)); component.selectCatalogItem(itemCandidate); component.form.patchValue({ collectionStatus: 'OWNED' }); component.submit(); component.submit(); expect(collectionService.addCollectionItem).toHaveBeenCalledTimes(1);
  });
});
