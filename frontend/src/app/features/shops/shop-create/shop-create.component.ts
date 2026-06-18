import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { CreateShopRequest } from '../../../core/models/shop.model';
import { ShopService } from '../../../core/services/shop.service';

@Component({
  selector: 'app-shop-create',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe
  ],
  templateUrl: './shop-create.component.html',
  styleUrl: './shop-create.component.scss'
})
export class ShopCreateComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly shopService = inject(ShopService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(160)]],
    description: ['', [Validators.maxLength(4000)]],
    contactEmail: ['', [Validators.email, Validators.maxLength(320)]],
    contactPhone: ['', [Validators.maxLength(40)]],
    country: ['', [Validators.pattern(/^[A-Za-z]{2}$/)]],
    currency: ['EUR', [Validators.pattern(/^[A-Za-z]{3}$/)]],
    defaultReservationExpirationHours: [48, [Validators.min(1)]],
    logoUrl: ['', [Validators.maxLength(2048)]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.shopService
      .createShop(this.toRequest())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (shop) => {
          const message = this.languageService.translate('shops.createdReloginHint');
          this.successMessage.set(message);
          void this.router.navigate(['/shops', shop.id], { state: { successMessage: message } });
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): CreateShopRequest {
    const value = this.form.getRawValue();
    return {
      name: value.name.trim(),
      description: this.optionalText(value.description),
      contactEmail: this.optionalText(value.contactEmail),
      contactPhone: this.optionalText(value.contactPhone),
      country: this.optionalUpper(value.country),
      currency: this.optionalUpper(value.currency),
      defaultReservationExpirationHours: value.defaultReservationExpirationHours,
      logoUrl: this.optionalText(value.logoUrl)
    };
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }

  private optionalUpper(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized.toUpperCase() : null;
  }
}
