import { Component, OnInit, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
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
  CatalogPublicationStatus,
  CatalogRecordStatus,
  CatalogSeriesResponse,
  CatalogSeriesType
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-series',
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
  templateUrl: './admin-series.component.html',
  styleUrl: './admin-series.component.scss'
})
export class AdminSeriesComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);

  readonly statuses: CatalogRecordStatus[] = ['DRAFT', 'ACTIVE', 'ARCHIVED'];
  readonly types: CatalogSeriesType[] = ['BOOK', 'COMIC', 'MANGA'];
  readonly publicationStatuses: CatalogPublicationStatus[] = [
    'ONGOING',
    'COMPLETED',
    'CANCELLED',
    'HIATUS',
    'UNKNOWN'
  ];
  readonly series = signal<CatalogSeriesResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly editing = signal<CatalogSeriesResponse | null>(null);
  readonly messageKey = signal<string | null>(null);
  readonly errorKey = signal<string | null>(null);

  readonly filters = this.fb.group({
    q: [''],
    recordStatus: ['' as CatalogRecordStatus | ''],
    type: ['' as CatalogSeriesType | ''],
    publicationStatus: ['' as CatalogPublicationStatus | '']
  });
  readonly form = this.fb.group(
    {
      franchiseId: [null as number | null],
      primaryPublisherId: [null as number | null],
      title: ['', [Validators.required, Validators.maxLength(255)]],
      originalTitle: ['', [Validators.maxLength(255)]],
      type: ['MANGA' as CatalogSeriesType, [Validators.required]],
      publicationStatus: ['UNKNOWN' as CatalogPublicationStatus, [Validators.required]],
      description: ['', [Validators.maxLength(4000)]],
      originCountry: ['', [Validators.pattern(/^[A-Za-z]{2}$/)]],
      originalLanguage: [''],
      startYear: [null as number | null, [Validators.min(1000), Validators.max(3000)]],
      endYear: [null as number | null, [Validators.min(1000), Validators.max(3000)]],
      recordStatus: ['DRAFT' as CatalogRecordStatus, [Validators.required]]
    },
    { validators: [yearRangeValidator] }
  );

  ngOnInit(): void {
    this.load();
  }

  load(page = this.page()): void {
    const filters = this.filters.getRawValue();
    this.loading.set(true);
    this.errorKey.set(null);
    this.service
      .searchSeries({
        q: filters.q,
        recordStatus: filters.recordStatus || null,
        type: filters.type || null,
        publicationStatus: filters.publicationStatus || null,
        page
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.series.set(response.content);
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
      franchiseId: null,
      primaryPublisherId: null,
      title: '',
      originalTitle: '',
      type: 'MANGA',
      publicationStatus: 'UNKNOWN',
      description: '',
      originCountry: '',
      originalLanguage: '',
      startYear: null,
      endYear: null,
      recordStatus: 'DRAFT'
    });
    this.messageKey.set(null);
    this.errorKey.set(null);
  }

  startEdit(series: CatalogSeriesResponse): void {
    this.editing.set(series);
    this.form.reset({
      franchiseId: series.franchiseId,
      primaryPublisherId: series.primaryPublisherId,
      title: series.title,
      originalTitle: series.originalTitle ?? '',
      type: series.type,
      publicationStatus: series.publicationStatus,
      description: series.description ?? '',
      originCountry: series.originCountry ?? '',
      originalLanguage: series.originalLanguage ?? '',
      startYear: series.startYear,
      endYear: series.endYear,
      recordStatus: series.recordStatus
    });
    this.messageKey.set(null);
    this.errorKey.set(null);
  }

  cancel(): void {
    this.startCreate();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request = {
      franchiseId: this.optionalNumber(value.franchiseId),
      primaryPublisherId: this.optionalNumber(value.primaryPublisherId),
      title: value.title.trim(),
      originalTitle: this.optionalText(value.originalTitle),
      type: value.type,
      publicationStatus: value.publicationStatus,
      description: this.optionalText(value.description),
      originCountry: this.optionalUpper(value.originCountry),
      originalLanguage: this.optionalText(value.originalLanguage),
      startYear: this.optionalNumber(value.startYear),
      endYear: this.optionalNumber(value.endYear),
      recordStatus: value.recordStatus
    };
    const current = this.editing();
    const operation = current
      ? this.service.updateSeries(current.id, request)
      : this.service.createSeries(request);

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

function yearRangeValidator(control: AbstractControl): ValidationErrors | null {
  const startYear = control.get('startYear')?.value as number | null;
  const endYear = control.get('endYear')?.value as number | null;

  if (startYear !== null && endYear !== null && endYear < startYear) {
    return { yearRange: true };
  }

  return null;
}
