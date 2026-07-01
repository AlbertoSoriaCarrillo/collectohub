import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { EditorialCatalogItemDetail } from '../../../core/models/editorial-catalog.model';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { EditorialItemDetailComponent } from './editorial-item-detail.component';

describe('EditorialItemDetailComponent', () => {
  const service = { getItemDetail: vi.fn() };

  beforeEach(async () => {
    service.getItemDetail.mockReset();
    service.getItemDetail.mockReturnValue(of(detail));
    await TestBed.configureTestingModule({
      imports: [EditorialItemDetailComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ itemId: '2' }) } } },
        { provide: EditorialCatalogService, useValue: service }
      ]
    }).compileComponents();
  });

  it('shows the item and its active editions', () => {
    const fixture = TestBed.createComponent(EditorialItemDetailComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[data-testid="editorial-item-detail"]')).toBeTruthy();
    expect(element.textContent).toContain('Volume 1');
    expect(element.textContent).toContain('Deluxe paperback');
    expect(element.querySelector('a[href="/catalog/editorial/editions/3"]')).toBeTruthy();
  });
});

const catalog = {
  series: { id: 1, franchiseId: null, franchiseName: null, primaryPublisherId: null, primaryPublisherName: null, title: 'Trigun Maximum', originalTitle: null, type: 'MANGA' as const, publicationStatus: 'COMPLETED', description: null, originCountry: 'JP', originalLanguage: 'ja', startYear: 1997, endYear: 2007, recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
  franchise: null,
  primaryPublisher: null
};
const detail: EditorialCatalogItemDetail = {
  catalog,
  item: { id: 2, seriesId: 1, seriesTitle: 'Trigun Maximum', title: 'Volume 1', originalTitle: null, sequenceLabel: '1', sortOrder: 1, description: null, firstPublicationDate: null, firstPublicationYear: 1997, originalLanguage: 'ja', originCountry: 'JP', recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null },
  editions: [{ id: 3, catalogItemId: 2, catalogItemTitle: 'Volume 1', publisherId: 5, publisherName: 'Dark Horse', isbn: '9780000000001', ean: null, format: 'PAPERBACK', editionName: 'Deluxe paperback', publicationDate: null, publicationYear: 2004, language: 'en', country: 'US', pageCount: 240, coverImageUrl: null, recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null }]
};
