import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminRelationshipsComponent } from './admin-relationships.component';

describe('AdminRelationshipsComponent', () => {
  const source = {
    id: 10,
    seriesId: 2,
    seriesTitle: 'Akira',
    title: 'Book 1',
    originalTitle: null,
    sequenceLabel: '1',
    sortOrder: 1,
    description: null,
    firstPublicationDate: null,
    firstPublicationYear: 1988,
    originalLanguage: 'ja',
    originCountry: 'JP',
    recordStatus: 'ACTIVE' as const,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: null
  };
  const target = { ...source, id: 11, title: 'Book 2', sequenceLabel: '2' };
  const page = { content: [source, target], page: 0, size: 10, totalElements: 2, totalPages: 1, first: true, last: true };
  const relationship = {
    id: 40,
    sourceCatalogItemId: 10,
    sourceCatalogItemTitle: 'Book 1',
    sourceCatalogSeriesId: 2,
    sourceCatalogSeriesTitle: 'Akira',
    targetCatalogItemId: 11,
    targetCatalogItemTitle: 'Book 2',
    targetCatalogSeriesId: 2,
    targetCatalogSeriesTitle: 'Akira',
    relationshipType: 'SEQUEL' as const,
    relationshipOrder: 1,
    description: 'Next volume',
    recordStatus: 'ACTIVE' as const,
    direction: 'OUTGOING' as const
  };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchItems: vi.fn(() => of(page)),
      getItemRelationships: vi.fn(() => of([relationship])),
      createItemRelationship: vi.fn(() => of(relationship)),
      updateItemRelationship: vi.fn(() => of(relationship)),
      deleteItemRelationship: vi.fn(() => of(void 0)),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminRelationshipsComponent],
      providers: [provideAnimationsAsync('noop'), { provide: EditorialAdminService, useValue: mock }]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('shows empty state without source item and searches/selects source', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminRelationshipsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.messageKey()).toBe('admin.editorial.relationships.noSource');

    component.sourceSearch.setValue({ seriesId: 2, q: 'akira' });
    component.searchSourceItems();
    component.selectSource(source);

    expect(service.searchItems).toHaveBeenCalledWith(2, { q: 'akira', page: 0, size: 10 });
    expect(service.getItemRelationships).toHaveBeenCalledWith(10, null);
    expect(component.relationships()).toEqual([relationship]);
  });

  it('renders existing relationships', () => {
    configure();
    const fixture = TestBed.createComponent(AdminRelationshipsComponent);
    const component = fixture.componentInstance;

    component.selectSource(source);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Book 1');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Book 2');
  });

  it('validates target and relationship order', () => {
    configure();
    const fixture = TestBed.createComponent(AdminRelationshipsComponent);
    const component = fixture.componentInstance;

    component.selectSource(source);
    component.form.patchValue({ targetCatalogItemId: null, relationshipOrder: 0 });
    component.submit();

    expect(component.form.controls.targetCatalogItemId.hasError('required')).toBe(true);
    expect(component.form.controls.relationshipOrder.hasError('min')).toBe(true);
  });

  it('creates and updates relationships', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminRelationshipsComponent);
    const component = fixture.componentInstance;

    component.selectSource(source);
    component.form.patchValue({
      targetCatalogItemId: 11,
      relationshipType: 'SEQUEL',
      relationshipOrder: 2,
      description: 'Next',
      recordStatus: 'ACTIVE'
    });
    component.submit();
    expect(service.createItemRelationship).toHaveBeenCalledWith(10, expect.objectContaining({
      targetCatalogItemId: 11,
      relationshipType: 'SEQUEL',
      relationshipOrder: 2
    }));

    component.startEdit(relationship);
    component.form.patchValue({ relationshipType: 'RELATED', relationshipOrder: 3 });
    component.submit();
    expect(service.updateItemRelationship).toHaveBeenCalledWith(10, 40, expect.objectContaining({
      relationshipType: 'RELATED',
      relationshipOrder: 3
    }));
  });

  it('deletes with confirmation and shows backend errors', () => {
    const service = configure({
      createItemRelationship: vi.fn(() => throwError(() => new Error('save'))),
      deleteItemRelationship: vi.fn(() => throwError(() => new Error('delete')))
    });
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminRelationshipsComponent);
    const component = fixture.componentInstance;

    component.selectSource(source);
    component.form.patchValue({ targetCatalogItemId: 11, relationshipOrder: 1 });
    component.submit();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');

    component.startEdit(relationship);
    component.deleteSelected();
    expect(service.deleteItemRelationship).toHaveBeenCalledWith(10, 40);
    expect(component.errorKey()).toBe('admin.editorial.messages.deleteError');
    confirmSpy.mockRestore();
  });
});
