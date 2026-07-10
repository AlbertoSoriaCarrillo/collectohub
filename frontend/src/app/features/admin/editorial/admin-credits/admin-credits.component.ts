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
  CatalogItemCreatorResponse,
  CreatorCreditRole
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-credits',
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
  templateUrl: './admin-credits.component.html',
  styleUrl: './admin-credits.component.scss'
})
export class AdminCreditsComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);

  readonly roles: CreatorCreditRole[] = [
    'AUTHOR',
    'WRITER',
    'ARTIST',
    'ILLUSTRATOR',
    'TRANSLATOR',
    'EDITOR',
    'OTHER'
  ];
  readonly credits = signal<CatalogItemCreatorResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly editing = signal<CatalogItemCreatorResponse | null>(null);
  readonly messageKey = signal<string | null>('admin.editorial.messages.contextRequired');
  readonly errorKey = signal<string | null>(null);

  readonly contextForm = this.fb.group({
    itemId: [null as number | null, [Validators.required]]
  });
  readonly form = this.fb.group({
    creatorId: [null as number | null, [Validators.required]],
    creditRole: ['AUTHOR' as CreatorCreditRole, [Validators.required]],
    creditOrder: [1, [Validators.required, Validators.min(1)]],
    creditLabel: ['']
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const itemId = this.optionalNumber(this.contextForm.controls.itemId.value);
    if (!itemId) {
      this.credits.set([]);
      this.messageKey.set('admin.editorial.messages.contextRequired');
      return;
    }

    this.loading.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service
      .listItemCreatorCredits(itemId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (credits) => this.credits.set(credits),
        error: () => this.errorKey.set('admin.editorial.messages.loadError')
      });
  }

  startCreate(): void {
    this.editing.set(null);
    this.form.reset({
      creatorId: null,
      creditRole: 'AUTHOR',
      creditOrder: 1,
      creditLabel: ''
    });
    this.errorKey.set(null);
  }

  startEdit(credit: CatalogItemCreatorResponse): void {
    this.editing.set(credit);
    this.form.reset({
      creatorId: credit.creatorId,
      creditRole: credit.creditRole,
      creditOrder: credit.creditOrder,
      creditLabel: credit.creditLabel ?? ''
    });
    this.form.controls.creatorId.disable();
    this.errorKey.set(null);
  }

  cancel(): void {
    this.form.controls.creatorId.enable();
    this.startCreate();
  }

  submit(): void {
    const itemId = this.optionalNumber(this.contextForm.controls.itemId.value);
    if (!itemId) {
      this.messageKey.set('admin.editorial.messages.contextRequired');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const current = this.editing();
    const operation = current
      ? this.service.updateItemCreatorCredit(itemId, current.id, {
          creditRole: value.creditRole,
          creditOrder: value.creditOrder,
          creditLabel: this.optionalText(value.creditLabel)
        })
      : this.service.createItemCreatorCredit(itemId, {
          creatorId: value.creatorId ?? 0,
          creditRole: value.creditRole,
          creditOrder: value.creditOrder,
          creditLabel: this.optionalText(value.creditLabel)
        });

    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.messageKey.set('admin.editorial.messages.saved');
        this.form.controls.creatorId.enable();
        this.startCreate();
        this.load();
      },
      error: () => this.errorKey.set('admin.editorial.messages.saveError')
    });
  }

  deleteSelected(): void {
    const itemId = this.optionalNumber(this.contextForm.controls.itemId.value);
    const current = this.editing();
    if (!itemId || !current || !window.confirm('Delete credit?')) {
      return;
    }

    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service
      .deleteItemCreatorCredit(itemId, current.id)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.messageKey.set('admin.editorial.messages.deleted');
          this.form.controls.creatorId.enable();
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

  private optionalNumber(value: number | null): number | null {
    return value === null || Number.isNaN(value) ? null : value;
  }
}
