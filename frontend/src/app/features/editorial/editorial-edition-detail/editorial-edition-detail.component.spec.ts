import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { EditorialCatalogEditionDetail } from '../../../core/models/editorial-catalog.model';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { EditorialEditionDetailComponent } from './editorial-edition-detail.component';

describe('EditorialEditionDetailComponent', () => {
  const service = { getEditionDetail: vi.fn() };

  beforeEach(async () => {
    service.getEditionDetail.mockReset();
    service.getEditionDetail.mockReturnValue(of(detail));
    await TestBed.configureTestingModule({
      imports: [EditorialEditionDetailComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ editionId: '3' }) } } },
        { provide: EditorialCatalogService, useValue: service }
      ]
    }).compileComponents();
  });

  it('shows edition data and its editorial context', () => {
    const fixture = TestBed.createComponent(EditorialEditionDetailComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[data-testid="editorial-edition-detail"]')).toBeTruthy();
    expect(element.textContent).toContain('Deluxe paperback');
    expect(element.textContent).toContain('Dark Horse');
    expect(element.textContent).toContain('9780000000001');
    expect(element.textContent).toContain('Trigun Maximum');
  });
});

const detail: EditorialCatalogEditionDetail = {
  catalog: {
    series: { id: 1, franchiseId: 4, franchiseName: 'Trigun', primaryPublisherId: 5, primaryPublisherName: 'Dark Horse', title: 'Trigun Maximum', originalTitle: null, type: 'MANGA', publicationStatus: 'COMPLETED', description: null, originCountry: 'JP', originalLanguage: 'ja', startYear: 1997, endYear: 2007, recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
    franchise: { id: 4, name: 'Trigun', slug: 'trigun', description: null, recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
    primaryPublisher: null
  },
  item: { id: 2, seriesId: 1, seriesTitle: 'Trigun Maximum', title: 'Volume 1', originalTitle: null, sequenceLabel: '1', sortOrder: 1, description: null, firstPublicationDate: null, firstPublicationYear: 1997, originalLanguage: 'ja', originCountry: 'JP', recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
  edition: { id: 3, catalogItemId: 2, catalogItemTitle: 'Volume 1', publisherId: 5, publisherName: 'Dark Horse', isbn: '9780000000001', ean: null, format: 'PAPERBACK', editionName: 'Deluxe paperback', publicationDate: null, publicationYear: 2004, language: 'en', country: 'US', pageCount: 240, coverImageUrl: null, recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
  publisher: { id: 5, name: 'Dark Horse', country: 'US', recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null }
};
