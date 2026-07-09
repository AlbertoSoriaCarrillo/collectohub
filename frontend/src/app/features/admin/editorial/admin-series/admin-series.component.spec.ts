import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminSeriesComponent } from './admin-series.component';

describe('AdminSeriesComponent', () => {
  const series = {
    id: 3,
    franchiseId: 2,
    franchiseName: 'Akira',
    primaryPublisherId: 1,
    primaryPublisherName: 'Kodansha',
    title: 'Akira',
    originalTitle: null,
    type: 'MANGA' as const,
    publicationStatus: 'COMPLETED' as const,
    description: null,
    originCountry: 'JP',
    originalLanguage: 'ja',
    startYear: 1982,
    endYear: 1990,
    recordStatus: 'ACTIVE' as const,
    createdAt: '',
    updatedAt: null
  };
  const page = {
    content: [series],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true
  };

  function configure(service: Partial<EditorialAdminService> = {}) {
    const mock = {
      searchSeries: vi.fn(() => of(page)),
      createSeries: vi.fn(() => of(series)),
      updateSeries: vi.fn(() => of(series)),
      ...service
    };
    TestBed.configureTestingModule({
      imports: [AdminSeriesComponent],
      providers: [
        provideAnimationsAsync('noop'),
        { provide: EditorialAdminService, useValue: mock }
      ]
    });
    return mock;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('loads, searches and filters by recordStatus, type and publicationStatus', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminSeriesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.filters.setValue({
      q: 'akira',
      recordStatus: 'ACTIVE',
      type: 'MANGA',
      publicationStatus: 'COMPLETED'
    });
    component.search();

    expect(component.series()).toHaveLength(1);
    expect(service.searchSeries).toHaveBeenLastCalledWith({
      q: 'akira',
      recordStatus: 'ACTIVE',
      type: 'MANGA',
      publicationStatus: 'COMPLETED',
      page: 0
    });
  });

  it('validates endYear greater than or equal to startYear', () => {
    configure();
    const fixture = TestBed.createComponent(AdminSeriesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.form.patchValue({ title: 'Bad years', startYear: 2000, endYear: 1999 });
    component.submit();

    expect(component.form.invalid).toBe(true);
    expect(component.form.hasError('yearRange')).toBe(true);
  });

  it('creates and updates valid series', () => {
    const service = configure();
    const fixture = TestBed.createComponent(AdminSeriesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.startCreate();
    component.form.setValue({
      franchiseId: null,
      primaryPublisherId: null,
      title: 'Trigun',
      originalTitle: '',
      type: 'MANGA',
      publicationStatus: 'COMPLETED',
      description: '',
      originCountry: 'jp',
      originalLanguage: 'ja',
      startYear: 1995,
      endYear: 1997,
      recordStatus: 'DRAFT'
    });
    component.submit();

    expect(service.createSeries).toHaveBeenCalledWith({
      franchiseId: null,
      primaryPublisherId: null,
      title: 'Trigun',
      originalTitle: null,
      type: 'MANGA',
      publicationStatus: 'COMPLETED',
      description: null,
      originCountry: 'JP',
      originalLanguage: 'ja',
      startYear: 1995,
      endYear: 1997,
      recordStatus: 'DRAFT'
    });

    component.startEdit(series);
    component.form.patchValue({ title: 'Akira updated', recordStatus: 'ARCHIVED' });
    component.submit();
    expect(service.updateSeries).toHaveBeenCalledWith(3, expect.objectContaining({
      title: 'Akira updated',
      recordStatus: 'ARCHIVED'
    }));
  });

  it('shows load and save errors', () => {
    const service = configure({
      searchSeries: vi.fn(() => throwError(() => new Error('load'))),
      createSeries: vi.fn(() => throwError(() => new Error('save')))
    });
    const fixture = TestBed.createComponent(AdminSeriesComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.errorKey()).toBe('admin.editorial.messages.loadError');
    component.form.patchValue({ title: 'Akira' });
    component.submit();

    expect(service.createSeries).toHaveBeenCalled();
    expect(component.errorKey()).toBe('admin.editorial.messages.saveError');
  });
});
