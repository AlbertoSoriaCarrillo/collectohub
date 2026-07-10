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
  CatalogItemEditionFormat,
  CatalogItemEditionResponse,
  CatalogRecordStatus
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-editions',
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
  templateUrl: './admin-editions.component.html',
  styleUrl: './admin-editions.component.scss'
})
export class AdminEditionsComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);

  readonly statuses: CatalogRecordStatus[] = ['DRAFT', 'ACTIVE', 'ARCHIVED'];
  readonly formats: CatalogItemEditionFormat[] = [
    'HARDCOVER',
    'PAPERBACK',
    'SOFTCOVER',
    'DIGITAL',
    'OMNIBUS',
    'BOX_SET',
    'SINGLE_ISSUE',
    'OTHER'
  ];
  readonly editions = signal<CatalogItemEditionResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly editing = signal<CatalogItemEditionResponse | null>(null);
  readonly messageKey = signal<string | null>('admin.editorial.messages.contextRequired');
  readonly errorKey = signal<string | null>(null);

  readonly filters = this.fb.group({
    itemId: [null as number | null, [Validators.required]],
    publisherId: [null as number | null],
    isbn: [''],
    ean: [''],
    format: ['' as CatalogItemEditionFormat | ''],
    language: [''],
    country: ['', [Validators.pattern(/^[A-Za-z]{2}$/)]],
    publicationYear: [null as number | null, [Validators.min(1000), Validators.max(3000)]],
    recordStatus: ['' as CatalogRecordStatus | '']
  });
  readonly form = this.fb.group({
    publisherId: [null as number | null],
    isbn: [''],
    ean: [''],
    format: ['PAPERBACK' as CatalogItemEditionFormat, [Validators.required]],
    editionName: [''],
    publicationDate: [''],
    publicationYear: [null as number | null, [Validators.min(1000), Validators.max(3000)]],
    language: [''],
    country: ['', [Validators.pattern(/^[A-Za-z]{2}$/)]],
    pageCount: [null as number | null, [Validators.min(1)]],
    coverImageUrl: ['', [Validators.pattern(/^https?:\/\/.+/)]],
    recordStatus: ['DRAFT' as CatalogRecordStatus, [Validators.required]]
  });

  ngOnInit(): void {
    this.load();
  }

  load(page = this.page()): void {
    const filters = this.filters.getRawValue();
    const itemId = this.optionalNumber(filters.itemId);
    if (!itemId) {
      this.editions.set([]);
      this.messageKey.set('admin.editorial.messages.contextRequired');
      return;
    }

    this.loading.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service
      .searchEditions(itemId, {
        publisherId: this.optionalNumber(filters.publisherId),
        isbn: filters.isbn,
        ean: filters.ean,
        format: filters.format || null,
        language: filters.language,
        country: this.optionalUpper(filters.country),
        publicationYear: this.optionalNumber(filters.publicationYear),
        recordStatus: filters.recordStatus || null,
        page
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.editions.set(response.content);
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
      publisherId: null,
      isbn: '',
      ean: '',
      format: 'PAPERBACK',
      editionName: '',
      publicationDate: '',
      publicationYear: null,
      language: '',
      country: '',
      pageCount: null,
      coverImageUrl: '',
      recordStatus: 'DRAFT'
    });
    this.errorKey.set(null);
  }

  startEdit(edition: CatalogItemEditionResponse): void {
    this.editing.set(edition);
    this.form.reset({
      publisherId: edition.publisherId,
      isbn: edition.isbn ?? '',
      ean: edition.ean ?? '',
      format: edition.format,
      editionName: edition.editionName ?? '',
      publicationDate: edition.publicationDate ?? '',
      publicationYear: edition.publicationYear,
      language: edition.language ?? '',
      country: edition.country ?? '',
      pageCount: edition.pageCount,
      coverImageUrl: edition.coverImageUrl ?? '',
      recordStatus: edition.recordStatus
    });
    this.errorKey.set(null);
  }

  cancel(): void {
    this.startCreate();
  }

  submit(): void {
    const itemId = this.optionalNumber(this.filters.controls.itemId.value);
    if (!itemId) {
      this.messageKey.set('admin.editorial.messages.contextRequired');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request = {
      publisherId: this.optionalNumber(value.publisherId),
      isbn: this.optionalText(value.isbn),
      ean: this.optionalText(value.ean),
      format: value.format,
      editionName: this.optionalText(value.editionName),
      publicationDate: this.optionalText(value.publicationDate),
      publicationYear: this.optionalNumber(value.publicationYear),
      language: this.optionalText(value.language),
      country: this.optionalUpper(value.country),
      pageCount: this.optionalNumber(value.pageCount),
      coverImageUrl: this.optionalText(value.coverImageUrl),
      recordStatus: value.recordStatus
    };
    const current = this.editing();
    const operation = current
      ? this.service.updateEdition(current.id, request)
      : this.service.createEdition(itemId, request);

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
