import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { MasterProductResponse } from '../../../core/models/catalog.model';
import { EditorialCatalogSearchItem } from '../../../core/models/editorial-catalog.model';
import {
  COLLECTION_ITEM_STATUSES,
  CreateCollectionItemRequest
} from '../../../core/models/collection.model';
import { PHYSICAL_CONDITIONS } from '../../../core/models/inventory.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';

@Component({
  selector: 'app-collection-item-create',
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
  templateUrl: './collection-item-create.component.html',
  styleUrl: './collection-item-create.component.scss'
})
export class CollectionItemCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  private readonly collectionService = inject(CollectionService);
  private readonly editorialCatalogService = inject(EditorialCatalogService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly statuses = COLLECTION_ITEM_STATUSES;
  readonly conditions = PHYSICAL_CONDITIONS;
  readonly products = signal<MasterProductResponse[]>([]);
  readonly editorialResults = signal<EditorialCatalogSearchItem[]>([]);
  readonly selectedEditorial = signal<EditorialCatalogSearchItem | null>(null);
  readonly collectionId = signal<number | null>(null);
  readonly loading = signal(false);
  readonly searching = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly productSearch = this.fb.nonNullable.group({
    name: ['']
  });
  readonly editorialSearch = this.fb.nonNullable.group({ q: [''] });
  readonly form = this.fb.group({
    referenceMode: ['LEGACY' as 'LEGACY' | 'EDITORIAL', [Validators.required]],
    masterProductId: [null as number | null],
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
    this.searchProducts();
  }

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
    this.errorMessage.set(null);
  }

  submit(): void {
    const collectionId = this.collectionId();
    if (!collectionId) {
      this.errorMessage.set(this.languageService.translate('collections.collectionNotFound'));
      return;
    }

    const value = this.form.getRawValue();
    const missingReference = value.referenceMode === 'LEGACY'
      ? !value.masterProductId
      : !this.selectedEditorial();
    if (this.form.invalid || missingReference) {
      this.form.markAllAsTouched();
      if (missingReference) {
        this.errorMessage.set(this.languageService.translate('collections.referenceRequired'));
      }
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .addCollectionItem(collectionId, this.toRequest())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/collections', collectionId]),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): CreateCollectionItemRequest {
    const value = this.form.getRawValue();
    const editorial = value.referenceMode === 'EDITORIAL' ? this.selectedEditorial() : null;
    return {
      masterProductId: value.referenceMode === 'LEGACY' ? Number(value.masterProductId) : null,
      catalogItemId: editorial?.itemId ?? null,
      catalogItemEditionId: editorial?.editionId ?? null,
      collectionStatus: value.collectionStatus as CreateCollectionItemRequest['collectionStatus'],
      physicalCondition:
        (this.optionalText(value.physicalCondition) as CreateCollectionItemRequest['physicalCondition']) ??
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
}
