import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import {
  COLLECTION_ITEM_REFERENCE_KINDS,
  COLLECTION_ITEM_SORTS,
  CollectionItemListFilters,
  CollectionItemReferenceKind,
  CollectionItemResponse,
  CollectionItemSort,
  CollectionItemStatus,
  CollectionResponse,
  CollectionSeriesProgressSummaryResponse
} from '../../../core/models/collection.model';
import { CollectionService } from '../../../core/services/collection.service';

const FILTERABLE_STATUSES: CollectionItemStatus[] = [
  'OWNED',
  'WANTED',
  'MISSING',
  'DUPLICATED',
  'SELLABLE',
  'TRADABLE'
];

@Component({
  selector: 'app-collection-detail',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './collection-detail.component.html',
  styleUrl: './collection-detail.component.scss'
})
export class CollectionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly collectionService = inject(CollectionService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly collection = signal<CollectionResponse | null>(null);
  readonly items = signal<CollectionItemResponse[]>([]);
  readonly progress = signal<CollectionSeriesProgressSummaryResponse[]>([]);
  readonly isOwner = signal(false);
  readonly loading = signal(false);
  readonly itemsLoading = signal(false);
  readonly progressLoading = signal(false);
  readonly collectionError = signal<string | null>(null);
  readonly itemsError = signal<string | null>(null);
  readonly progressError = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);
  readonly activeFilters = signal<CollectionItemListFilters>({});
  readonly itemStatuses = FILTERABLE_STATUSES;
  readonly referenceKinds = COLLECTION_ITEM_REFERENCE_KINDS;
  readonly sorts = COLLECTION_ITEM_SORTS;

  readonly filterForm = new FormGroup({
    q: new FormControl('', { nonNullable: true }),
    status: new FormControl<CollectionItemStatus[]>([], { nonNullable: true }),
    referenceKind: new FormControl<CollectionItemReferenceKind[]>([], { nonNullable: true }),
    seriesId: new FormControl<number | null>(null),
    sort: new FormControl<CollectionItemSort>('CATALOG_ORDER', { nonNullable: true })
  });

  readonly persistedSummary = computed(() => {
    const snapshot = this.collection()?.items ?? [];
    const owned = snapshot.filter((item) => item.collectionStatus === 'OWNED').length;
    const wanted = snapshot.filter((item) => item.collectionStatus === 'WANTED').length;
    return {
      total: snapshot.length,
      owned,
      wanted,
      other: snapshot.length - owned - wanted,
      legacyMissing: snapshot.filter((item) => item.collectionStatus === 'MISSING').length
    };
  });

  readonly hasActiveFilters = computed(() => {
    const filters = this.activeFilters();
    return Boolean(
      filters.q ||
      filters.status?.length ||
      filters.referenceKind?.length ||
      filters.seriesId ||
      (filters.sort && filters.sort !== 'CATALOG_ORDER')
    );
  });

  ngOnInit(): void {
    const collectionId = Number(this.route.snapshot.paramMap.get('collectionId'));
    if (!Number.isFinite(collectionId) || collectionId <= 0) {
      this.collectionError.set(this.languageService.translate('collections.collectionNotFound'));
      return;
    }

    const filters = this.filtersFromQueryParams();
    this.activeFilters.set(filters);
    this.filterForm.patchValue({
      q: filters.q ?? '',
      status: filters.status ?? [],
      referenceKind: filters.referenceKind ?? [],
      seriesId: filters.seriesId ?? null,
      sort: filters.sort ?? 'CATALOG_ORDER'
    });
    this.loadCollection(collectionId);
  }

  applyFilters(): void {
    const collection = this.collection();
    if (!collection) {
      return;
    }
    const value = this.filterForm.getRawValue();
    const filters: CollectionItemListFilters = {
      q: value.q.trim() || null,
      status: value.status,
      referenceKind: value.referenceKind,
      seriesId: value.seriesId && value.seriesId > 0 ? value.seriesId : null,
      sort: value.sort
    };
    this.activeFilters.set(filters);
    void this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: this.queryParams(filters)
    });
    this.loadItems(collection.id);
  }

  clearFilters(): void {
    this.filterForm.reset({ q: '', status: [], referenceKind: [], seriesId: null, sort: 'CATALOG_ORDER' });
    this.applyFilters();
  }

  retryItems(): void {
    const collection = this.collection();
    if (collection) {
      this.loadItems(collection.id);
    }
  }

  retryProgress(): void {
    const collection = this.collection();
    if (collection && this.isOwner()) {
      this.loadProgress(collection.id);
    }
  }

  deleteItem(item: CollectionItemResponse): void {
    const collection = this.collection();
    if (
      !collection ||
      !window.confirm(
        this.languageService.translate('collections.itemDeleteConfirm', {
          name: this.itemTitle(item)
        })
      )
    ) {
      return;
    }

    this.actionError.set(null);
    this.collectionService.deleteCollectionItem(collection.id, item.id).subscribe({
      next: () => this.loadCollection(collection.id),
      error: (error) => this.actionError.set(this.errorMessageService.toMessage(error))
    });
  }

  itemTitle(item: CollectionItemResponse): string {
    return item.catalogItemTitle || item.manualTitle || item.masterProductName || this.languageService.translate('common.notReported');
  }

  referenceLabel(item: CollectionItemResponse): string {
    if (item.referenceKind) {
      const key = {
        DIRECT_CATALOG: 'collections.directCatalogReference',
        VERIFIED_BRIDGE: 'collections.verifiedBridge',
        LEGACY_UNRESOLVED: 'collections.legacyUnresolvedReference',
        MANUAL: 'collections.manualReference',
        INVALID_REFERENCE: 'collections.invalidReference'
      }[item.referenceKind];
      return this.languageService.translate(key);
    }
    const key = item.editorialReferenceSource === 'MANUAL'
      ? 'collections.manualReference'
      : item.editorialReferenceSource === 'VERIFIED_BRIDGE'
      ? 'collections.verifiedBridge'
      : item.editorialReferenceSource === 'MANUAL_EDITORIAL'
        ? 'collections.editorialItem'
        : 'collections.legacyReference';
    return this.languageService.translate(key);
  }

  private loadCollection(collectionId: number): void {
    this.loading.set(true);
    this.collectionError.set(null);
    this.actionError.set(null);
    this.collectionService
      .getCollection(collectionId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (collection) => {
          this.collection.set(collection);
          this.updateOwner(collection);
          this.loadItems(collection.id);
        },
        error: (error) => this.collectionError.set(this.errorMessageService.toMessage(error))
      });
  }

  private loadItems(collectionId: number): void {
    this.itemsLoading.set(true);
    this.itemsError.set(null);
    this.collectionService
      .getCollectionItems(collectionId, this.activeFilters())
      .pipe(finalize(() => this.itemsLoading.set(false)))
      .subscribe({
        next: (items) => this.items.set(items),
        error: (error) => this.itemsError.set(this.errorMessageService.toMessage(error))
      });
  }

  private loadProgress(collectionId: number): void {
    this.progressLoading.set(true);
    this.progressError.set(null);
    this.collectionService
      .getCollectionSeriesProgressSummary(collectionId)
      .pipe(finalize(() => this.progressLoading.set(false)))
      .subscribe({
        next: (progress) => this.progress.set(progress),
        error: (error) => this.progressError.set(this.errorMessageService.toMessage(error))
      });
  }

  private updateOwner(collection: CollectionResponse): void {
    const currentUser = this.authService.currentUser();
    if (currentUser) {
      const owner = currentUser.id === collection.userId;
      this.isOwner.set(owner);
      if (owner) {
        this.loadProgress(collection.id);
      }
      return;
    }

    if (!this.authService.hasToken()) {
      this.isOwner.set(false);
      return;
    }

    this.authService.getMe().subscribe({
      next: (user) => {
        const owner = user.id === collection.userId;
        this.isOwner.set(owner);
        if (owner) {
          this.loadProgress(collection.id);
        }
      },
      error: () => this.isOwner.set(false)
    });
  }

  private filtersFromQueryParams(): CollectionItemListFilters {
    const params = this.route.snapshot.queryParamMap;
    const statuses = params.getAll('status').filter((value): value is CollectionItemStatus =>
      FILTERABLE_STATUSES.includes(value as CollectionItemStatus)
    );
    const referenceKinds = params.getAll('referenceKind').filter((value): value is CollectionItemReferenceKind =>
      COLLECTION_ITEM_REFERENCE_KINDS.includes(value as CollectionItemReferenceKind)
    );
    const sortValue = params.get('sort') as CollectionItemSort | null;
    const seriesValue = Number(params.get('seriesId'));
    return {
      q: params.get('q')?.trim() || null,
      status: statuses,
      referenceKind: referenceKinds,
      seriesId: Number.isFinite(seriesValue) && seriesValue > 0 ? seriesValue : null,
      sort: sortValue && COLLECTION_ITEM_SORTS.includes(sortValue) ? sortValue : 'CATALOG_ORDER'
    };
  }

  private queryParams(filters: CollectionItemListFilters): Record<string, string | number | string[] | null> {
    return {
      q: filters.q || null,
      status: filters.status?.length ? filters.status : null,
      referenceKind: filters.referenceKind?.length ? filters.referenceKind : null,
      seriesId: filters.seriesId ?? null,
      sort: filters.sort && filters.sort !== 'CATALOG_ORDER' ? filters.sort : null
    };
  }
}
