import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import {
  MasterProductResponse,
  ProductCategoryResponse
} from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';

@Component({
  selector: 'app-catalog-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './catalog-list.component.html',
  styleUrl: './catalog-list.component.scss'
})
export class CatalogListComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly catalogService = inject(CatalogService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly categories = signal<ProductCategoryResponse[]>([]);
  readonly products = signal<MasterProductResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    categoryCode: [''],
    name: [''],
    franchise: [''],
    collectionName: [''],
    language: [''],
    status: ['ACTIVE']
  });

  ngOnInit(): void {
    this.loadCategories();
    this.search();
  }

  canCreateProduct(): boolean {
    return this.authService.hasAnyRole(['ADMIN', 'SHOP_OWNER']);
  }

  search(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.catalogService
      .searchMasterProducts(this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (products) => this.products.set(products),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  resetFilters(): void {
    this.form.reset({
      categoryCode: '',
      name: '',
      franchise: '',
      collectionName: '',
      language: '',
      status: 'ACTIVE'
    });
    this.search();
  }

  private loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }
}
