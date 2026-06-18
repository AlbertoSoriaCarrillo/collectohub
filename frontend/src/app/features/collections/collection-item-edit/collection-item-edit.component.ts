import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
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
  COLLECTION_ITEM_STATUSES,
  CollectionItemResponse,
  UpdateCollectionItemRequest
} from '../../../core/models/collection.model';
import { PHYSICAL_CONDITIONS } from '../../../core/models/inventory.model';
import { CollectionService } from '../../../core/services/collection.service';

@Component({
  selector: 'app-collection-item-edit',
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
  templateUrl: './collection-item-edit.component.html',
  styleUrl: './collection-item-edit.component.scss'
})
export class CollectionItemEditComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly collectionService = inject(CollectionService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly statuses = COLLECTION_ITEM_STATUSES;
  readonly conditions = PHYSICAL_CONDITIONS;
  readonly collectionId = signal<number | null>(null);
  readonly itemId = signal<number | null>(null);
  readonly item = signal<CollectionItemResponse | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    collectionStatus: ['', [Validators.required]],
    physicalCondition: [''],
    unitNumber: ['', [Validators.maxLength(50)]],
    totalLimitedUnits: [null as number | null, [Validators.min(1)]],
    notes: ['', [Validators.maxLength(4000)]],
    acquiredAt: ['']
  });

  ngOnInit(): void {
    const collectionId = Number(this.route.snapshot.paramMap.get('collectionId'));
    const itemId = Number(this.route.snapshot.paramMap.get('itemId'));
    if (
      !Number.isFinite(collectionId) ||
      collectionId <= 0 ||
      !Number.isFinite(itemId) ||
      itemId <= 0
    ) {
      this.errorMessage.set(this.languageService.translate('collections.itemNotFound'));
      return;
    }

    this.collectionId.set(collectionId);
    this.itemId.set(itemId);
    this.loadItem(collectionId, itemId);
  }

  submit(): void {
    const collectionId = this.collectionId();
    const itemId = this.itemId();
    if (!collectionId || !itemId) {
      this.errorMessage.set(this.languageService.translate('collections.itemNotFound'));
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .updateCollectionItem(collectionId, itemId, this.toRequest())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/collections', collectionId]),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private loadItem(collectionId: number, itemId: number): void {
    this.loading.set(true);
    this.collectionService
      .getCollectionItems(collectionId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (items) => {
          const item = items.find((candidate) => candidate.id === itemId);
          if (!item) {
            this.errorMessage.set(this.languageService.translate('collections.itemNotFound'));
            return;
          }

          this.item.set(item);
          this.form.patchValue({
            collectionStatus: item.collectionStatus,
            physicalCondition: item.physicalCondition ?? '',
            unitNumber: item.unitNumber ?? '',
            totalLimitedUnits: item.totalLimitedUnits,
            notes: item.notes ?? '',
            acquiredAt: item.acquiredAt ?? ''
          });
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private toRequest(): UpdateCollectionItemRequest {
    const value = this.form.getRawValue();
    return {
      collectionStatus: value.collectionStatus as UpdateCollectionItemRequest['collectionStatus'],
      physicalCondition:
        (this.optionalText(value.physicalCondition) as UpdateCollectionItemRequest['physicalCondition']) ??
        null,
      unitNumber: this.optionalText(value.unitNumber),
      totalLimitedUnits: value.totalLimitedUnits ? Number(value.totalLimitedUnits) : null,
      notes: this.optionalText(value.notes),
      acquiredAt: this.optionalText(value.acquiredAt)
    };
  }

  private optionalText(value: string | null | undefined): string | null {
    const normalized = (value ?? '').trim();
    return normalized ? normalized : null;
  }
}
