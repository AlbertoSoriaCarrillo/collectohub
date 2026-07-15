import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
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

  it('does not request progress for invalid route identifiers', () => {
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.componentInstance.collectionId.set(null);
    fixture.componentInstance.seriesId.set(null);
    fixture.componentInstance.loadProgress();

    expect(collectionService.getCollectionSeriesProgress).not.toHaveBeenCalled();
  });

  it('shows a visible PUT error and allows a retry', () => {
    collectionService.updateCollectionItem.mockReturnValueOnce(throwError(() => new Error('update failed')));
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    fixture.componentInstance.markWantedAsOwned(progress.items[1]);
    fixture.detectChanges();

    expect(fixture.componentInstance.progress()).toEqual(progress);
    expect(fixture.componentInstance.transitionAppliedAwaitingReload()).toBe(false);
    expect(fixture.nativeElement.querySelector('[data-testid="progress-transition-error"]')).not.toBeNull();
    fixture.componentInstance.markWantedAsOwned(progress.items[1]);
    expect(collectionService.updateCollectionItem).toHaveBeenCalledTimes(2);
    confirmSpy.mockRestore();
  });

  it('keeps the old progress and requires a canonical retry when reload fails after PUT', () => {
    collectionService.getCollectionSeriesProgress
      .mockReturnValueOnce(of(progress))
      .mockReturnValueOnce(throwError(() => new Error('reload failed')));
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    fixture.componentInstance.markWantedAsOwned(progress.items[1]);
    fixture.detectChanges();

    expect(fixture.componentInstance.progress()).toEqual(progress);
    expect(fixture.componentInstance.transitionAppliedAwaitingReload()).toBe(true);
    expect(fixture.nativeElement.querySelector('[data-testid="progress-reload-required"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('[data-testid="progress-retry-load"]')).not.toBeNull();
    fixture.componentInstance.markWantedAsOwned(progress.items[1]);
    expect(collectionService.updateCollectionItem).toHaveBeenCalledTimes(1);
    confirmSpy.mockRestore();
  });

  it('retries only GET and unlocks transitions after a successful canonical reload', () => {
    const reloaded = { ...progress, ownedItems: 2, wantedItems: 0, items: [progress.items[0], { ...progress.items[1], calculatedStatus: 'OWNED' as const, ownedCollectionItemIds: [302], wantedCollectionItemIds: [] }, progress.items[2]] };
    collectionService.getCollectionSeriesProgress
      .mockReturnValueOnce(of(progress))
      .mockReturnValueOnce(throwError(() => new Error('reload failed')))
      .mockReturnValueOnce(of(reloaded));
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    fixture.componentInstance.markWantedAsOwned(progress.items[1]);
    fixture.componentInstance.retryLoadProgress();

    expect(collectionService.updateCollectionItem).toHaveBeenCalledTimes(1);
    expect(collectionService.getCollectionSeriesProgress).toHaveBeenCalledTimes(3);
    expect(fixture.componentInstance.progress()).toEqual(reloaded);
    expect(fixture.componentInstance.transitionAppliedAwaitingReload()).toBe(false);
    confirmSpy.mockRestore();
  });

  it('blocks concurrent reloads while a request is pending', () => {
    const pending = new Subject<CollectionSeriesProgressResponse>();
    collectionService.getCollectionSeriesProgress.mockReturnValueOnce(pending);
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    fixture.componentInstance.retryLoadProgress();

    expect(fixture.componentInstance.loading()).toBe(true);
    expect(collectionService.getCollectionSeriesProgress).toHaveBeenCalledTimes(1);
    pending.next(progress);
    pending.complete();
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('renders multiple wanted edit links in backend order without a direct transition', () => {
    const multipleWanted = { ...progress, items: [{ ...progress.items[1], wantedCollectionItemIds: [901, 902] }] };
    collectionService.getCollectionSeriesProgress.mockReturnValueOnce(of(multipleWanted));
    const fixture = TestBed.createComponent(CollectionSeriesProgressComponent);
    fixture.detectChanges();
    const page = fixture.nativeElement as HTMLElement;

    expect(page.querySelector('[data-testid="progress-mark-owned"]')).toBeNull();
    expect(page.querySelector('[data-testid="progress-multiple-wanted"]')).not.toBeNull();
    expect([...page.querySelectorAll('[data-testid="progress-edit-wanted-entry"]')].map((link) => link.getAttribute('ng-reflect-router-link'))).toHaveLength(2);
    expect(collectionService.updateCollectionItem).not.toHaveBeenCalled();
  });
});
