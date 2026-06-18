import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { ShopProductResponse } from '../../../core/models/inventory.model';
import { ShopResponse } from '../../../core/models/shop.model';
import { InventoryService } from '../../../core/services/inventory.service';
import { ReservationService } from '../../../core/services/reservation.service';
import { ShopService } from '../../../core/services/shop.service';

@Component({
  selector: 'app-shop-product-detail',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './shop-product-detail.component.html',
  styleUrl: './shop-product-detail.component.scss'
})
export class ShopProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly inventoryService = inject(InventoryService);
  private readonly reservationService = inject(ReservationService);
  private readonly shopService = inject(ShopService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly product = signal<ShopProductResponse | null>(null);
  readonly shop = signal<ShopResponse | null>(null);
  readonly loading = signal(false);
  readonly creatingReservation = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly reservationForm = this.fb.group({
    quantity: [1, [Validators.required, Validators.min(1)]],
    userMessage: ['']
  });

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

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  createReservation(product: ShopProductResponse): void {
    if (this.reservationForm.invalid) {
      this.reservationForm.markAllAsTouched();
      return;
    }

    const value = this.reservationForm.getRawValue();
    const quantity = Number(value.quantity);
    this.creatingReservation.set(true);
    this.errorMessage.set(null);
    this.reservationService
      .createReservation({
        shopProductId: product.id,
        quantity: Number.isFinite(quantity) && quantity > 0 ? quantity : 1,
        userMessage: value.userMessage?.trim() || null
      })
      .subscribe({
        next: (reservation) => {
          void this.router.navigate(['/reservations', reservation.id], {
            state: { successMessage: 'Reserva creada correctamente.' }
          });
        },
        error: (error) => {
          this.errorMessage.set(this.errorMessageService.toMessage(error));
          this.creatingReservation.set(false);
        }
      });
  }

  private loadShop(shopId: number): void {
    this.shopService.getPublicShop(shopId).subscribe({
      next: (shop) => this.shop.set(shop),
      error: () => this.shop.set(null)
    });
  }
}
