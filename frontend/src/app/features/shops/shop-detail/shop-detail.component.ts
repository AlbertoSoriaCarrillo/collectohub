import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { PublicShopProductResponse } from '../../../core/models/inventory.model';
import { ShopMemberResponse, ShopResponse } from '../../../core/models/shop.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopService } from '../../../core/services/shop.service';

@Component({
  selector: 'app-shop-detail',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule, TranslatePipe],
  templateUrl: './shop-detail.component.html',
  styleUrl: './shop-detail.component.scss'
})
export class ShopDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly shopService = inject(ShopService);
  private readonly inventoryService = inject(InventoryService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly shop = signal<ShopResponse | null>(null);
  readonly membership = signal<ShopMemberResponse | null>(null);
  readonly publicProducts = signal<PublicShopProductResponse[]>([]);
  readonly publicProductsLoading = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(history.state?.successMessage ?? null);

  ngOnInit(): void {
    const shopId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(shopId)) {
      this.errorMessage.set(this.languageService.translate('shops.notFound'));
      return;
    }

    this.loading.set(true);
    this.shopService
      .getPublicShop(shopId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (shop) => {
          this.shop.set(shop);
          this.loadPublicProducts(shop.id);
          this.loadMembership(shop.id);
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  canManage(): boolean {
    const role = this.membership()?.role;
    return role === 'OWNER' || role === 'MANAGER';
  }

  priceLabel(product: PublicShopProductResponse): string {
    return `${product.priceAmount} ${product.currency}`;
  }

  private loadPublicProducts(shopId: number): void {
    this.publicProductsLoading.set(true);
    this.inventoryService
      .getPublicShopProducts(shopId)
      .pipe(finalize(() => this.publicProductsLoading.set(false)))
      .subscribe({
        next: (products) => this.publicProducts.set(products),
        error: () => this.publicProducts.set([])
      });
  }

  private loadMembership(shopId: number): void {
    if (!this.authService.hasToken()) {
      return;
    }

    this.shopService.getMyShops().subscribe({
      next: (shops) => {
        const currentShop = shops.find((shop) => shop.id === shopId);
        this.membership.set(currentShop?.currentUserMembership ?? null);
      },
      error: () => this.membership.set(null)
    });
  }
}
