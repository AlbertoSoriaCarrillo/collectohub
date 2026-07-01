import { convertToParamMap } from '@angular/router';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of } from 'rxjs';
import { EditorialCatalogSeriesDetail } from '../../../core/models/editorial-catalog.model';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { EditorialSeriesDetailComponent } from './editorial-series-detail.component';

describe('EditorialSeriesDetailComponent', () => {
  const service = { getSeriesDetail: vi.fn() };

  beforeEach(async () => {
    service.getSeriesDetail.mockReset();
    service.getSeriesDetail.mockReturnValue(of(detail));
    await TestBed.configureTestingModule({
      imports: [EditorialSeriesDetailComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ seriesId: '1' }) } } },
        { provide: EditorialCatalogService, useValue: service }
      ]
    }).compileComponents();
  });

  it('shows the series and its active items', () => {
    const fixture = TestBed.createComponent(EditorialSeriesDetailComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[data-testid="editorial-series-detail"]')).toBeTruthy();
    expect(element.textContent).toContain('Trigun Maximum');
    expect(element.textContent).toContain('Volume 1');
    expect(element.querySelector('a[href="/catalog/editorial/items/2"]')).toBeTruthy();
  });
});

const detail: EditorialCatalogSeriesDetail = {
  catalog: {
    series: {
      id: 1, franchiseId: 4, franchiseName: 'Trigun', primaryPublisherId: 5,
      primaryPublisherName: 'Dark Horse', title: 'Trigun Maximum', originalTitle: null,
      type: 'MANGA', publicationStatus: 'COMPLETED', description: 'A western manga.',
      originCountry: 'JP', originalLanguage: 'ja', startYear: 1997, endYear: 2007,
      recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null
    },
    franchise: { id: 4, name: 'Trigun', slug: 'trigun', description: null, recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
    primaryPublisher: { id: 5, name: 'Dark Horse', country: 'US', recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null }
  },
  items: [{
    catalog: undefined as never,
    item: { id: 2, seriesId: 1, seriesTitle: 'Trigun Maximum', title: 'Volume 1', originalTitle: null, sequenceLabel: '1', sortOrder: 1, description: null, firstPublicationDate: null, firstPublicationYear: 1997, originalLanguage: 'ja', originCountry: 'JP', recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
    editions: []
  }]
};
detail.items[0].catalog = detail.catalog;
