import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import {
  CatalogItemResponse,
  CatalogRecordStatus
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-items',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './admin-items.component.html',
  styleUrl: './admin-items.component.scss'
})
export class AdminItemsComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);

  readonly statuses: CatalogRecordStatus[] = ['DRAFT', 'ACTIVE', 'ARCHIVED'];
  readonly items = signal<CatalogItemResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly editing = signal<CatalogItemResponse | null>(null);
  readonly messageKey = signal<string | null>('admin.editorial.messages.contextRequired');
  readonly errorKey = signal<string | null>(null);

  readonly filters = this.fb.group({
    seriesId: [null as number | null, [Validators.required]],
    q: [''],
    recordStatus: ['' as CatalogRecordStatus | ''],
    publicationYear: [null as number | null, [Validators.min(1000), Validators.max(3000)]],
    language: [''],
    country: ['', [Validators.pattern(/^[A-Za-z]{2}$/)]]
  });
  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    originalTitle: ['', [Validators.maxLength(255)]],
    sequenceLabel: ['', [Validators.maxLength(64)]],
    sortOrder: [null as number | null, [Validators.min(0)]],
    description: ['', [Validators.maxLength(4000)]],
    firstPublicationDate: [''],
    firstPublicationYear: [null as number | null, [Validators.min(1000), Validators.max(3000)]],
    originalLanguage: [''],
    originCountry: ['', [Validators.pattern(/^[A-Za-z]{2}$/)]],
    recordStatus: ['DRAFT' as CatalogRecordStatus, [Validators.required]]
  });

  ngOnInit(): void {
    this.load();
  }

  load(page = this.page()): void {
    const filters = this.filters.getRawValue();
    const seriesId = this.optionalNumber(filters.seriesId);
    if (!seriesId) {
      this.items.set([]);
      this.messageKey.set('admin.editorial.messages.contextRequired');
      return;
    }

    this.loading.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service
      .searchItems(seriesId, {
        q: filters.q,
        recordStatus: filters.recordStatus || null,
        publicationYear: this.optionalNumber(filters.publicationYear),
        language: filters.language,
        country: this.optionalUpper(filters.country),
        page
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.items.set(response.content);
          this.page.set(response.page);
          this.totalPages.set(response.totalPages);
        },
        error: () => this.errorKey.set('admin.editorial.messages.loadError')
      });
  }

  search(): void {
    this.load(0);
  }

  previous(): void {
    if (this.page() > 0) {
      this.load(this.page() - 1);
    }
  }

  next(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.load(this.page() + 1);
    }
  }

  startCreate(): void {
    this.editing.set(null);
    this.form.reset({
      title: '',
      originalTitle: '',
      sequenceLabel: '',
      sortOrder: null,
      description: '',
      firstPublicationDate: '',
      firstPublicationYear: null,
      originalLanguage: '',
      originCountry: '',
      recordStatus: 'DRAFT'
    });
    this.errorKey.set(null);
  }

  startEdit(item: CatalogItemResponse): void {
    this.editing.set(item);
    this.form.reset({
      title: item.title,
      originalTitle: item.originalTitle ?? '',
      sequenceLabel: item.sequenceLabel ?? '',
      sortOrder: item.sortOrder,
      description: item.description ?? '',
      firstPublicationDate: item.firstPublicationDate ?? '',
      firstPublicationYear: item.firstPublicationYear,
      originalLanguage: item.originalLanguage ?? '',
      originCountry: item.originCountry ?? '',
      recordStatus: item.recordStatus
    });
    this.errorKey.set(null);
  }

  cancel(): void {
    this.startCreate();
  }

  submit(): void {
    const seriesId = this.optionalNumber(this.filters.controls.seriesId.value);
    if (!seriesId) {
      this.messageKey.set('admin.editorial.messages.contextRequired');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request = {
      title: value.title.trim(),
      originalTitle: this.optionalText(value.originalTitle),
      sequenceLabel: this.optionalText(value.sequenceLabel),
      sortOrder: this.optionalNumber(value.sortOrder),
      description: this.optionalText(value.description),
      firstPublicationDate: this.optionalText(value.firstPublicationDate),
      firstPublicationYear: this.optionalNumber(value.firstPublicationYear),
      originalLanguage: this.optionalText(value.originalLanguage),
      originCountry: this.optionalUpper(value.originCountry),
      recordStatus: value.recordStatus
    };
    const current = this.editing();
    const operation = current
      ? this.service.updateItem(current.id, request)
      : this.service.createItem(seriesId, request);

    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.messageKey.set('admin.editorial.messages.saved');
        this.startCreate();
        this.load();
      },
      error: () => this.errorKey.set('admin.editorial.messages.saveError')
    });
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }

  private optionalUpper(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized.toUpperCase() : null;
  }

  private optionalNumber(value: number | null): number | null {
    return value === null || Number.isNaN(value) ? null : value;
  }
}
