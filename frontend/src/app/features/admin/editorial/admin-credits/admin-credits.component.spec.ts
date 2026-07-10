import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminCreditsComponent } from './admin-credits.component';

describe('AdminCreditsComponent', () => {
  const credit = {
    id: 30,
    catalogItemId: 10,
    creatorId: 12,
    creatorName: 'Katsuhiro Otomo',
    creatorSlug: 'katsuhiro-otomo',
    creditRole: 'AUTHOR' as const,
    creditOrder: 1,
    creditLabel: null
  };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      listItemCreatorCredits: vi.fn(() => of([credit])),
      createItemCreatorCredit: vi.fn(() => of(credit)),
      updateItemCreatorCredit: vi.fn(() => of(credit)),
      deleteItemCreatorCredit: vi.fn(() => of(void 0)),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminCreditsComponent],
      providers: [provideAnimationsAsync('noop'), { provide: EditorialAdminService, useValue: mock }]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('does not load without itemId and loads when itemId exists', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminCreditsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(service.listItemCreatorCredits).not.toHaveBeenCalled();
    expect(component.messageKey()).toBe('admin.editorial.messages.contextRequired');

    component.contextForm.patchValue({ itemId: 10 });
    component.load();
    expect(component.credits()).toHaveLength(1);
    expect(service.listItemCreatorCredits).toHaveBeenCalledWith(10);
  });

  it('creates and updates valid credits', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminCreditsComponent);
    const component = fixture.componentInstance;
    component.contextForm.patchValue({ itemId: 10 });

    component.startCreate();
    component.form.setValue({ creatorId: 12, creditRole: 'AUTHOR', creditOrder: 1, creditLabel: '' });
    component.submit();
    expect(service.createItemCreatorCredit).toHaveBeenCalledWith(10, {
      creatorId: 12,
      creditRole: 'AUTHOR',
      creditOrder: 1,
      creditLabel: null
    });

    component.startEdit(credit);
    component.form.patchValue({ creditRole: 'ARTIST', creditOrder: 2, creditLabel: 'Pencils' });
    component.submit();
    expect(service.updateItemCreatorCredit).toHaveBeenCalledWith(10, 30, {
      creditRole: 'ARTIST',
      creditOrder: 2,
      creditLabel: 'Pencils'
    });
  });

  it('deletes with confirmation', () => {
    const service = configure();
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminCreditsComponent);
    const component = fixture.componentInstance;
    component.contextForm.patchValue({ itemId: 10 });

    component.startEdit(credit);
    component.deleteSelected();

    expect(confirmSpy).toHaveBeenCalled();
    expect(service.deleteItemCreatorCredit).toHaveBeenCalledWith(10, 30);
    confirmSpy.mockRestore();
  });

  it('shows errors and validates required creatorId and creditOrder', () => {
    const service = configure({
      listItemCreatorCredits: vi.fn(() => throwError(() => new Error('load'))),
      createItemCreatorCredit: vi.fn(() => throwError(() => new Error('save'))),
      deleteItemCreatorCredit: vi.fn(() => throwError(() => new Error('delete')))
    });
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminCreditsComponent);
    const component = fixture.componentInstance;
    component.contextForm.patchValue({ itemId: 10 });
    component.load();
    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');

    component.form.patchValue({ creatorId: null, creditOrder: 0 });
    component.submit();
    expect(component.form.controls.creatorId.hasError('required')).toBe(true);
    expect(component.form.controls.creditOrder.hasError('min')).toBe(true);

    component.form.patchValue({ creatorId: 12, creditOrder: 1 });
    component.submit();
    expect(service.createItemCreatorCredit).toHaveBeenCalled();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');

    component.startEdit(credit);
    expect(component.form.controls.creatorId.disabled).toBe(true);
    component.deleteSelected();
    expect(component.errorKey()).toBe('admin.editorial.messages.deleteError');
    confirmSpy.mockRestore();
  });
});
