import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { ShopResponse } from '../../../core/models/shop.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopService } from '../../../core/services/shop.service';

@Component({
  selector: 'app-shop-inventory',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule, TranslatePipe],
  templateUrl: './shop-inventory.component.html',
  styleUrl: './shop-inventory.component.scss'
})
export class ShopInventoryComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly inventoryService = inject(InventoryService);
  private readonly shopService = inject(ShopService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly shop = signal<ShopResponse | null>(null);
  readonly products = signal<ShopProductResponse[]>([]);
  readonly loading = signal(false);
  readonly shopLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly shopId = signal<number | null>(null);

  ngOnInit(): void {
    const shopId = Number(this.route.snapshot.paramMap.get('shopId'));
    if (!Number.isFinite(shopId) || shopId <= 0) {
      this.errorMessage.set(this.languageService.translate('shops.notFound'));
      return;
    }

    this.shopId.set(shopId);
    this.loadShop(shopId);
    this.loadInventory(shopId);
  }

  priceLabel(product: ShopProductResponse): string {
    return `${product.priceAmount} ${product.currency}`;
  }

  private loadShop(shopId: number): void {
    this.shopLoading.set(true);
    this.shopService
      .getPublicShop(shopId)
      .pipe(finalize(() => this.shopLoading.set(false)))
      .subscribe({
        next: (shop) => this.shop.set(shop),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private loadInventory(shopId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.inventoryService
      .getMyShopProducts(shopId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (products) => this.products.set(products),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }
}
