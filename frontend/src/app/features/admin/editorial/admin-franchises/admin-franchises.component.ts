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
  CatalogFranchiseResponse,
  CatalogRecordStatus
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-franchises',
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
  templateUrl: './admin-franchises.component.html',
  styleUrl: './admin-franchises.component.scss'
})
export class AdminFranchisesComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);

  readonly statuses: CatalogRecordStatus[] = ['DRAFT', 'ACTIVE', 'ARCHIVED'];
  readonly franchises = signal<CatalogFranchiseResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly editing = signal<CatalogFranchiseResponse | null>(null);
  readonly messageKey = signal<string | null>(null);
  readonly errorKey = signal<string | null>(null);

  readonly filters = this.fb.group({
    q: [''],
    recordStatus: ['' as CatalogRecordStatus | '']
  });
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    slug: ['', [Validators.required, Validators.pattern(/^[a-z0-9]+(?:-[a-z0-9]+)*$/)]],
    description: ['', [Validators.maxLength(4000)]],
    recordStatus: ['DRAFT' as CatalogRecordStatus, [Validators.required]]
  });

  ngOnInit(): void {
    this.load();
  }

  load(page = this.page()): void {
    const filters = this.filters.getRawValue();
    this.loading.set(true);
    this.errorKey.set(null);
    this.service
      .searchFranchises({
        q: filters.q,
        recordStatus: filters.recordStatus || null,
        page
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.franchises.set(response.content);
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
    this.form.reset({ name: '', slug: '', description: '', recordStatus: 'DRAFT' });
    this.messageKey.set(null);
    this.errorKey.set(null);
  }

  startEdit(franchise: CatalogFranchiseResponse): void {
    this.editing.set(franchise);
    this.form.reset({
      name: franchise.name,
      slug: franchise.slug,
      description: franchise.description ?? '',
      recordStatus: franchise.recordStatus
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
      slug: value.slug.trim(),
      description: this.optionalText(value.description),
      recordStatus: value.recordStatus
    };
    const current = this.editing();
    const operation = current
      ? this.service.updateFranchise(current.id, request)
      : this.service.createFranchise(request);

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
}
