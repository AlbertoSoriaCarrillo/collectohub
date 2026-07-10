import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminItemsComponent } from './admin-items.component';

describe('AdminItemsComponent', () => {
  const item = {
    id: 10,
    seriesId: 4,
    seriesTitle: 'Akira',
    title: 'Chapter 1',
    originalTitle: null,
    sequenceLabel: '1',
    sortOrder: 1,
    description: null,
    firstPublicationDate: null,
    firstPublicationYear: 1988,
    originalLanguage: 'ja',
    originCountry: 'JP',
    recordStatus: 'ACTIVE' as const,
    createdAt: '',
    updatedAt: null
  };
  const page = { content: [item], page: 0, size: 20, totalElements: 1, totalPages: 1, first: true, last: true };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchItems: vi.fn(() => of(page)),
      createItem: vi.fn(() => of(item)),
      updateItem: vi.fn(() => of(item)),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminItemsComponent],
      providers: [provideAnimationsAsync('noop'), { provide: EditorialAdminService, useValue: mock }]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('does not load without seriesId', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminItemsComponent);
    fixture.detectChanges();

    expect(service.searchItems).not.toHaveBeenCalled();
    expect(fixture.componentInstance.messageKey()).toBe('admin.editorial.messages.contextRequired');
  });

  it('loads, searches and filters when seriesId exists', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminItemsComponent);
    const component = fixture.componentInstance;
    component.filters.patchValue({ seriesId: 4 });
    component.load();

    component.filters.patchValue({ q: 'chapter', recordStatus: 'ACTIVE', publicationYear: 1988, country: 'jp' });
    component.search();

    expect(component.items()).toHaveLength(1);
    expect(service.searchItems).toHaveBeenLastCalledWith(4, expect.objectContaining({
      q: 'chapter',
      recordStatus: 'ACTIVE',
      publicationYear: 1988,
      country: 'JP',
      page: 0
    }));
  });

  it('creates and updates valid items', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminItemsComponent);
    const component = fixture.componentInstance;
    component.filters.patchValue({ seriesId: 4 });
    component.startCreate();
    component.form.patchValue({ title: 'Chapter 2', originCountry: 'jp', recordStatus: 'DRAFT' });
    component.submit();
    expect(service.createItem).toHaveBeenCalledWith(4, expect.objectContaining({
      title: 'Chapter 2',
      originCountry: 'JP',
      recordStatus: 'DRAFT'
    }));

    component.startEdit(item);
    component.form.patchValue({ title: 'Chapter 1 updated', recordStatus: 'ARCHIVED' });
    component.submit();
    expect(service.updateItem).toHaveBeenCalledWith(10, expect.objectContaining({
      title: 'Chapter 1 updated',
      recordStatus: 'ARCHIVED'
    }));
  });

  it('validates country and shows load and save errors', () => {
    const service = configure({
      searchItems: vi.fn(() => throwError(() => new Error('load'))),
      createItem: vi.fn(() => throwError(() => new Error('save')))
    });
    const fixture = TestBed.createComponent(AdminItemsComponent);
    const component = fixture.componentInstance;
    component.filters.patchValue({ seriesId: 4 });
    component.load();
    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');

    component.form.patchValue({ title: 'Bad', originCountry: 'JPN' });
    component.submit();
    expect(component.form.controls.originCountry.hasError('pattern')).toBe(true);

    component.form.patchValue({ originCountry: 'JP' });
    component.submit();
    expect(service.createItem).toHaveBeenCalled();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');
  });
});
