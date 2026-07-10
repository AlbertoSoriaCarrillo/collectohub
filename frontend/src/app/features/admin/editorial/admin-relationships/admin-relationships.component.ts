import { Component, inject, signal } from '@angular/core';
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
  CatalogRecordStatus,
  EditorialAdminItemRelationshipResponse,
  EditorialAdminRelationshipType
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-relationships',
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
  templateUrl: './admin-relationships.component.html',
  styleUrl: './admin-relationships.component.scss'
})
export class AdminRelationshipsComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);

  readonly statuses: CatalogRecordStatus[] = ['DRAFT', 'ACTIVE', 'ARCHIVED'];
  readonly types: EditorialAdminRelationshipType[] = [
    'ADAPTATION',
    'REMAKE',
    'REPRINT',
    'SAME_WORK',
    'SPIN_OFF',
    'PREQUEL',
    'SEQUEL',
    'RELATED'
  ];

  readonly sourceResults = signal<CatalogItemResponse[]>([]);
  readonly targetResults = signal<CatalogItemResponse[]>([]);
  readonly selectedSource = signal<CatalogItemResponse | null>(null);
  readonly relationships = signal<EditorialAdminItemRelationshipResponse[]>([]);
  readonly loadingItems = signal(false);
  readonly loadingRelationships = signal(false);
  readonly saving = signal(false);
  readonly editing = signal<EditorialAdminItemRelationshipResponse | null>(null);
  readonly messageKey = signal<string | null>('admin.editorial.relationships.noSource');
  readonly errorKey = signal<string | null>(null);

  readonly sourceSearch = this.fb.group({
    seriesId: [null as number | null, [Validators.required]],
    q: ['']
  });
  readonly targetSearch = this.fb.group({
    seriesId: [null as number | null, [Validators.required]],
    q: ['']
  });
  readonly filters = this.fb.group({
    recordStatus: ['' as CatalogRecordStatus | '']
  });
  readonly form = this.fb.group({
    targetCatalogItemId: [null as number | null, [Validators.required]],
    relationshipType: ['RELATED' as EditorialAdminRelationshipType, [Validators.required]],
    relationshipOrder: [1, [Validators.required, Validators.min(1)]],
    description: [''],
    recordStatus: ['DRAFT' as CatalogRecordStatus, [Validators.required]]
  });

  searchSourceItems(): void {
    this.searchItems(this.sourceSearch.getRawValue(), this.sourceResults);
  }

  searchTargetItems(): void {
    this.searchItems(this.targetSearch.getRawValue(), this.targetResults);
  }

  selectSource(item: CatalogItemResponse): void {
    this.selectedSource.set(item);
    this.messageKey.set(null);
    this.startCreate();
    this.loadRelationships();
  }

  selectTarget(item: CatalogItemResponse): void {
    this.form.controls.targetCatalogItemId.setValue(item.id);
  }

  loadRelationships(): void {
    const source = this.selectedSource();
    if (!source) {
      this.relationships.set([]);
      this.messageKey.set('admin.editorial.relationships.noSource');
      return;
    }

    this.loadingRelationships.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service
      .getItemRelationships(source.id, this.filters.controls.recordStatus.value || null)
      .pipe(finalize(() => this.loadingRelationships.set(false)))
      .subscribe({
        next: (relationships) => this.relationships.set(relationships),
        error: () => this.errorKey.set('admin.editorial.messages.loadError')
      });
  }

  startCreate(): void {
    this.editing.set(null);
    this.form.reset({
      targetCatalogItemId: null,
      relationshipType: 'RELATED',
      relationshipOrder: 1,
      description: '',
      recordStatus: 'DRAFT'
    });
    this.errorKey.set(null);
  }

  startEdit(relationship: EditorialAdminItemRelationshipResponse): void {
    this.editing.set(relationship);
    this.form.reset({
      targetCatalogItemId: relationship.targetCatalogItemId,
      relationshipType: relationship.relationshipType,
      relationshipOrder: relationship.relationshipOrder,
      description: relationship.description ?? '',
      recordStatus: relationship.recordStatus
    });
    this.errorKey.set(null);
  }

  cancel(): void {
    this.startCreate();
  }

  submit(): void {
    const source = this.selectedSource();
    if (!source) {
      this.messageKey.set('admin.editorial.relationships.selectSourceFirst');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    if (value.targetCatalogItemId === source.id) {
      this.errorKey.set('admin.editorial.relationships.sameItemWarning');
      return;
    }

    const request = {
      targetCatalogItemId: value.targetCatalogItemId ?? 0,
      relationshipType: value.relationshipType,
      relationshipOrder: value.relationshipOrder,
      description: this.optionalText(value.description),
      recordStatus: value.recordStatus
    };
    const current = this.editing();
    const operation = current
      ? this.service.updateItemRelationship(source.id, current.id, request)
      : this.service.createItemRelationship(source.id, request);

    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.messageKey.set('admin.editorial.relationships.saved');
        this.startCreate();
        this.loadRelationships();
      },
      error: () => this.errorKey.set('admin.editorial.messages.saveError')
    });
  }

  deleteSelected(): void {
    const source = this.selectedSource();
    const current = this.editing();
    if (!source || !current || !window.confirm('Delete relationship?')) {
      return;
    }

    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service
      .deleteItemRelationship(source.id, current.id)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.messageKey.set('admin.editorial.relationships.deleted');
          this.startCreate();
          this.loadRelationships();
        },
        error: () => this.errorKey.set('admin.editorial.messages.deleteError')
      });
  }

  private searchItems(
    criteria: { seriesId: number | null; q: string },
    target: { set(value: CatalogItemResponse[]): void }
  ): void {
    const seriesId = this.optionalNumber(criteria.seriesId);
    if (!seriesId) {
      target.set([]);
      this.messageKey.set('admin.editorial.messages.contextRequired');
      return;
    }

    this.loadingItems.set(true);
    this.errorKey.set(null);
    this.service
      .searchItems(seriesId, { q: criteria.q, page: 0, size: 10 })
      .pipe(finalize(() => this.loadingItems.set(false)))
      .subscribe({
        next: (response) => target.set(response.content),
        error: () => this.errorKey.set('admin.editorial.messages.loadError')
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
