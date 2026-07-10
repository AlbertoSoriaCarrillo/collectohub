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
import { LanguageService } from '../../../../core/i18n/language.service';
import {
  CatalogRecordStatus,
  CreatorResponse
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-creators',
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
  templateUrl: './admin-creators.component.html',
  styleUrl: './admin-creators.component.scss'
})
export class AdminCreatorsComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);
  private readonly languageService = inject(LanguageService);

  readonly statuses: CatalogRecordStatus[] = ['DRAFT', 'ACTIVE', 'ARCHIVED'];
  readonly creators = signal<CreatorResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly editing = signal<CreatorResponse | null>(null);
  readonly messageKey = signal<string | null>(null);
  readonly errorKey = signal<string | null>(null);

  readonly filters = this.fb.group({
    q: [''],
    recordStatus: ['' as CatalogRecordStatus | '']
  });
  readonly form = this.fb.group(
    {
      name: ['', [Validators.required, Validators.maxLength(255)]],
      slug: ['', [Validators.pattern(/^[a-z0-9]+(?:-[a-z0-9]+)*$/)]],
      sortName: ['', [Validators.maxLength(255)]],
      biography: ['', [Validators.maxLength(4000)]],
      country: ['', [Validators.pattern(/^[A-Za-z]{2}$/)]],
      birthYear: [null as number | null, [Validators.min(0)]],
      deathYear: [null as number | null, [Validators.min(0)]],
      recordStatus: ['DRAFT' as CatalogRecordStatus, [Validators.required]]
    },
    { validators: [lifeYearRangeValidator] }
  );

  ngOnInit(): void {
    this.load();
  }

  load(page = this.page()): void {
    const filters = this.filters.getRawValue();
    this.loading.set(true);
    this.errorKey.set(null);
    this.service
      .searchCreators({
        q: filters.q,
        recordStatus: filters.recordStatus || null,
        page
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.creators.set(response.content);
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
      name: '',
      slug: '',
      sortName: '',
      biography: '',
      country: '',
      birthYear: null,
      deathYear: null,
      recordStatus: 'DRAFT'
    });
    this.messageKey.set(null);
    this.errorKey.set(null);
  }

  startEdit(creator: CreatorResponse): void {
    this.editing.set(creator);
    this.form.reset({
      name: creator.name,
      slug: creator.slug,
      sortName: creator.sortName ?? '',
      biography: creator.biography ?? '',
      country: creator.country ?? '',
      birthYear: creator.birthYear,
      deathYear: creator.deathYear,
      recordStatus: creator.recordStatus
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
      name: value.name.trim(),
      slug: this.optionalText(value.slug),
      sortName: this.optionalText(value.sortName),
      biography: this.optionalText(value.biography),
      country: this.optionalUpper(value.country),
      birthYear: this.optionalNumber(value.birthYear),
      deathYear: this.optionalNumber(value.deathYear),
      recordStatus: value.recordStatus
    };
    const current = this.editing();
    const operation = current
      ? this.service.updateCreator(current.id, request)
      : this.service.createCreator(request);

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

  deleteSelected(): void {
    const current = this.editing();
    if (!current || !window.confirm(this.languageService.translate('admin.editorial.messages.confirmDeleteCreator'))) {
      return;
    }

    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service
      .deleteCreator(current.id)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.messageKey.set('admin.editorial.messages.deleted');
          this.startCreate();
          this.load();
        },
        error: () => this.errorKey.set('admin.editorial.messages.deleteError')
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

function lifeYearRangeValidator(control: AbstractControl): ValidationErrors | null {
  const birthYear = control.get('birthYear')?.value as number | null;
  const deathYear = control.get('deathYear')?.value as number | null;

  if (birthYear !== null && deathYear !== null && deathYear < birthYear) {
    return { yearRange: true };
  }

  return null;
}
