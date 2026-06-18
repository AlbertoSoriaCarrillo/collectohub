import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { ShopResponse } from '../../../core/models/shop.model';
import { ShopService } from '../../../core/services/shop.service';

@Component({
  selector: 'app-my-shops',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule, TranslatePipe],
  templateUrl: './my-shops.component.html',
  styleUrl: './my-shops.component.scss'
})
export class MyShopsComponent implements OnInit {
  private readonly shopService = inject(ShopService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly shops = signal<ShopResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadShops();
  }

  loadShops(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.shopService
      .getMyShops()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (shops) => this.shops.set(shops),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }
}
