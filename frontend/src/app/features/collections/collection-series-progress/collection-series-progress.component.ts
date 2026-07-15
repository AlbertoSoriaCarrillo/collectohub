import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import {
  CollectionSeriesProgressItemResponse,
  CollectionSeriesProgressResponse
} from '../../../core/models/collection.model';
import { CollectionService } from '../../../core/services/collection.service';

@Component({
  selector: 'app-collection-series-progress',
  imports: [
    RouterLink,
    NgTemplateOutlet,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatProgressBarModule,
    TranslatePipe
  ],
  templateUrl: './collection-series-progress.component.html',
  styleUrl: './collection-series-progress.component.scss'
})
export class CollectionSeriesProgressComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly collectionService = inject(CollectionService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);
  private readonly destroyRef = inject(DestroyRef);

  readonly collectionId = signal<number | null>(null);
  readonly seriesId = signal<number | null>(null);
  readonly progress = signal<CollectionSeriesProgressResponse | null>(null);
  readonly loading = signal(false);
  readonly transitioningCollectionItemId = signal<number | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly transitionErrorMessage = signal<string | null>(null);
  readonly transitionAppliedAwaitingReload = signal(false);

  readonly ownedItems = computed(() => this.groupedItems('OWNED'));
  readonly wantedItems = computed(() => this.groupedItems('WANTED'));
  readonly missingItems = computed(() => this.groupedItems('MISSING'));

  ngOnInit(): void {
    const collectionId = Number(this.route.snapshot.paramMap.get('collectionId'));
    const seriesId = Number(this.route.snapshot.paramMap.get('seriesId'));
    if (!this.isValidId(collectionId) || !this.isValidId(seriesId)) {
      this.errorMessage.set(this.languageService.translate('collections.seriesProgressLoadError'));
      return;
    }
    this.collectionId.set(collectionId);
    this.seriesId.set(seriesId);
    this.loadProgress();
  }

  loadProgress(afterTransition = false): void {
    const collectionId = this.collectionId();
    const seriesId = this.seriesId();
    if (!this.isValidId(collectionId) || !this.isValidId(seriesId)) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .getCollectionSeriesProgress(collectionId, seriesId)
      .pipe(finalize(() => this.loading.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (progress) => {
          this.progress.set(progress);
          this.errorMessage.set(null);
          this.transitionErrorMessage.set(null);
          this.transitionAppliedAwaitingReload.set(false);
        },
        error: (error) => {
          if (afterTransition) {
            this.transitionErrorMessage.set(
              this.languageService.translate('collections.seriesProgressUpdatedReloadFailed')
            );
            this.transitionAppliedAwaitingReload.set(true);
            return;
          }
          this.errorMessage.set(this.errorMessageService.toMessage(error));
        }
      });
  }

  retryLoadProgress(): void {
    if (this.loading()) {
      return;
    }
    this.loadProgress(this.transitionAppliedAwaitingReload());
  }

  markWantedAsOwned(item: CollectionSeriesProgressItemResponse): void {
    const collectionId = this.collectionId();
    const wantedId = item.wantedCollectionItemIds[0];
    if (
      !this.isValidId(collectionId) ||
      item.calculatedStatus !== 'WANTED' ||
      item.wantedCollectionItemIds.length !== 1 ||
      wantedId == null ||
      this.transitioningCollectionItemId() !== null ||
      this.transitionAppliedAwaitingReload() ||
      this.loading()
    ) {
      return;
    }

    const confirmed = window.confirm(
      this.languageService.translate('collections.seriesProgressConfirmOwned', { title: item.title })
    );
    if (!confirmed) {
      return;
    }

    this.transitionErrorMessage.set(null);
    this.transitioningCollectionItemId.set(wantedId);
    this.transitionAppliedAwaitingReload.set(false);
    this.collectionService
      .updateCollectionItem(collectionId, wantedId, { collectionStatus: 'OWNED' })
      .pipe(
        finalize(() => this.transitioningCollectionItemId.set(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.transitionAppliedAwaitingReload.set(true);
          this.loadProgress(true);
        },
        error: (error) => {
          this.transitionErrorMessage.set(this.errorMessageService.toMessage(error));
          this.transitionAppliedAwaitingReload.set(false);
        }
      });
  }

  itemEditLink(itemId: number): (string | number)[] {
    return ['/collections', this.collectionId()!, 'items', itemId, 'edit'];
  }

  private groupedItems(status: CollectionSeriesProgressItemResponse['calculatedStatus']): CollectionSeriesProgressItemResponse[] {
    return (this.progress()?.items ?? []).filter((item) => item.calculatedStatus === status);
  }

  private isValidId(value: number | null): value is number {
    return value !== null && Number.isFinite(value) && value > 0;
  }
}
