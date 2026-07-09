import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminFranchisesComponent } from './admin-franchises.component';

describe('AdminFranchisesComponent', () => {
  const page = {
    content: [{ id: 2, name: 'Akira', slug: 'akira', description: null, recordStatus: 'ACTIVE' as const, createdAt: '', updatedAt: null }],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true
  };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchFranchises: vi.fn(() => of(page)),
      createFranchise: vi.fn(() => of(page.content[0])),
      updateFranchise: vi.fn(() => of(page.content[0])),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminFranchisesComponent],
      providers: [
        provideAnimationsAsync('noop'),
        { provide: EditorialAdminService, useValue: mock }
      ]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('loads the initial list and filters by status', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminFranchisesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.filters.setValue({ q: 'akira', recordStatus: 'ACTIVE' });
    component.search();

    expect(component.franchises()).toHaveLength(1);
    expect(service.searchFranchises).toHaveBeenLastCalledWith({
      q: 'akira',
      recordStatus: 'ACTIVE',
      page: 0
    });
  });

  it('validates slug format', () => {
    configure();
    const fixture = TestBed.createComponent(AdminFranchisesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.form.setValue({ name: 'Bad', slug: 'Bad Slug', description: '', recordStatus: 'DRAFT' });
    component.submit();

    expect(component.form.invalid).toBe(true);
    expect(component.form.controls.slug.hasError('pattern')).toBe(true);
  });

  it('creates and updates valid franchises', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminFranchisesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.startCreate();
    component.form.setValue({ name: 'Trigun', slug: 'trigun', description: '', recordStatus: 'DRAFT' });
    component.submit();
    expect(service.createFranchise).toHaveBeenCalledWith({
      name: 'Trigun',
      slug: 'trigun',
      description: null,
      recordStatus: 'DRAFT'
    });

    component.startEdit(page.content[0]);
    component.form.setValue({ name: 'Akira', slug: 'akira', description: 'Neo Tokyo', recordStatus: 'ARCHIVED' });
    component.submit();
    expect(service.updateFranchise).toHaveBeenCalledWith(2, {
      name: 'Akira',
      slug: 'akira',
      description: 'Neo Tokyo',
      recordStatus: 'ARCHIVED'
    });
  });

  it('shows load and save errors', () => {
    const service = configure({
      searchFranchises: vi.fn(() => throwError(() => new Error('load'))),
      createFranchise: vi.fn(() => throwError(() => new Error('save')))
    });
    const fixture = TestBed.createComponent(AdminFranchisesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');
    component.form.setValue({ name: 'Trigun', slug: 'trigun', description: '', recordStatus: 'ACTIVE' });
    component.submit();

    expect(service.createFranchise).toHaveBeenCalled();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');
  });
});
