import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { ShopResponse } from '../../../core/models/shop.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopService } from '../../../core/services/shop.service';

@Component({
  selector: 'app-shop-product-detail',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule],
  templateUrl: './shop-product-detail.component.html',
  styleUrl: './shop-product-detail.component.scss'
})
export class ShopProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly inventoryService = inject(InventoryService);
  private readonly shopService = inject(ShopService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly product = signal<ShopProductResponse | null>(null);
  readonly shop = signal<ShopResponse | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const shopProductId = Number(this.route.snapshot.paramMap.get('shopProductId'));
    if (!Number.isFinite(shopProductId) || shopProductId <= 0) {
      this.errorMessage.set('Producto no encontrado.');
      return;
    }

    this.loading.set(true);
    this.inventoryService.getPublicShopProduct(shopProductId).subscribe({
      next: (product) => {
        this.product.set(product);
        this.loading.set(false);
        this.loadShop(product.shopId);
      },
      error: (error) => {
        this.errorMessage.set(this.errorMessageService.toMessage(error));
        this.loading.set(false);
      }
    });
  }

  priceLabel(product: ShopProductResponse): string {
    return `${product.priceAmount} ${product.currency}`;
  }

  private loadShop(shopId: number): void {
    this.shopService.getPublicShop(shopId).subscribe({
      next: (shop) => this.shop.set(shop),
      error: () => this.shop.set(null)
    });
  }
}
