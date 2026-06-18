import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import {
  COLLECTION_VISIBILITIES,
  CollectionSearchFilters,
  CollectionResponse
} from '../../../core/models/collection.model';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';

@Component({
  selector: 'app-my-collections',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './my-collections.component.html',
  styleUrl: './my-collections.component.scss'
})
export class MyCollectionsComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly collectionService = inject(CollectionService);
  private readonly catalogService = inject(CatalogService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly visibilities = COLLECTION_VISIBILITIES;
  readonly categories = signal<ProductCategoryResponse[]>([]);
  readonly collections = signal<CollectionResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly filters = this.fb.group({
    visibility: [''],
    categoryCode: ['']
  });

  ngOnInit(): void {
    this.loadCategories();
    this.loadCollections();
  }

  loadCollections(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .getMyCollections(this.toFilters())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (collections) => this.collections.set(collections),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  resetFilters(): void {
    this.filters.reset({ visibility: '', categoryCode: '' });
    this.loadCollections();
  }

  deleteCollection(collection: CollectionResponse): void {
    if (
      !window.confirm(
        this.languageService.translate('collections.collectionDeleteConfirm', {
          name: collection.name
        })
      )
    ) {
      return;
    }

    this.collectionService.deleteCollection(collection.id).subscribe({
      next: () =>
        this.collections.set(this.collections().filter((candidate) => candidate.id !== collection.id)),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  private loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  private toFilters(): CollectionSearchFilters {
    const value = this.filters.getRawValue();
    return {
      visibility: value.visibility
        ? (value.visibility as CollectionSearchFilters['visibility'])
        : null,
      categoryCode: value.categoryCode || null
    };
  }
}
