import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CollectionSeriesProgressResponse } from '../../../core/models/collection.model';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionSeriesProgressComponent } from './collection-series-progress.component';

describe('CollectionSeriesProgressComponent', () => {
  const progress: CollectionSeriesProgressResponse = {
    collectionId: 100, seriesId: 500, seriesTitle: 'Dragon Ball', totalCatalogItems: 3,
    ownedItems: 1, wantedItems: 1, missingItems: 1, completionPercentage: 33,
    items: [
      { catalogItemId: 1, title: 'Volume 1', sequenceLabel: '1', sortOrder: 1, firstPublicationYear: 1984, calculatedStatus: 'OWNED', ownedCollectionItemIds: [301], wantedCollectionItemIds: [], selectedEditionIds: [601], legacyStatusWarning: false },
      { catalogItemId: 2, title: 'Volume 2', sequenceLabel: '2', sortOrder: 2, firstPublicationYear: null, calculatedStatus: 'WANTED', ownedCollectionItemIds: [], wantedCollectionItemIds: [302], selectedEditionIds: [], legacyStatusWarning: true },
      { catalogItemId: 3, title: 'Volume 3', sequenceLabel: null, sortOrder: 3, firstPublicationYear: null, calculatedStatus: 'MISSING', ownedCollectionItemIds: [], wantedCollectionItemIds: [], selectedEditionIds: [], legacyStatusWarning: false }
    ]
  };

  const collectionService = {
    getCollectionSeriesProgress: vi.fn(() => of(progress)),
    updateCollectionItem: vi.fn(() => of({ id: 302 }))
  };

  beforeEach(async () => {
    collectionService.getCollectionSeriesProgress.mockClear();
    collectionService.updateCollectionItem.mockClear();
    collectionService.getCollectionSeriesProgress.mockReturnValue(of(progress));
    await TestBed.configureTestingModule({
      imports: [CollectionSeriesProgressComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ collectionId: '100', seriesId: '500' }) } } },
        { provide: CollectionService, useValue: collectionService }
      ]
    }).compileComponents();
  });

  it('loads the exact progress resource and renders summary and groups', async () => {
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(collectionService.getCollectionSeriesProgress).toHaveBeenCalledWith(100, 500);
    const page = fixture.nativeElement as HTMLElement;
    expect(page.querySelector('[data-testid="series-progress-total"]')?.textContent).toContain('3');
    expect(page.querySelectorAll('[data-testid="progress-item-card"]')).toHaveLength(3);
    expect(page.querySelector('[data-testid="progress-item-legacy-warning"]')).not.toBeNull();
  });

  it('updates the single wanted entry and reloads canonical progress', () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    fixture.componentInstance.markWantedAsOwned(progress.items[1]);

    expect(confirmSpy).toHaveBeenCalledWith(expect.stringContaining('Volume 2'));
    expect(collectionService.updateCollectionItem).toHaveBeenCalledWith(100, 302, { collectionStatus: 'OWNED' });
    expect(collectionService.getCollectionSeriesProgress).toHaveBeenCalledTimes(2);
    confirmSpy.mockRestore();
  });

  it('does not update a wanted item without an editable entry id', () => {
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    fixture.componentInstance.markWantedAsOwned({ ...progress.items[1], wantedCollectionItemIds: [] });

    expect(collectionService.updateCollectionItem).not.toHaveBeenCalled();
  });

  it('keeps current progress when the transition fails', () => {
    collectionService.updateCollectionItem.mockReturnValueOnce(throwError(() => new Error('failed')));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    fixture.componentInstance.markWantedAsOwned(progress.items[1]);

    expect(fixture.componentInstance.progress()).toEqual(progress);
    expect(fixture.componentInstance.transitionErrorMessage()).toBeTruthy();
  });
});
