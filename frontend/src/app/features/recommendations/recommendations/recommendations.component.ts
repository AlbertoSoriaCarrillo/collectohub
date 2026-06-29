import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import {
  RecommendationFilters,
  RecommendedShopProductResponse,
  UserRecommendationSummaryResponse
} from '../../../core/models/recommendation.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { RecommendationService } from '../../../core/services/recommendation.service';

@Component({
  selector: 'app-recommendations',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './recommendations.component.html',
  styleUrl: './recommendations.component.scss'
})
export class RecommendationsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly recommendationService = inject(RecommendationService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly categories = signal<ProductCategoryResponse[]>([]);
  readonly recommendations = signal<RecommendedShopProductResponse[]>([]);
  readonly summary = signal<UserRecommendationSummaryResponse | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly filters = this.fb.group({
    categoryCode: ['']
  });

  ngOnInit(): void {
    this.loadCategories();
    this.loadRecommendations();
  }

  loadRecommendations(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const filters = this.toFilters();
    forkJoin({
      recommendations: this.recommendationService.getMyRecommendations(filters),
      summary: this.recommendationService.getMyRecommendationSummary(filters)
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ recommendations, summary }) => {
          this.recommendations.set(recommendations.recommendations);
          this.summary.set(summary);
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  resetFilters(): void {
    this.filters.reset({
      categoryCode: ''
    });
    this.loadRecommendations();
  }

  hasCollectionNeeds(): boolean {
    const summary = this.summary();
    return Boolean(summary && summary.missingCollectionItems + summary.wantedCollectionItems > 0);
  }

  private loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  private toFilters(): RecommendationFilters {
    const value = this.filters.getRawValue();
    return {
      categoryCode: value.categoryCode || null
    };
  }
}
