import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import {
  COLLECTION_VISIBILITIES,
  CreateCollectionRequest
} from '../../../core/models/collection.model';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { CollectionService } from '../../../core/services/collection.service';

@Component({
  selector: 'app-collection-create',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './collection-create.component.html',
  styleUrl: './collection-create.component.scss'
})
export class CollectionCreateComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  private readonly collectionService = inject(CollectionService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly visibilities = COLLECTION_VISIBILITIES;
  readonly categories = signal<ProductCategoryResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(160)]],
    description: ['', [Validators.maxLength(4000)]],
    visibility: ['PRIVATE', [Validators.required]],
    categoryCode: ['']
  });

  ngOnInit(): void {
    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .createCollection(this.toRequest())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (collection) => void this.router.navigate(['/collections', collection.id]),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): CreateCollectionRequest {
    const value = this.form.getRawValue();
    return {
      name: value.name.trim(),
      description: this.optionalText(value.description),
      visibility: value.visibility as CreateCollectionRequest['visibility'],
      categoryCode: this.optionalText(value.categoryCode)
    };
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }
}
