import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { AuthService } from '../../../core/auth/auth.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { MasterProductResponse } from '../../../core/models/catalog.model';
import { EditorialCatalogItemDetail, EditorialCatalogSearchItem } from '../../../core/models/editorial-catalog.model';
import {
  COLLECTION_ITEM_STATUSES,
  CollectionItemResponse,
  UpdateCollectionItemRequest
} from '../../../core/models/collection.model';
import { PHYSICAL_CONDITIONS } from '../../../core/models/inventory.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';

@Component({
  selector: 'app-collection-item-edit',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './collection-item-edit.component.html',
  styleUrl: './collection-item-edit.component.scss'
})
export class CollectionItemEditComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly catalogService = inject(CatalogService);
  private readonly collectionService = inject(CollectionService);
  private readonly editorialCatalogService = inject(EditorialCatalogService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);
  private readonly destroyRef = inject(DestroyRef);
  private manualLinkSearchRequestId = 0;
  private manualLinkDetailRequestId = 0;

  readonly statuses = COLLECTION_ITEM_STATUSES;
  readonly conditions = PHYSICAL_CONDITIONS;
  readonly collectionId = signal<number | null>(null);
  readonly itemId = signal<number | null>(null);
  readonly item = signal<CollectionItemResponse | null>(null);
  readonly loading = signal(false);
  readonly ownerLoading = signal(false);
  readonly isOwner = signal(false);
  readonly accessDenied = signal(false);
  readonly saving = signal(false);
  readonly searching = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly products = signal<MasterProductResponse[]>([]);
  readonly editorialResults = signal<EditorialCatalogSearchItem[]>([]);
  readonly selectedEditorial = signal<EditorialCatalogSearchItem | null>(null);
  readonly manualLinkPanelOpen = signal(false);
  readonly manualLinkSearching = signal(false);
  readonly manualLinkSearchPerformed = signal(false);
  readonly manualLinkDetailLoading = signal(false);
  readonly manualLinkSubmitting = signal(false);
  readonly manualLinkResults = signal<EditorialCatalogSearchItem[]>([]);
  readonly selectedManualLinkItem = signal<EditorialCatalogSearchItem | null>(null);
  readonly selectedManualLinkDetail = signal<EditorialCatalogItemDetail | null>(null);
  readonly selectedManualLinkEditionId = signal<number | null>(null);
  readonly manualLinkErrorMessage = signal<string | null>(null);
  readonly productSearch = this.fb.nonNullable.group({ name: [''] });
  readonly editorialSearch = this.fb.nonNullable.group({ q: [''] });
  readonly manualLinkSearch = this.fb.nonNullable.group({ q: [''] });
  readonly form = this.fb.group({
    referenceMode: ['LEGACY' as 'LEGACY' | 'EDITORIAL' | 'MANUAL'],
    masterProductId: [null as number | null],
    manualTitle: ['', [Validators.maxLength(160)]],
    manualDescription: ['', [Validators.maxLength(4000)]],
    manualType: ['', [Validators.maxLength(80)]],
    collectionStatus: ['', [Validators.required]],
    physicalCondition: [''],
    unitNumber: ['', [Validators.maxLength(50)]],
    totalLimitedUnits: [null as number | null, [Validators.min(1)]],
    notes: ['', [Validators.maxLength(4000)]],
    acquiredAt: ['']
  });

  searchProducts(): void {
    this.searching.set(true);
    this.catalogService
      .searchMasterProducts({
        name: this.productSearch.controls.name.value,
        status: 'ACTIVE'
      })
      .pipe(finalize(() => this.searching.set(false)))
      .subscribe({
        next: (products) => this.products.set(products),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  searchEditorial(): void {
    this.searching.set(true);
    this.editorialCatalogService.search({ q: this.editorialSearch.controls.q.value, size: 30 })
      .pipe(finalize(() => this.searching.set(false)))
      .subscribe({
        next: (page) => this.editorialResults.set(
          page.content.filter((result) => result.resultType === 'ITEM' || result.resultType === 'EDITION')
        ),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  selectEditorial(result: EditorialCatalogSearchItem): void {
    if (result.resultType === 'SERIES' || result.itemId == null) {
      this.errorMessage.set(this.languageService.translate('collections.seriesNotAllowed'));
      return;
    }
    this.selectedEditorial.set(result);
  }

  ngOnInit(): void {
    const collectionId = Number(this.route.snapshot.paramMap.get('collectionId'));
    const itemId = Number(this.route.snapshot.paramMap.get('itemId'));
    if (
      !Number.isFinite(collectionId) ||
      collectionId <= 0 ||
      !Number.isFinite(itemId) ||
      itemId <= 0
    ) {
      this.errorMessage.set(this.languageService.translate('collections.itemNotFound'));
      return;
    }

    this.collectionId.set(collectionId);
    this.itemId.set(itemId);
    this.loadCollectionAndOwnership(collectionId, itemId);
  }

  submit(): void {
    const collectionId = this.collectionId();
    const itemId = this.itemId();
    if (!collectionId || !itemId) {
      this.errorMessage.set(this.languageService.translate('collections.itemNotFound'));
      return;
    }

    if (!this.isOwner() || this.saving()) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    if (value.referenceMode === 'MANUAL' && !this.optionalText(value.manualTitle)) {
      this.form.controls.manualTitle.markAsTouched();
      this.errorMessage.set(this.languageService.translate('collections.manualTitleRequired'));
      return;
    }
    if (value.referenceMode === 'LEGACY' && !value.masterProductId) {
      this.errorMessage.set(this.languageService.translate('collections.legacyReferenceRequired'));
      return;
    }
    if (
      value.referenceMode === 'EDITORIAL' &&
      !this.selectedEditorial() &&
      !this.item()?.catalogItemId
    ) {
      this.errorMessage.set(this.languageService.translate('collections.editorialReferenceRequired'));
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .updateCollectionItem(collectionId, itemId, this.toRequest())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/collections', collectionId]),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  openManualCatalogLink(): void {
    if (!this.isOwner() || !this.item() || !this.isManualItem(this.item()!) || this.saving() || this.manualLinkSubmitting()) return;
    if (this.form.dirty) { this.manualLinkErrorMessage.set(this.languageService.translate('collections.saveBeforeCatalogLink')); return; }
    this.manualLinkPanelOpen.set(true);
    this.manualLinkErrorMessage.set(null);
  }

  closeManualCatalogLink(): void {
    if (this.manualLinkSubmitting()) return;
    this.resetManualCatalogLinkState();
  }

  private resetManualCatalogLinkState(): void {
    this.manualLinkSearchRequestId++;
    this.manualLinkDetailRequestId++;
    this.manualLinkPanelOpen.set(false);
    this.manualLinkSearching.set(false);
    this.manualLinkDetailLoading.set(false);
    this.manualLinkSearchPerformed.set(false);
    this.manualLinkSearch.reset({ q: '' });
    this.manualLinkResults.set([]);
    this.selectedManualLinkItem.set(null);
    this.selectedManualLinkDetail.set(null);
    this.selectedManualLinkEditionId.set(null);
    this.manualLinkErrorMessage.set(null);
  }

  searchCatalogForManualLink(): void {
    if (!this.isOwner() || !this.manualLinkPanelOpen() || !this.item() || !this.isManualItem(this.item()!) || this.manualLinkSearching() || this.manualLinkSubmitting()) return;
    const requestId = ++this.manualLinkSearchRequestId;
    this.clearManualLinkSelection();
    this.manualLinkSearchPerformed.set(true);
    this.manualLinkResults.set([]);
    this.manualLinkErrorMessage.set(null);
    this.manualLinkSearching.set(true);
    this.editorialCatalogService.search({ q: this.manualLinkSearch.controls.q.value.trim(), size: 30 })
      .pipe(finalize(() => { if (requestId === this.manualLinkSearchRequestId) this.manualLinkSearching.set(false); }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => { if (requestId === this.manualLinkSearchRequestId && this.manualLinkPanelOpen()) this.manualLinkResults.set(this.toManualLinkCandidates(page.content)); },
        error: (error) => { if (requestId === this.manualLinkSearchRequestId && this.manualLinkPanelOpen()) this.manualLinkErrorMessage.set(this.errorMessageService.toMessage(error)); }
      });
  }

  selectManualLinkCatalogItem(candidate: EditorialCatalogSearchItem): void {
    if (!this.isOwner() || !this.manualLinkPanelOpen() || !this.item() || !this.isManualItem(this.item()!) || candidate.itemId == null || this.manualLinkSearching() || this.manualLinkSubmitting()) return;
    this.clearManualLinkSelection();
    const requestId = ++this.manualLinkDetailRequestId;
    this.selectedManualLinkItem.set(candidate);
    this.manualLinkErrorMessage.set(null);
    this.manualLinkDetailLoading.set(true);
    this.editorialCatalogService.getItemDetail(candidate.itemId)
      .pipe(finalize(() => { if (requestId === this.manualLinkDetailRequestId) this.manualLinkDetailLoading.set(false); }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (detail) => { if (requestId === this.manualLinkDetailRequestId && this.selectedManualLinkItem()?.itemId === detail.item.id) this.selectedManualLinkDetail.set(detail); },
        error: (error) => { if (requestId === this.manualLinkDetailRequestId) { this.selectedManualLinkItem.set(null); this.manualLinkErrorMessage.set(this.errorMessageService.toMessage(error)); } }
      });
  }

  selectManualLinkEdition(editionId: number | null): void {
    const editions = this.selectedManualLinkDetail()?.editions ?? [];
    this.selectedManualLinkEditionId.set(editionId !== null && editions.some((edition) => edition.id === editionId) ? editionId : null);
  }

  linkManualItemToCatalog(): void {
    const collectionId = this.collectionId();
    const item = this.item();
    const detail = this.selectedManualLinkDetail();
    if (!collectionId || !item || !detail || !this.isOwner() || !this.isManualItem(item) || !this.manualLinkPanelOpen() || this.selectedManualLinkItem()?.itemId !== detail.item.id || this.form.dirty || this.manualLinkDetailLoading() || this.saving() || this.manualLinkSubmitting()) {
      if (this.form.dirty) this.manualLinkErrorMessage.set(this.languageService.translate('collections.saveBeforeCatalogLink'));
      return;
    }
    const editionId = this.selectedManualLinkEditionId();
    const edition = editionId === null ? null : detail.editions.find((candidate) => candidate.id === editionId);
    if (editionId !== null && !edition) { this.selectedManualLinkEditionId.set(null); return; }
    const message = [
      this.languageService.translate('collections.confirmManualCatalogLink'),
      detail.item.title + (edition ? ` - ${edition.editionName ?? edition.format}` : ` - ${this.languageService.translate('collections.noSpecificEdition')}`),
      this.languageService.translate('collections.linkManualIdentityWarning'),
      this.languageService.translate('collections.linkManualPersonalDataPreserved')
    ].join('\n');
    if (!window.confirm(message)) return;
    this.manualLinkSubmitting.set(true);
    this.manualLinkErrorMessage.set(null);
    this.collectionService.linkManualCollectionItemToCatalog(collectionId, item.id, { catalogItemId: detail.item.id, catalogItemEditionId: editionId })
      .pipe(finalize(() => this.manualLinkSubmitting.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (response) => { this.item.set(response); this.manualLinkSubmitting.set(false); this.resetManualCatalogLinkState(); void this.router.navigate(['/collections', collectionId]); }, error: (error) => this.manualLinkErrorMessage.set(this.errorMessageService.toMessage(error)) });
  }

  private loadCollectionAndOwnership(collectionId: number, itemId: number): void {
    this.loading.set(true);
    this.collectionService.getCollection(collectionId).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (collection) => this.resolveOwnership(collection.userId, itemId),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  private resolveOwnership(ownerId: number, itemId: number): void {
    const currentUser = this.authService.currentUser();
    if (currentUser) { this.setOwnership(currentUser.id, ownerId, itemId); return; }
    if (!this.authService.hasToken()) { this.accessDenied.set(true); return; }
    this.ownerLoading.set(true);
    this.authService.getMe().pipe(finalize(() => this.ownerLoading.set(false))).subscribe({
      next: (user) => this.setOwnership(user.id, ownerId, itemId),
      error: () => this.accessDenied.set(true)
    });
  }

  private setOwnership(userId: number, ownerId: number, itemId: number): void {
    const owner = userId === ownerId;
    this.isOwner.set(owner);
    this.accessDenied.set(!owner);
    if (owner) this.loadItem(this.collectionId()!, itemId);
  }

  private loadItem(collectionId: number, itemId: number): void {
    this.loading.set(true);
    this.collectionService
      .getCollectionItems(collectionId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (items) => {
          const item = items.find((candidate) => candidate.id === itemId);
          if (!item) {
            this.errorMessage.set(this.languageService.translate('collections.itemNotFound'));
            return;
          }

          this.item.set(item);
          this.form.patchValue({
            referenceMode: this.isManualItem(item) ? 'MANUAL' : item.catalogItemId ? 'EDITORIAL' : 'LEGACY',
            masterProductId: item.masterProductId,
            manualTitle: item.manualTitle ?? '',
            manualDescription: item.manualDescription ?? '',
            manualType: item.manualType ?? '',
            collectionStatus: item.collectionStatus,
            physicalCondition: item.physicalCondition ?? '',
            unitNumber: item.unitNumber ?? '',
            totalLimitedUnits: item.totalLimitedUnits,
            notes: item.notes ?? '',
            acquiredAt: item.acquiredAt ?? ''
          });
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): UpdateCollectionItemRequest {
    const value = this.form.getRawValue();
    const editorial = value.referenceMode === 'EDITORIAL' ? this.selectedEditorial() : null;
    const reference = value.referenceMode === 'MANUAL'
      ? {
          manualTitle: this.normalizedEditableText(value.manualTitle),
          manualDescription: this.normalizedEditableText(value.manualDescription),
          manualType: this.normalizedEditableText(value.manualType)
        }
      : value.referenceMode === 'LEGACY'
      ? {
          masterProductId: Number(value.masterProductId),
          catalogItemId: null,
          catalogItemEditionId: null
        }
      : editorial
        ? {
            masterProductId: null,
            catalogItemId: editorial.itemId,
            catalogItemEditionId: editorial.editionId
          }
        : {};
    return {
      ...reference,
      collectionStatus: value.collectionStatus as UpdateCollectionItemRequest['collectionStatus'],
      physicalCondition:
        (this.optionalText(value.physicalCondition) as UpdateCollectionItemRequest['physicalCondition']) ??
        null,
      unitNumber: this.optionalText(value.unitNumber),
      totalLimitedUnits: value.totalLimitedUnits ? Number(value.totalLimitedUnits) : null,
      notes: this.optionalText(value.notes),
      acquiredAt: this.optionalText(value.acquiredAt)
    };
  }

  private optionalText(value: string | null | undefined): string | null {
    const normalized = (value ?? '').trim();
    return normalized ? normalized : null;
  }

  private normalizedEditableText(value: string | null | undefined): string {
    return (value ?? '').trim();
  }

  isManualItem(item: CollectionItemResponse): boolean {
    return item.referenceKind === 'MANUAL' || item.editorialReferenceSource === 'MANUAL';
  }

  private toManualLinkCandidates(results: EditorialCatalogSearchItem[]): EditorialCatalogSearchItem[] {
    const candidates = new Map<number, EditorialCatalogSearchItem>();
    results.filter((result) => result.resultType !== 'SERIES' && result.itemId !== null).forEach((result) => {
      const existing = candidates.get(result.itemId!);
      if (!existing || result.resultType === 'ITEM') candidates.set(result.itemId!, result);
    });
    return [...candidates.values()];
  }

  private clearManualLinkSelection(): void {
    this.manualLinkDetailRequestId++;
    this.manualLinkDetailLoading.set(false);
    this.selectedManualLinkItem.set(null);
    this.selectedManualLinkDetail.set(null);
    this.selectedManualLinkEditionId.set(null);
  }
}
