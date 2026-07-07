import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { MasterProductResponse } from '../../../core/models/catalog.model';
import { EditorialCatalogSearchItem } from '../../../core/models/editorial-catalog.model';
import {
  PHYSICAL_CONDITIONS,
  SHOP_PRODUCT_COMMERCIAL_STATUSES,
  ShopProductResponse,
  UpdateShopProductRequest
} from '../../../core/models/inventory.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { CatalogService } from '../../../core/services/catalog.service';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';

@Component({
  selector: 'app-shop-product-edit',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './shop-product-edit.component.html',
  styleUrl: './shop-product-edit.component.scss'
})
export class ShopProductEditComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly inventoryService = inject(InventoryService);
  private readonly catalogService = inject(CatalogService);
  private readonly editorialCatalogService = inject(EditorialCatalogService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly statuses = SHOP_PRODUCT_COMMERCIAL_STATUSES;
  readonly conditions = PHYSICAL_CONDITIONS;
  readonly shopId = signal<number | null>(null);
  readonly shopProductId = signal<number | null>(null);
  readonly product = signal<ShopProductResponse | null>(null);
  readonly masterProducts = signal<MasterProductResponse[]>([]);
  readonly editorialResults = signal<EditorialCatalogSearchItem[]>([]);
  readonly selectedEditorial = signal<EditorialCatalogSearchItem | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly searching = signal(false);
  readonly productSearch = this.fb.nonNullable.group({ name: [''] });
  readonly editorialSearch = this.fb.nonNullable.group({ query: [''] });
  readonly form = this.fb.group({
    referenceMode: ['LEGACY' as 'LEGACY' | 'EDITORIAL', [Validators.required]],
    masterProductId: [null as number | null],
    priceAmount: [null as number | null, [Validators.required, Validators.min(0)]],
    currency: ['EUR', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
    stockQuantity: [0, [Validators.required, Validators.min(0)]],
    commercialStatus: ['AVAILABLE', [Validators.required]],
    physicalCondition: ['', [Validators.required]],
    visible: [true],
    unitNumber: ['', [Validators.maxLength(50)]],
    totalLimitedUnits: [null as number | null, [Validators.min(1)]],
    notes: ['', [Validators.maxLength(4000)]]
  });

  ngOnInit(): void {
    const shopId = Number(this.route.snapshot.paramMap.get('shopId'));
    const shopProductId = Number(this.route.snapshot.paramMap.get('shopProductId'));
    if (
      !Number.isFinite(shopId) ||
      shopId <= 0 ||
      !Number.isFinite(shopProductId) ||
      shopProductId <= 0
    ) {
      this.errorMessage.set(this.languageService.translate('inventory.notFound'));
      return;
    }

    this.shopId.set(shopId);
    this.shopProductId.set(shopProductId);
    this.loadProduct(shopId, shopProductId);
  }

  submit(): void {
    const shopId = this.shopId();
    const shopProductId = this.shopProductId();
    if (!shopId || !shopProductId) {
      this.errorMessage.set(this.languageService.translate('inventory.notFound'));
      return;
    }

    if (this.form.invalid || !this.hasValidReference()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    this.inventoryService
      .updateShopProduct(shopId, shopProductId, this.toRequest())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/shops', shopId, 'inventory']),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  searchProducts(): void {
    this.searching.set(true);
    this.catalogService
      .searchMasterProducts({ name: this.productSearch.controls.name.value, status: 'ACTIVE' })
      .pipe(finalize(() => this.searching.set(false)))
      .subscribe({
        next: (products) => this.masterProducts.set(products),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  searchEditorial(): void {
    this.searching.set(true);
    this.editorialCatalogService
      .search({ q: this.editorialSearch.controls.query.value, page: 0, size: 20 })
      .pipe(finalize(() => this.searching.set(false)))
      .subscribe({
        next: (page) =>
          this.editorialResults.set(
            page.content.filter((result) => result.resultType === 'ITEM' || result.resultType === 'EDITION')
          ),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  selectEditorial(result: EditorialCatalogSearchItem): void {
    if (result.resultType === 'SERIES') {
      this.errorMessage.set(this.languageService.translate('inventory.seriesNotAllowed'));
      return;
    }
    this.selectedEditorial.set(result);
    this.errorMessage.set(null);
  }

  private loadProduct(shopId: number, shopProductId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.inventoryService
      .getMyShopProducts(shopId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (products) => {
          const product = products.find(
            (candidate) => candidate.id === shopProductId && candidate.shopId === shopId
          );
          if (!product) {
            this.errorMessage.set(this.languageService.translate('inventory.notFound'));
            return;
          }
          this.product.set(product);
          const editorial = Boolean(product.catalogItemId);
          this.form.patchValue({
            referenceMode: editorial ? 'EDITORIAL' : 'LEGACY',
            masterProductId: product.masterProductId,
            priceAmount: product.priceAmount,
            currency: product.currency,
            stockQuantity: product.stockQuantity,
            commercialStatus: product.commercialStatus,
            physicalCondition: product.physicalCondition,
            visible: product.visible,
            unitNumber: product.unitNumber ?? '',
            totalLimitedUnits: product.totalLimitedUnits,
            notes: product.notes ?? ''
          });
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): UpdateShopProductRequest {
    const value = this.form.getRawValue();
    const product = this.product();
    const selected = this.selectedEditorial();
    const isLegacy = value.referenceMode === 'LEGACY';
    return {
      masterProductId: isLegacy ? Number(value.masterProductId) : null,
      catalogItemId: isLegacy ? null : selected?.itemId ?? product?.catalogItemId ?? null,
      catalogItemEditionId: isLegacy
        ? null
        : selected
          ? selected.editionId
          : product?.catalogItemEditionId ?? null,
      priceAmount: Number(value.priceAmount),
      currency: (value.currency ?? '').trim().toUpperCase(),
      stockQuantity: Number(value.stockQuantity),
      commercialStatus: value.commercialStatus as UpdateShopProductRequest['commercialStatus'],
      physicalCondition: value.physicalCondition as UpdateShopProductRequest['physicalCondition'],
      visible: value.visible,
      unitNumber: this.optionalText(value.unitNumber),
      totalLimitedUnits: value.totalLimitedUnits ? Number(value.totalLimitedUnits) : null,
      notes: this.optionalText(value.notes)
    };
  }

  private hasValidReference(): boolean {
    const value = this.form.getRawValue();
    if (value.referenceMode === 'LEGACY') {
      if (value.masterProductId) return true;
      this.errorMessage.set(this.languageService.translate('inventory.legacyReferenceRequired'));
      return false;
    }
    if (this.selectedEditorial()?.itemId || this.product()?.catalogItemId) return true;
    this.errorMessage.set(this.languageService.translate('inventory.editorialReferenceRequired'));
    return false;
  }

  private optionalText(value: string | null | undefined): string | null {
    const normalized = (value ?? '').trim();
    return normalized ? normalized : null;
  }
}
