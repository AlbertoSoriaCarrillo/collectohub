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

  it('shows ordered creator credits with translated roles and labels', () => {
    const fixture = TestBed.createComponent(EditorialItemDetailComponent);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[data-testid="editorial-creators"]')).toBeTruthy();
    expect(element.textContent).toMatch(/Creditos|Credits/);
    expect(element.textContent).toMatch(/Autor|Author/);
    expect(element.textContent).toContain('Yasuhiro Nightow');
    expect(element.textContent).toContain('Original story');
  });

  it.each([[], undefined])('hides creator credits when creators are %s', (creators) => {
    service.getItemDetail.mockReturnValue(of({ ...detail, creators }));
    const fixture = TestBed.createComponent(EditorialItemDetailComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-testid="editorial-creators"]')).toBeNull();
  });

  it('shows editorial relationships with translated type, direction and description', () => {
    const fixture = TestBed.createComponent(EditorialItemDetailComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[data-testid="editorial-relationships"]')).toBeTruthy();
    expect(element.textContent).toMatch(/Relaciones|Relationships/);
    expect(element.textContent).toMatch(/Secuela|Sequel/);
    expect(element.textContent).toMatch(/Relacionada desde este item|Related from this item/);
    expect(element.textContent).toContain('Volume 2');
    expect(element.textContent).toContain('Continues the story');
  });

  it('links outgoing relationships to the target item', () => {
    const fixture = TestBed.createComponent(EditorialItemDetailComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('a[href="/catalog/editorial/items/4"]')).toBeTruthy();
  });

  it('links incoming relationships to the source item', () => {
    const fixture = TestBed.createComponent(EditorialItemDetailComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toMatch(/Misma obra|Same work/);
    expect(element.textContent).toMatch(/Relaciona hacia este item|Related to this item/);
    expect(element.querySelector('a[href="/catalog/editorial/items/5"]')).toBeTruthy();
  });

  it.each([[], undefined])('hides editorial relationships when relationships are %s', (relationships) => {
    service.getItemDetail.mockReturnValue(of({ ...detail, relationships }));
    const fixture = TestBed.createComponent(EditorialItemDetailComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-testid="editorial-relationships"]')).toBeNull();
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
  editions: [{ id: 3, catalogItemId: 2, catalogItemTitle: 'Volume 1', publisherId: 5, publisherName: 'Dark Horse', isbn: '9780000000001', ean: null, format: 'PAPERBACK', editionName: 'Deluxe paperback', publicationDate: null, publicationYear: 2004, language: 'en', country: 'US', pageCount: 240, coverImageUrl: null, recordStatus: 'ACTIVE', createdAt: '2026-01-01T00:00:00Z', updatedAt: null }],
  creators: [{ id: 10, creatorId: 20, creatorName: 'Yasuhiro Nightow', creatorSlug: 'yasuhiro-nightow', creditRole: 'AUTHOR', creditOrder: 1, creditLabel: 'Original story' }],
  relationships: [
    {
      id: 30,
      sourceCatalogItemId: 2,
      sourceCatalogItemTitle: 'Volume 1',
      sourceCatalogSeriesId: 1,
      sourceCatalogSeriesTitle: 'Trigun Maximum',
      targetCatalogItemId: 4,
      targetCatalogItemTitle: 'Volume 2',
      targetCatalogSeriesId: 1,
      targetCatalogSeriesTitle: 'Trigun Maximum',
      relationshipType: 'SEQUEL',
      relationshipOrder: 1,
      description: 'Continues the story',
      direction: 'OUTGOING'
    },
    {
      id: 31,
      sourceCatalogItemId: 5,
      sourceCatalogItemTitle: 'Original one-shot',
      sourceCatalogSeriesId: 6,
      sourceCatalogSeriesTitle: 'Trigun Origins',
      targetCatalogItemId: 2,
      targetCatalogItemTitle: 'Volume 1',
      targetCatalogSeriesId: 1,
      targetCatalogSeriesTitle: 'Trigun Maximum',
      relationshipType: 'SAME_WORK',
      relationshipOrder: 2,
      description: null,
      direction: 'INCOMING'
    }
  ]
};
