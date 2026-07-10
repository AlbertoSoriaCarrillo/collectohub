import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminEditionsComponent } from './admin-editions.component';

describe('AdminEditionsComponent', () => {
  const edition = {
    id: 11,
    catalogItemId: 10,
    catalogItemTitle: 'Chapter 1',
    publisherId: 2,
    publisherName: 'Kodansha',
    isbn: '123',
    ean: null,
    format: 'PAPERBACK' as const,
    editionName: 'First edition',
    publicationDate: null,
    publicationYear: 1999,
    language: 'ja',
    country: 'JP',
    pageCount: 200,
    coverImageUrl: 'https://example.test/cover.jpg',
    recordStatus: 'ACTIVE' as const,
    createdAt: '',
    updatedAt: null
  };
  const page = { content: [edition], page: 0, size: 20, totalElements: 1, totalPages: 1, first: true, last: true };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchEditions: vi.fn(() => of(page)),
      createEdition: vi.fn(() => of(edition)),
      updateEdition: vi.fn(() => of(edition)),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminEditionsComponent],
      providers: [provideAnimationsAsync('noop'), { provide: EditorialAdminService, useValue: mock }]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('does not load without itemId', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminEditionsComponent);
    fixture.detectChanges();

    expect(service.searchEditions).not.toHaveBeenCalled();
    expect(fixture.componentInstance.messageKey()).toBe('admin.editorial.messages.contextRequired');
  });

  it('loads and filters by recordStatus and format when itemId exists', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminEditionsComponent);
    const component = fixture.componentInstance;
    component.filters.patchValue({ itemId: 10, recordStatus: 'ACTIVE', format: 'PAPERBACK' });
    component.search();

    expect(component.editions()).toHaveLength(1);
    expect(service.searchEditions).toHaveBeenLastCalledWith(10, expect.objectContaining({
      recordStatus: 'ACTIVE',
      format: 'PAPERBACK',
      page: 0
    }));
  });

  it('creates and updates valid editions', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminEditionsComponent);
    const component = fixture.componentInstance;
    component.filters.patchValue({ itemId: 10 });
    component.startCreate();
    component.form.patchValue({
      editionName: 'New edition',
      country: 'jp',
      coverImageUrl: 'https://example.test/new.jpg',
      recordStatus: 'DRAFT'
    });
    component.submit();
    expect(service.createEdition).toHaveBeenCalledWith(10, expect.objectContaining({
      editionName: 'New edition',
      country: 'JP',
      coverImageUrl: 'https://example.test/new.jpg',
      recordStatus: 'DRAFT'
    }));

    component.startEdit(edition);
    component.form.patchValue({ editionName: 'Updated', recordStatus: 'ARCHIVED' });
    component.submit();
    expect(service.updateEdition).toHaveBeenCalledWith(11, expect.objectContaining({
      editionName: 'Updated',
      recordStatus: 'ARCHIVED'
    }));
  });

  it('validates cover URL, country and shows load and save errors', () => {
    const service = configure({
      searchEditions: vi.fn(() => throwError(() => new Error('load'))),
      createEdition: vi.fn(() => throwError(() => new Error('save')))
    });
    const fixture = TestBed.createComponent(AdminEditionsComponent);
    const component = fixture.componentInstance;
    component.filters.patchValue({ itemId: 10 });
    component.load();
    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');

    component.form.patchValue({ country: 'JPN', coverImageUrl: 'ftp://bad.test' });
    component.submit();
    expect(component.form.controls.country.hasError('pattern')).toBe(true);
    expect(component.form.controls.coverImageUrl.hasError('pattern')).toBe(true);

    component.form.patchValue({ country: 'JP', coverImageUrl: 'http://example.test/cover.jpg' });
    component.submit();
    expect(service.createEdition).toHaveBeenCalled();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');
  });
});
