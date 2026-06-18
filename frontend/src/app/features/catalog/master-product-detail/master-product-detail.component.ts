import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { MasterProductResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';

@Component({
  selector: 'app-master-product-detail',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule],
  templateUrl: './master-product-detail.component.html',
  styleUrl: './master-product-detail.component.scss'
})
export class MasterProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly catalogService = inject(CatalogService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly product = signal<MasterProductResponse | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const productId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(productId) || productId <= 0) {
      this.errorMessage.set('Producto no valido.');
      return;
    }

    this.loading.set(true);
    this.catalogService.getMasterProduct(productId).subscribe({
      next: (product) => {
        this.product.set(product);
        this.loading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.errorMessageService.toMessage(error));
        this.loading.set(false);
      }
    });
  }

  canManageProduct(): boolean {
    return this.authService.hasAnyRole(['ADMIN', 'SHOP_OWNER']);
  }
}
