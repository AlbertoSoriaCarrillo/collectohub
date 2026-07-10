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
import { LanguageService } from '../../../../core/i18n/language.service';
import {
  BackfillMasterProductCatalogLinksResponse,
  EditorialLegacyBridgeResponse,
  MasterProductCatalogLinkResponse,
  MasterProductCatalogLinkSource,
  MasterProductCatalogLinkStatus
} from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({
  selector: 'app-admin-master-product-links',
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
  templateUrl: './admin-master-product-links.component.html',
  styleUrl: './admin-master-product-links.component.scss'
})
export class AdminMasterProductLinksComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(EditorialAdminService);
  private readonly languageService = inject(LanguageService);

  readonly statuses: MasterProductCatalogLinkStatus[] = ['PROPOSED', 'VERIFIED', 'REJECTED'];
  readonly sources: MasterProductCatalogLinkSource[] = [
    'MANUAL', 'ISBN', 'EAN', 'TITLE', 'TITLE_AND_VOLUME', 'TITLE_AND_PUBLISHER', 'BACKFILL'
  ];
  readonly links = signal<MasterProductCatalogLinkResponse[]>([]);
  readonly editing = signal<MasterProductCatalogLinkResponse | null>(null);
  readonly bridge = signal<EditorialLegacyBridgeResponse | null>(null);
  readonly backfillResult = signal<BackfillMasterProductCatalogLinksResponse | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly bridgeLoading = signal(false);
  readonly messageKey = signal<string | null>(null);
  readonly errorKey = signal<string | null>(null);

  readonly filters = this.fb.group({
    masterProductId: [null as number | null],
    catalogItemId: [null as number | null],
    catalogItemEditionId: [null as number | null],
    linkStatus: ['' as MasterProductCatalogLinkStatus | ''],
    linkSource: ['' as MasterProductCatalogLinkSource | '']
  });
  readonly form = this.fb.group({
    masterProductId: [null as number | null, [Validators.required, Validators.min(1)]],
    catalogItemId: [null as number | null, [Validators.required, Validators.min(1)]],
    catalogItemEditionId: [null as number | null, [Validators.min(1)]],
    linkStatus: ['PROPOSED' as MasterProductCatalogLinkStatus, [Validators.required]],
    linkSource: ['MANUAL' as MasterProductCatalogLinkSource, [Validators.required]],
    confidenceScore: [null as number | null, [Validators.min(0), Validators.max(1)]],
    matchReason: [''],
    reviewNote: ['']
  });

  constructor() {
    this.loadLinks();
  }

  loadLinks(): void {
    this.loading.set(true);
    this.errorKey.set(null);
    this.service
      .searchMasterProductLinks(this.filters.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => this.links.set(response.content),
        error: () => this.errorKey.set('admin.editorial.messages.loadError')
      });
  }

  startCreate(): void {
    this.editing.set(null);
    this.form.reset({
      masterProductId: null,
      catalogItemId: null,
      catalogItemEditionId: null,
      linkStatus: 'PROPOSED',
      linkSource: 'MANUAL',
      confidenceScore: null,
      matchReason: '',
      reviewNote: ''
    });
    this.errorKey.set(null);
  }

  startEdit(link: MasterProductCatalogLinkResponse): void {
    this.editing.set(link);
    this.form.reset({
      masterProductId: link.masterProductId,
      catalogItemId: link.catalogItemId,
      catalogItemEditionId: link.catalogItemEditionId,
      linkStatus: link.linkStatus,
      linkSource: link.linkSource,
      confidenceScore: link.confidenceScore,
      matchReason: link.matchReason ?? '',
      reviewNote: link.reviewNote ?? ''
    });
    this.form.controls.masterProductId.disable();
    this.errorKey.set(null);
  }

  cancel(): void {
    this.form.controls.masterProductId.enable();
    this.startCreate();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request = {
      catalogItemId: value.catalogItemId ?? 0,
      catalogItemEditionId: value.catalogItemEditionId,
      linkStatus: value.linkStatus,
      linkSource: value.linkSource,
      confidenceScore: value.confidenceScore,
      matchReason: this.optionalText(value.matchReason),
      reviewNote: this.optionalText(value.reviewNote)
    };
    const current = this.editing();
    const operation = current
      ? this.service.updateMasterProductLink(current.id, request)
      : this.service.createMasterProductLink({ masterProductId: value.masterProductId ?? 0, ...request });

    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.messageKey.set('admin.editorial.masterLinks.saved');
        this.cancel();
        this.loadLinks();
      },
      error: () => this.errorKey.set('admin.editorial.messages.saveError')
    });
  }

  verify(link: MasterProductCatalogLinkResponse): void {
    if (!window.confirm(this.languageService.translate('admin.editorial.masterLinks.verifyConfirm'))) {
      return;
    }
    this.updateLinkState(() => this.service.verifyMasterProductLink(link.id), 'verified');
  }

  reject(link: MasterProductCatalogLinkResponse): void {
    if (!window.confirm(this.languageService.translate('admin.editorial.masterLinks.rejectConfirm'))) {
      return;
    }
    this.updateLinkState(() => this.service.rejectMasterProductLink(link.id), 'rejected');
  }

  backfill(): void {
    if (!window.confirm(this.languageService.translate('admin.editorial.masterLinks.backfillConfirm'))) {
      return;
    }
    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    this.service.backfillMasterProductLinks().pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (result) => {
        this.backfillResult.set(result);
        this.messageKey.set('admin.editorial.masterLinks.backfillDone');
        this.loadLinks();
      },
      error: () => this.errorKey.set('admin.editorial.messages.saveError')
    });
  }

  lookupBridge(): void {
    const masterProductId = this.positiveNumber(this.filters.controls.masterProductId.value);
    if (!masterProductId) {
      this.errorKey.set('admin.editorial.masterLinks.masterProductId');
      return;
    }
    this.bridgeLoading.set(true);
    this.errorKey.set(null);
    this.bridge.set(null);
    this.service
      .getEditorialLegacyBridge(masterProductId)
      .pipe(finalize(() => this.bridgeLoading.set(false)))
      .subscribe({
        next: (bridge) => this.bridge.set(bridge),
        error: () => this.errorKey.set('admin.editorial.masterLinks.noBridge')
      });
  }

  private updateLinkState(
    operation: () => ReturnType<EditorialAdminService['verifyMasterProductLink']>,
    outcome: 'verified' | 'rejected'
  ): void {
    this.saving.set(true);
    this.errorKey.set(null);
    this.messageKey.set(null);
    operation().pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.messageKey.set(`admin.editorial.masterLinks.${outcome}`);
        this.loadLinks();
      },
      error: () => this.errorKey.set('admin.editorial.messages.saveError')
    });
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }

  private positiveNumber(value: number | null): number | null {
    return value !== null && value > 0 && !Number.isNaN(value) ? value : null;
  }
}
