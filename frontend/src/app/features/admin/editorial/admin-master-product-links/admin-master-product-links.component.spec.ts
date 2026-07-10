import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminMasterProductLinksComponent } from './admin-master-product-links.component';

describe('AdminMasterProductLinksComponent', () => {
  const link = {
    id: 4, masterProductId: 7, masterProductName: 'Akira Vol. 1',
    catalogItemId: 10, catalogItemTitle: 'Akira 1', catalogItemEditionId: 11,
    catalogItemEditionLabel: 'Paperback 2026', linkStatus: 'PROPOSED' as const,
    linkSource: 'ISBN' as const, confidenceScore: 0.9, matchReason: 'ISBN',
    reviewNote: null, createdAt: '2026-01-01T00:00:00Z', updatedAt: null
  };
  const page = { content: [link], page: 0, size: 20, totalElements: 1, totalPages: 1, first: true, last: true };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchMasterProductLinks: vi.fn(() => of(page)),
      getMasterProductLink: vi.fn(() => of(link)),
      createMasterProductLink: vi.fn(() => of(link)),
      updateMasterProductLink: vi.fn(() => of(link)),
      verifyMasterProductLink: vi.fn(() => of(link)),
      rejectMasterProductLink: vi.fn(() => of(link)),
      backfillMasterProductLinks: vi.fn(() => of({ scanned: 4, proposed: 2, skipped: 1, ambiguous: 1 })),
      getEditorialLegacyBridge: vi.fn(() => of({
        linkId: 4, masterProductId: 7, masterProductName: 'Akira Vol. 1',
        linkStatus: 'PROPOSED', linkSource: 'ISBN', confidenceScore: 0.9,
        matchReason: 'ISBN', catalogItemId: 10, catalogItemTitle: 'Akira 1',
        catalogItemEditionId: 11, catalogItemEditionLabel: 'Paperback 2026'
      })),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminMasterProductLinksComponent],
      providers: [provideAnimationsAsync('noop'), { provide: EditorialAdminService, useValue: mock }]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('loads links, shows them and filters by master product status and source', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminMasterProductLinksComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    component.filters.patchValue({ masterProductId: 7, linkStatus: 'PROPOSED', linkSource: 'ISBN' });
    component.loadLinks();

    expect(service.searchMasterProductLinks).toHaveBeenLastCalledWith(expect.objectContaining({
      masterProductId: 7, linkStatus: 'PROPOSED', linkSource: 'ISBN'
    }));
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Akira Vol. 1');
  });

  it('validates positive IDs and a confidence score between zero and one', () => {
    configure();
    const fixture = TestBed.createComponent(AdminMasterProductLinksComponent);
    const component = fixture.componentInstance;
    component.form.patchValue({ masterProductId: 0, catalogItemId: 0, confidenceScore: 1.1 });
    component.submit();

    expect(component.form.controls.masterProductId.hasError('min')).toBe(true);
    expect(component.form.controls.catalogItemId.hasError('min')).toBe(true);
    expect(component.form.controls.confidenceScore.hasError('max')).toBe(true);
  });

  it('creates and edits a link', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminMasterProductLinksComponent);
    const component = fixture.componentInstance;
    component.form.patchValue({ masterProductId: 7, catalogItemId: 10, confidenceScore: 0.8 });
    component.submit();
    expect(service.createMasterProductLink).toHaveBeenCalledWith(expect.objectContaining({ masterProductId: 7, catalogItemId: 10 }));

    component.startEdit(link);
    component.form.patchValue({ catalogItemId: 12 });
    component.submit();
    expect(service.updateMasterProductLink).toHaveBeenCalledWith(4, expect.objectContaining({ catalogItemId: 12 }));
  });

  it('verifies, rejects, backfills and looks up the bridge after confirmation', () => {
    const service = configure();
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminMasterProductLinksComponent);
    const component = fixture.componentInstance;
    component.verify(link);
    component.reject(link);
    component.backfill();
    component.filters.controls.masterProductId.setValue(7);
    component.lookupBridge();

    expect(service.verifyMasterProductLink).toHaveBeenCalledWith(4);
    expect(service.rejectMasterProductLink).toHaveBeenCalledWith(4);
    expect(service.backfillMasterProductLinks).toHaveBeenCalled();
    expect(component.backfillResult()?.proposed).toBe(2);
    expect(service.getEditorialLegacyBridge).toHaveBeenCalledWith(7);
    expect(component.bridge()?.linkId).toBe(4);
    confirmSpy.mockRestore();
  });

  it('shows backend errors', () => {
    configure({ searchMasterProductLinks: vi.fn(() => throwError(() => new Error('load'))) });
    const fixture = TestBed.createComponent(AdminMasterProductLinksComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');
  });
});
