import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { MasterProductResponse } from '../../../core/models/catalog.model';
import {
  EditorialCatalogItemDetail,
  EditorialCatalogSearchItem
} from '../../../core/models/editorial-catalog.model';
import { CollectionResponse, COLLECTION_ITEM_STATUSES, CreateCollectionItemRequest } from '../../../core/models/collection.model';
import { PHYSICAL_CONDITIONS } from '../../../core/models/inventory.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';

type ReferenceMode = 'EDITORIAL' | 'MANUAL' | 'LEGACY';

@Component({
  selector: 'app-collection-item-create',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, TranslatePipe],
  templateUrl: './collection-item-create.component.html',
  styleUrl: './collection-item-create.component.scss'
})
export class CollectionItemCreateComponent implements OnInit {
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
  private detailRequestId = 0;
  private editorialSearchRequestId = 0;
  private legacySearchRequestId = 0;

  readonly statuses = COLLECTION_ITEM_STATUSES;
  readonly conditions = PHYSICAL_CONDITIONS;
  readonly collectionId = signal<number | null>(null);
  readonly collection = signal<CollectionResponse | null>(null);
  readonly isOwner = signal(false);
  readonly loading = signal(false);
  readonly ownerLoading = signal(false);
  readonly searching = signal(false);
  readonly detailLoading = signal(false);
  readonly saving = signal(false);
  readonly accessDenied = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly products = signal<MasterProductResponse[]>([]);
  readonly editorialResults = signal<EditorialCatalogSearchItem[]>([]);
  readonly editorialSearchPerformed = signal(false);
  readonly selectedCatalogItem = signal<EditorialCatalogSearchItem | null>(null);
  readonly selectedCatalogItemDetail = signal<EditorialCatalogItemDetail | null>(null);
  readonly selectedEditionId = signal<number | null>(null);
  readonly productSearch = this.fb.nonNullable.group({ name: [''] });
  readonly editorialSearch = this.fb.nonNullable.group({ q: [''] });
  readonly form = this.fb.group({
    referenceMode: ['EDITORIAL' as ReferenceMode, [Validators.required]],
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

  ngOnInit(): void {
    const collectionId = Number(this.route.snapshot.paramMap.get('collectionId'));
    if (!Number.isFinite(collectionId) || collectionId <= 0) {
      this.errorMessage.set(this.languageService.translate('collections.collectionNotFound'));
      return;
    }
    this.collectionId.set(collectionId);
    this.loadCollection(collectionId);
  }

  changeReferenceMode(mode: ReferenceMode): void {
    this.searching.set(false);
    if (mode === 'EDITORIAL') {
      this.legacySearchRequestId++;
      this.form.controls.masterProductId.setValue(null);
      this.products.set([]);
    } else if (mode === 'LEGACY') {
      this.editorialSearchRequestId++;
      this.clearEditorialState();
    } else {
      this.editorialSearchRequestId++;
      this.legacySearchRequestId++;
      this.clearEditorialState();
      this.form.controls.masterProductId.setValue(null);
      this.products.set([]);
    }
    if (mode !== 'MANUAL') this.form.patchValue({ manualTitle: '', manualDescription: '', manualType: '' });
    this.errorMessage.set(null);
  }

  searchProducts(): void {
    if (!this.isOwner() || this.searching()) return;
    const requestId = ++this.legacySearchRequestId;
    this.searching.set(true);
    this.catalogService.searchMasterProducts({ name: this.productSearch.controls.name.value, status: 'ACTIVE' })
      .pipe(finalize(() => { if (requestId === this.legacySearchRequestId) this.searching.set(false); }), takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (products) => { if (requestId === this.legacySearchRequestId) this.products.set(products); }, error: (error) => { if (requestId === this.legacySearchRequestId) this.errorMessage.set(this.errorMessageService.toMessage(error)); } });
  }

  searchEditorial(): void {
    if (!this.isOwner() || this.searching()) return;
    this.clearEditorialState();
    const requestId = ++this.editorialSearchRequestId;
    this.editorialSearchPerformed.set(true);
    this.editorialResults.set([]);
    this.errorMessage.set(null);
    this.searching.set(true);
    this.editorialCatalogService.search({ q: this.editorialSearch.controls.q.value, size: 30 })
      .pipe(finalize(() => { if (requestId === this.editorialSearchRequestId) this.searching.set(false); }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => { if (requestId === this.editorialSearchRequestId) this.editorialResults.set(this.toItemCandidates(page.content)); },
        error: (error) => { if (requestId === this.editorialSearchRequestId) this.errorMessage.set(this.errorMessageService.toMessage(error)); }
      });
  }

  selectCatalogItem(candidate: EditorialCatalogSearchItem): void {
    if (!this.isOwner() || candidate.itemId == null) return;
    const requestId = ++this.detailRequestId;
    this.selectedCatalogItem.set(candidate);
    this.selectedCatalogItemDetail.set(null);
    this.selectedEditionId.set(null);
    this.errorMessage.set(null);
    this.detailLoading.set(true);
    this.editorialCatalogService.getItemDetail(candidate.itemId)
      .pipe(
        finalize(() => { if (requestId === this.detailRequestId) this.detailLoading.set(false); }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (detail) => {
          if (requestId !== this.detailRequestId || this.selectedCatalogItem()?.itemId !== detail.item.id) return;
          this.selectedCatalogItemDetail.set(detail);
        },
        error: (error) => {
          if (requestId !== this.detailRequestId) return;
          this.selectedCatalogItem.set(null);
          this.selectedEditionId.set(null);
          this.errorMessage.set(this.errorMessageService.toMessage(error));
        }
      });
  }

  selectEdition(editionId: number | null): void {
    const editions = this.selectedCatalogItemDetail()?.editions ?? [];
    this.selectedEditionId.set(editionId !== null && editions.some((edition) => edition.id === editionId) ? editionId : null);
  }

  submit(): void {
    const collectionId = this.collectionId();
    if (!collectionId) {
      this.errorMessage.set(this.languageService.translate('collections.collectionNotFound'));
      return;
    }
    if (!this.isOwner() || this.saving() || this.detailLoading()) return;
    const value = this.form.getRawValue();
    const missingReference = value.referenceMode === 'EDITORIAL'
      ? !this.selectedCatalogItemDetail()
      : value.referenceMode === 'LEGACY' ? !value.masterProductId : !this.optionalText(value.manualTitle);
    if (this.form.invalid || missingReference) {
      this.form.markAllAsTouched();
      if (missingReference) this.errorMessage.set(this.languageService.translate('collections.referenceRequired'));
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.collectionService.addCollectionItem(collectionId, this.toRequest())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({ next: () => void this.router.navigate(['/collections', collectionId]), error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error)) });
  }

  private loadCollection(collectionId: number): void {
    this.loading.set(true);
    this.collectionService.getCollection(collectionId).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (collection) => { this.collection.set(collection); this.resolveOwnership(collection); },
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  private resolveOwnership(collection: CollectionResponse): void {
    const currentUser = this.authService.currentUser();
    if (currentUser) { this.setOwnership(currentUser.id, collection); return; }
    if (!this.authService.hasToken()) { this.accessDenied.set(true); return; }
    this.ownerLoading.set(true);
    this.authService.getMe().pipe(finalize(() => this.ownerLoading.set(false))).subscribe({
      next: (user) => this.setOwnership(user.id, collection),
      error: () => this.accessDenied.set(true)
    });
  }

  private setOwnership(userId: number, collection: CollectionResponse): void {
    const owner = userId === collection.userId;
    this.isOwner.set(owner);
    this.accessDenied.set(!owner);
  }

  private toItemCandidates(results: EditorialCatalogSearchItem[]): EditorialCatalogSearchItem[] {
    const candidates = new Map<number, EditorialCatalogSearchItem>();
    results.filter((result) => result.resultType !== 'SERIES' && result.itemId !== null).forEach((result) => {
      const existing = candidates.get(result.itemId!);
      if (!existing || result.resultType === 'ITEM') candidates.set(result.itemId!, result);
    });
    return [...candidates.values()];
  }

  private clearEditorialSelection(): void {
    this.detailRequestId++;
    this.detailLoading.set(false);
    this.selectedCatalogItem.set(null);
    this.selectedCatalogItemDetail.set(null);
    this.selectedEditionId.set(null);
  }

  private clearEditorialState(): void {
    this.editorialSearchRequestId++;
    this.searching.set(false);
    this.clearEditorialSelection();
    this.editorialResults.set([]);
    this.editorialSearchPerformed.set(false);
  }

  private toRequest(): CreateCollectionItemRequest {
    const value = this.form.getRawValue();
    const selected = this.selectedCatalogItem();
    const detail = this.selectedCatalogItemDetail();
    const editorial = value.referenceMode === 'EDITORIAL' && selected?.itemId === detail?.item.id ? detail : null;
    return {
      masterProductId: value.referenceMode === 'LEGACY' ? Number(value.masterProductId) : null,
      catalogItemId: editorial?.item.id ?? null,
      catalogItemEditionId: editorial ? this.selectedEditionId() : null,
      manualTitle: value.referenceMode === 'MANUAL' ? this.optionalText(value.manualTitle) : null,
      manualDescription: value.referenceMode === 'MANUAL' ? this.optionalText(value.manualDescription) : null,
      manualType: value.referenceMode === 'MANUAL' ? this.optionalText(value.manualType) : null,
      collectionStatus: value.collectionStatus as CreateCollectionItemRequest['collectionStatus'],
      physicalCondition: (this.optionalText(value.physicalCondition) as CreateCollectionItemRequest['physicalCondition']) ?? null,
      unitNumber: this.optionalText(value.unitNumber),
      totalLimitedUnits: value.totalLimitedUnits ? Number(value.totalLimitedUnits) : null,
      notes: this.optionalText(value.notes),
      acquiredAt: this.optionalText(value.acquiredAt)
    };
  }

  private optionalText(value: string | null | undefined): string | null {
    const normalized = (value ?? '').trim();
    return normalized || null;
  }
}
