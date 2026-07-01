import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { EditorialCatalogSearchItem, PageResponse } from '../../../core/models/editorial-catalog.model';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';
import { EditorialSearchComponent } from './editorial-search.component';

describe('EditorialSearchComponent', () => {
  const service = { search: vi.fn() };

  beforeEach(async () => {
    service.search.mockReset();
    service.search.mockReturnValue(of(page([])));
    await TestBed.configureTestingModule({
      imports: [EditorialSearchComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: EditorialCatalogService, useValue: service }
      ]
    }).compileComponents();
  });

  it('renders the empty state', () => {
    const fixture = TestBed.createComponent(EditorialSearchComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-testid="editorial-empty-state"]')).toBeTruthy();
  });

  it('shows editorial results', () => {
    service.search.mockReturnValue(of(page([result('SERIES', 1), result('ITEM', 2), result('EDITION', 3)])));
    const fixture = TestBed.createComponent(EditorialSearchComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('[data-result-type]').length).toBe(3);
    expect(fixture.nativeElement.textContent).toContain('Trigun');
    expect(fixture.nativeElement.textContent).toContain('Volume 1');
    expect(fixture.nativeElement.textContent).toContain('Paperback');
  });

  it('links series, items and editions to their public detail routes', () => {
    service.search.mockReturnValue(of(page([result('SERIES', 1), result('ITEM', 2), result('EDITION', 3)])));
    const fixture = TestBed.createComponent(EditorialSearchComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const links = Array.from(
      element.querySelectorAll<HTMLAnchorElement>('[data-result-type]')
    ).map((anchor) => anchor.getAttribute('href'));

    expect(links).toContain('/catalog/editorial/series/1');
    expect(links).toContain('/catalog/editorial/items/2');
    expect(links).toContain('/catalog/editorial/editions/3');
  });
});

function page(content: EditorialCatalogSearchItem[]): PageResponse<EditorialCatalogSearchItem> {
  return { content, page: 0, size: 12, totalElements: content.length, totalPages: content.length ? 1 : 0, first: true, last: true };
}

function result(type: 'SERIES' | 'ITEM' | 'EDITION', id: number): EditorialCatalogSearchItem {
  return {
    resultType: type,
    seriesId: type === 'SERIES' ? id : 1,
    seriesTitle: 'Trigun',
    itemId: type === 'ITEM' ? id : type === 'EDITION' ? 2 : null,
    itemTitle: type === 'SERIES' ? null : 'Volume 1',
    editionId: type === 'EDITION' ? id : null,
    editionName: type === 'EDITION' ? 'Paperback' : null,
    publisherName: 'Dark Horse',
    franchiseName: 'Trigun',
    type: 'MANGA',
    language: 'en',
    country: 'US',
    publicationYear: 2004,
    coverImageUrl: null,
    linkedMasterProductId: null,
    linkedMasterProductName: null
  };
}
