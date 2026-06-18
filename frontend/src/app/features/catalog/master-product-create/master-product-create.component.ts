import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import {
  CreateMasterProductRequest,
  ProductCategoryResponse
} from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';

@Component({
  selector: 'app-master-product-create',
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
  templateUrl: './master-product-create.component.html',
  styleUrl: './master-product-create.component.scss'
})
export class MasterProductCreateComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly catalogService = inject(CatalogService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);
  private readonly router = inject(Router);

  readonly categories = signal<ProductCategoryResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    categoryCode: ['', [Validators.required]],
    description: ['', [Validators.maxLength(4000)]],
    franchise: ['', [Validators.maxLength(255)]],
    collectionName: ['', [Validators.maxLength(255)]],
    volumeNumber: ['', [Validators.maxLength(64)]],
    publisher: ['', [Validators.maxLength(255)]],
    isbn: ['', [Validators.maxLength(32)]],
    ean: ['', [Validators.maxLength(32)]],
    releaseDate: [''],
    editionStartDate: [''],
    editionEndDate: [''],
    language: ['', [Validators.maxLength(8)]],
    limitedEdition: [false],
    limitedEditionTotalUnits: [null as number | null, [Validators.min(1)]],
    publicationCountries: [''],
    coverImageUrl: ['', [Validators.maxLength(2048)]],
    attributesJson: ['{}']
  });

  ngOnInit(): void {
    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  canCreateProduct(): boolean {
    return this.authService.hasAnyRole(['ADMIN', 'SHOP_OWNER']);
  }

  submit(): void {
    if (!this.canCreateProduct()) {
      this.errorMessage.set(this.languageService.translate('catalog.noPermissionError'));
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request = this.toRequest();
    if (!request) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.catalogService
      .createMasterProduct(request)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (product) => void this.router.navigate(['/catalog', product.id]),
        error: (error) => this.errorMessage.set(this.toErrorMessage(error))
      });
  }

  private toRequest(): CreateMasterProductRequest | null {
    const value = this.form.getRawValue();
    const attributes = this.parseAttributes(value.attributesJson);
    if (!attributes) {
      return null;
    }

    return {
      name: value.name.trim(),
      categoryCode: value.categoryCode,
      description: this.optionalText(value.description),
      franchise: this.optionalText(value.franchise),
      collectionName: this.optionalText(value.collectionName),
      volumeNumber: this.optionalText(value.volumeNumber),
      publisher: this.optionalText(value.publisher),
      isbn: this.optionalText(value.isbn),
      ean: this.optionalText(value.ean),
      releaseDate: this.optionalText(value.releaseDate),
      editionStartDate: this.optionalText(value.editionStartDate),
      editionEndDate: this.optionalText(value.editionEndDate),
      language: this.optionalText(value.language),
      limitedEdition: value.limitedEdition,
      limitedEditionTotalUnits: value.limitedEditionTotalUnits,
      publicationCountries: this.parseCountries(value.publicationCountries),
      coverImageUrl: this.optionalText(value.coverImageUrl),
      attributes
    };
  }

  private parseAttributes(value: string): Record<string, unknown> | null {
    const normalized = value.trim();
    if (!normalized) {
      return {};
    }

    try {
      const parsed = JSON.parse(normalized) as unknown;
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        return parsed as Record<string, unknown>;
      }
      this.errorMessage.set(this.languageService.translate('catalog.attributesObjectError'));
      return null;
    } catch {
      this.errorMessage.set(this.languageService.translate('catalog.attributesJsonError'));
      return null;
    }
  }

  private parseCountries(value: string): string[] {
    return value
      .split(',')
      .map((country) => country.trim().toUpperCase())
      .filter(Boolean);
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }

  private toErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === 409) {
      return this.languageService.translate('catalog.duplicateProduct');
    }

    return this.errorMessageService.toMessage(error);
  }
}
