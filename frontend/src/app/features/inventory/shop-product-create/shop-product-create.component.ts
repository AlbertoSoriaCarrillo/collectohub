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
import { MasterProductResponse } from '../../../core/models/catalog.model';
import {
  CreateShopProductRequest,
  PHYSICAL_CONDITIONS,
  SHOP_PRODUCT_COMMERCIAL_STATUSES
} from '../../../core/models/inventory.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { InventoryService } from '../../../core/services/inventory.service';

@Component({
  selector: 'app-shop-product-create',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './shop-product-create.component.html',
  styleUrl: './shop-product-create.component.scss'
})
export class ShopProductCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  private readonly inventoryService = inject(InventoryService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly statuses = SHOP_PRODUCT_COMMERCIAL_STATUSES;
  readonly conditions = PHYSICAL_CONDITIONS;
  readonly masterProducts = signal<MasterProductResponse[]>([]);
  readonly shopId = signal<number | null>(null);
  readonly loading = signal(false);
  readonly searching = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly productSearch = this.fb.nonNullable.group({
    name: ['']
  });
  readonly form = this.fb.group({
    masterProductId: [null as number | null, [Validators.required]],
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
    if (!Number.isFinite(shopId) || shopId <= 0) {
      this.errorMessage.set('Tienda no encontrada.');
      return;
    }

    this.shopId.set(shopId);
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
        next: (products) => this.masterProducts.set(products),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  submit(): void {
    const shopId = this.shopId();
    if (!shopId) {
      this.errorMessage.set('Tienda no encontrada.');
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.inventoryService
      .createShopProduct(shopId, this.toRequest())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/shops', shopId, 'inventory']),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): CreateShopProductRequest {
    const value = this.form.getRawValue();
    return {
      masterProductId: Number(value.masterProductId),
      priceAmount: Number(value.priceAmount),
      currency: this.requiredUpper(value.currency),
      stockQuantity: Number(value.stockQuantity),
      commercialStatus: value.commercialStatus as CreateShopProductRequest['commercialStatus'],
      physicalCondition: value.physicalCondition as CreateShopProductRequest['physicalCondition'],
      visible: value.visible,
      unitNumber: this.optionalText(value.unitNumber),
      totalLimitedUnits: value.totalLimitedUnits ? Number(value.totalLimitedUnits) : null,
      notes: this.optionalText(value.notes)
    };
  }

  private requiredUpper(value: string | null | undefined): string {
    return (value ?? '').trim().toUpperCase();
  }

  private optionalText(value: string | null | undefined): string | null {
    const normalized = (value ?? '').trim();
    return normalized ? normalized : null;
  }
}
