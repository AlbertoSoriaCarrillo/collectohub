import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import {
  EditorialCatalogSearchItem,
  EditorialCatalogSearchParams,
  EditorialPublicResultType,
  EditorialSeriesType,
  PageResponse
} from '../../../core/models/editorial-catalog.model';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';

@Component({
  selector: 'app-editorial-search',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './editorial-search.component.html',
  styleUrls: ['./editorial-search.component.scss', '../editorial-shared.scss']
})
export class EditorialSearchComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly editorialCatalogService = inject(EditorialCatalogService);
  private readonly languageService = inject(LanguageService);

  readonly result = signal<PageResponse<EditorialCatalogSearchItem> | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    q: [''],
    type: [''],
    resultType: ['']
  });
  readonly pageSize = 12;

  ngOnInit(): void {
    this.search();
  }

  search(resetPage = true): void {
    const currentPage = resetPage ? 0 : (this.result()?.page ?? 0);
    const formValue = this.form.getRawValue();
    const params: EditorialCatalogSearchParams = {
      q: formValue.q,
      type: (formValue.type || null) as EditorialSeriesType | null,
      resultType: (formValue.resultType || null) as EditorialPublicResultType | null,
      page: currentPage,
      size: this.pageSize,
      sort: 'title,asc'
    };

    this.loading.set(true);
    this.errorMessage.set(null);
    this.editorialCatalogService
      .search(params)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (result) => this.result.set(result),
        error: () => this.errorMessage.set(this.languageService.translate('editorial.error'))
      });
  }

  clear(): void {
    this.form.reset({ q: '', type: '', resultType: '' });
    this.search();
  }

  changePage(page: number): void {
    const current = this.result();
    if (!current || page < 0 || page >= current.totalPages || page === current.page) {
      return;
    }
    this.result.update((value) => (value ? { ...value, page } : value));
    this.search(false);
  }

  resultLink(item: EditorialCatalogSearchItem): (string | number)[] {
    if (item.resultType === 'SERIES' && item.seriesId) {
      return ['/catalog/editorial/series', item.seriesId];
    }
    if (item.resultType === 'ITEM' && item.itemId) {
      return ['/catalog/editorial/items', item.itemId];
    }
    if (item.resultType === 'EDITION' && item.editionId) {
      return ['/catalog/editorial/editions', item.editionId];
    }
    return ['/catalog/editorial'];
  }

  displayTitle(item: EditorialCatalogSearchItem): string {
    return item.editionName || item.itemTitle || item.seriesTitle || '';
  }
}
