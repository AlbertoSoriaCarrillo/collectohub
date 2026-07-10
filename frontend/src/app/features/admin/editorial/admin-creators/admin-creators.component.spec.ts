import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminCreatorsComponent } from './admin-creators.component';

describe('AdminCreatorsComponent', () => {
  const creator = {
    id: 12,
    name: 'Katsuhiro Otomo',
    slug: 'katsuhiro-otomo',
    sortName: 'Otomo, Katsuhiro',
    biography: null,
    country: 'JP',
    birthYear: 1954,
    deathYear: null,
    recordStatus: 'ACTIVE' as const
  };
  const page = { content: [creator], page: 0, size: 20, totalElements: 1, totalPages: 1, first: true, last: true };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchCreators: vi.fn(() => of(page)),
      createCreator: vi.fn(() => of(creator)),
      updateCreator: vi.fn(() => of(creator)),
      deleteCreator: vi.fn(() => of(void 0)),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminCreatorsComponent],
      providers: [provideAnimationsAsync('noop'), { provide: EditorialAdminService, useValue: mock }]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('loads, searches and filters creators', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminCreatorsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.filters.setValue({ q: 'otomo', recordStatus: 'ACTIVE' });
    component.search();

    expect(component.creators()).toHaveLength(1);
    expect(service.searchCreators).toHaveBeenLastCalledWith({ q: 'otomo', recordStatus: 'ACTIVE', page: 0 });
  });

  it('creates and updates valid creators', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminCreatorsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.startCreate();
    component.form.patchValue({ name: 'Naoki Urasawa', slug: 'naoki-urasawa', country: 'jp', recordStatus: 'DRAFT' });
    component.submit();
    expect(service.createCreator).toHaveBeenCalledWith(expect.objectContaining({
      name: 'Naoki Urasawa',
      slug: 'naoki-urasawa',
      country: 'JP',
      recordStatus: 'DRAFT'
    }));

    component.startEdit(creator);
    component.form.patchValue({ name: 'Otomo', recordStatus: 'ARCHIVED' });
    component.submit();
    expect(service.updateCreator).toHaveBeenCalledWith(12, expect.objectContaining({
      name: 'Otomo',
      recordStatus: 'ARCHIVED'
    }));
  });

  it('deletes with confirmation', () => {
    const service = configure();
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminCreatorsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.startEdit(creator);
    component.deleteSelected();

    expect(confirmSpy).toHaveBeenCalled();
    expect(service.deleteCreator).toHaveBeenCalledWith(12);
    confirmSpy.mockRestore();
  });

  it('shows load, save and delete errors and validates fields', () => {
    const service = configure({
      searchCreators: vi.fn(() => throwError(() => new Error('load'))),
      createCreator: vi.fn(() => throwError(() => new Error('save'))),
      deleteCreator: vi.fn(() => throwError(() => new Error('delete')))
    });
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminCreatorsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');

    component.form.patchValue({ name: 'Bad', country: 'JPN', birthYear: 2000, deathYear: 1990 });
    component.submit();
    expect(component.form.controls.country.hasError('pattern')).toBe(true);
    expect(component.form.hasError('yearRange')).toBe(true);

    component.form.patchValue({ country: 'JP', deathYear: 2020 });
    component.submit();
    expect(service.createCreator).toHaveBeenCalled();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');

    component.startEdit(creator);
    component.deleteSelected();
    expect(component.errorKey()).toBe('admin.editorial.messages.deleteError');
    confirmSpy.mockRestore();
  });
});
