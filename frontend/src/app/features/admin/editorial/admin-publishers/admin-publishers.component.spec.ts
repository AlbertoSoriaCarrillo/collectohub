import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminPublishersComponent } from './admin-publishers.component';

describe('AdminPublishersComponent', () => {
  const page = {
    content: [{ id: 1, name: 'Panini', country: 'ES', recordStatus: 'ACTIVE' as const, createdAt: '', updatedAt: null }],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true
  };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchPublishers: vi.fn(() => of(page)),
      createPublisher: vi.fn(() => of(page.content[0])),
      updatePublisher: vi.fn(() => of(page.content[0])),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminPublishersComponent],
      providers: [
        provideAnimationsAsync('noop'),
        { provide: EditorialAdminService, useValue: mock }
      ]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('loads the initial list', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminPublishersComponent);
    fixture.detectChanges();

    expect(service.searchPublishers).toHaveBeenCalled();
    expect(fixture.componentInstance.publishers()).toHaveLength(1);
  });

  it('searches and filters by recordStatus', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminPublishersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.filters.setValue({ q: 'panini', recordStatus: 'ACTIVE' });
    component.search();

    expect(service.searchPublishers).toHaveBeenLastCalledWith({
      q: 'panini',
      recordStatus: 'ACTIVE',
      page: 0
    });
  });

  it('creates and updates valid publishers', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminPublishersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.startCreate();
    component.form.setValue({ name: 'Norma', country: 'es', recordStatus: 'DRAFT' });
    component.submit();
    expect(service.createPublisher).toHaveBeenCalledWith({
      name: 'Norma',
      country: 'ES',
      recordStatus: 'DRAFT'
    });

    component.startEdit(page.content[0]);
    component.form.setValue({ name: 'Panini Comics', country: '', recordStatus: 'ARCHIVED' });
    component.submit();
    expect(service.updatePublisher).toHaveBeenCalledWith(1, {
      name: 'Panini Comics',
      country: null,
      recordStatus: 'ARCHIVED'
    });
  });

  it('shows load and save errors', () => {
    const service = configure({
      searchPublishers: vi.fn(() => throwError(() => new Error('load'))),
      createPublisher: vi.fn(() => throwError(() => new Error('save')))
    });
    const fixture = TestBed.createComponent(AdminPublishersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');

    component.form.setValue({ name: 'Norma', country: 'ES', recordStatus: 'ACTIVE' });
    component.submit();

    expect(service.createPublisher).toHaveBeenCalled();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');
  });
});
