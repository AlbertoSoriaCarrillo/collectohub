import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { EditorialCatalogEditionDetail } from '../../../core/models/editorial-catalog.model';
import { EditorialCatalogService } from '../../../core/services/editorial-catalog.service';

@Component({
  selector: 'app-editorial-edition-detail',
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule, TranslatePipe],
  templateUrl: './editorial-edition-detail.component.html',
  styleUrls: ['./editorial-edition-detail.component.scss', '../editorial-shared.scss']
})
export class EditorialEditionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly editorialCatalogService = inject(EditorialCatalogService);
  private readonly languageService = inject(LanguageService);

  readonly detail = signal<EditorialCatalogEditionDetail | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const editionId = Number(this.route.snapshot.paramMap.get('editionId'));
    if (!Number.isInteger(editionId) || editionId <= 0) {
      this.errorMessage.set(this.languageService.translate('editorial.notFound'));
      return;
    }
    this.loading.set(true);
    this.editorialCatalogService
      .getEditionDetail(editionId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (detail) => this.detail.set(detail),
        error: () => this.errorMessage.set(this.languageService.translate('editorial.notFound'))
      });
  }
}
