import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, NonNullableFormBuilder } from '@angular/forms';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { EditorialDataQualityReportResponse, EditorialDataQualityScope } from '../../../../core/models/editorial-admin.model';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';

@Component({ selector: 'app-admin-data-quality', imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatProgressSpinnerModule, MatSelectModule, TranslatePipe], templateUrl: './admin-data-quality.component.html', styleUrl: './admin-data-quality.component.scss' })
export class AdminDataQualityComponent {
  private readonly fb = inject(NonNullableFormBuilder); private readonly service = inject(EditorialAdminService);
  readonly scopes: EditorialDataQualityScope[] = ['ALL', 'PUBLISHERS', 'FRANCHISES', 'SERIES', 'ITEMS', 'EDITIONS', 'CREATORS', 'MASTER_LINKS'];
  readonly form = this.fb.group({ scope: ['ALL' as EditorialDataQualityScope] });
  readonly report = signal<EditorialDataQualityReportResponse | null>(null); readonly loading = signal(false); readonly error = signal(false);
  constructor() { this.load(); }
  load(): void { this.loading.set(true); this.error.set(false); this.service.getEditorialDataQualityReport(this.form.controls.scope.value).pipe(finalize(() => this.loading.set(false))).subscribe({ next: value => this.report.set(value), error: () => this.error.set(true) }); }
}
