import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import {
  COLLECTION_VISIBILITIES,
  CollectionResponse,
  UpdateCollectionRequest
} from '../../../core/models/collection.model';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';

@Component({
  selector: 'app-collection-edit',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './collection-edit.component.html',
  styleUrl: './collection-edit.component.scss'
})
export class CollectionEditComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  private readonly collectionService = inject(CollectionService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly visibilities = COLLECTION_VISIBILITIES;
  readonly categories = signal<ProductCategoryResponse[]>([]);
  readonly collection = signal<CollectionResponse | null>(null);
  readonly collectionId = signal<number | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(160)]],
    description: ['', [Validators.maxLength(4000)]],
    visibility: ['PRIVATE', [Validators.required]],
    categoryCode: ['']
  });

  ngOnInit(): void {
    const collectionId = Number(this.route.snapshot.paramMap.get('collectionId'));
    if (!Number.isFinite(collectionId) || collectionId <= 0) {
      this.errorMessage.set(this.languageService.translate('collections.collectionNotFound'));
      return;
    }

    this.collectionId.set(collectionId);
    this.loadCategories();
    this.loadCollection(collectionId);
  }

  submit(): void {
    const collectionId = this.collectionId();
    if (!collectionId) {
      this.errorMessage.set(this.languageService.translate('collections.collectionNotFound'));
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .updateCollection(collectionId, this.toRequest())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/collections', collectionId]),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  private loadCollection(collectionId: number): void {
    this.loading.set(true);
    this.collectionService
      .getCollection(collectionId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (collection) => {
          this.collection.set(collection);
          this.form.patchValue({
            name: collection.name,
            description: collection.description ?? '',
            visibility: collection.visibility,
            categoryCode: collection.categoryCode ?? ''
          });
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): UpdateCollectionRequest {
    const value = this.form.getRawValue();
    return {
      name: value.name.trim(),
      description: this.optionalText(value.description),
      visibility: value.visibility as UpdateCollectionRequest['visibility'],
      categoryCode: this.optionalText(value.categoryCode)
    };
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }
}
