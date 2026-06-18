import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { CollectionItemResponse, CollectionResponse } from '../../../core/models/collection.model';
import { CollectionService } from '../../../core/services/collection.service';

@Component({
  selector: 'app-collection-detail',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule, TranslatePipe],
  templateUrl: './collection-detail.component.html',
  styleUrl: './collection-detail.component.scss'
})
export class CollectionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly collectionService = inject(CollectionService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly collection = signal<CollectionResponse | null>(null);
  readonly items = signal<CollectionItemResponse[]>([]);
  readonly isOwner = signal(false);
  readonly loading = signal(false);
  readonly itemsLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const collectionId = Number(this.route.snapshot.paramMap.get('collectionId'));
    if (!Number.isFinite(collectionId) || collectionId <= 0) {
      this.errorMessage.set(this.languageService.translate('collections.collectionNotFound'));
      return;
    }

    this.loadCollection(collectionId);
  }

  deleteItem(item: CollectionItemResponse): void {
    const collection = this.collection();
    if (
      !collection ||
      !window.confirm(
        this.languageService.translate('collections.itemDeleteConfirm', {
          name: item.masterProductName
        })
      )
    ) {
      return;
    }

    this.collectionService.deleteCollectionItem(collection.id, item.id).subscribe({
      next: () => this.items.set(this.items().filter((candidate) => candidate.id !== item.id)),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  private loadCollection(collectionId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.collectionService
      .getCollection(collectionId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (collection) => {
          this.collection.set(collection);
          this.items.set(collection.items ?? []);
          this.updateOwner(collection);
          this.loadItems(collection.id);
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private loadItems(collectionId: number): void {
    this.itemsLoading.set(true);
    this.collectionService
      .getCollectionItems(collectionId)
      .pipe(finalize(() => this.itemsLoading.set(false)))
      .subscribe({
        next: (items) => this.items.set(items),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  private updateOwner(collection: CollectionResponse): void {
    const currentUser = this.authService.currentUser();
    if (currentUser) {
      this.isOwner.set(currentUser.id === collection.userId);
      return;
    }

    if (!this.authService.hasToken()) {
      this.isOwner.set(false);
      return;
    }

    this.authService.getMe().subscribe({
      next: (user) => this.isOwner.set(user.id === collection.userId),
      error: () => this.isOwner.set(false)
    });
  }
}
