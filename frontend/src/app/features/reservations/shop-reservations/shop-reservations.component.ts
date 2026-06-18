import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import {
  RESERVATION_STATUSES,
  ReservationResponse,
  ReservationStatus,
  ShopReservationSearchFilters
} from '../../../core/models/reservation.model';
import { ReservationService } from '../../../core/services/reservation.service';

interface ReservationAction {
  status: ReservationStatus;
  labelKey: string;
}

@Component({
  selector: 'app-shop-reservations',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './shop-reservations.component.html',
  styleUrl: './shop-reservations.component.scss'
})
export class ShopReservationsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly reservationService = inject(ReservationService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly statuses = RESERVATION_STATUSES;
  readonly shopId = signal<number | null>(null);
  readonly reservations = signal<ReservationResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly filters = this.fb.group({
    status: [''],
    userId: [null as number | null],
    shopProductId: [null as number | null]
  });
  readonly shopResponseForm = this.fb.group({
    shopResponse: ['']
  });

  ngOnInit(): void {
    const shopId = Number(this.route.snapshot.paramMap.get('shopId'));
    if (!Number.isFinite(shopId) || shopId <= 0) {
      this.errorMessage.set(this.languageService.translate('shops.notFound'));
      return;
    }

    this.shopId.set(shopId);
    this.loadReservations();
  }

  loadReservations(): void {
    const shopId = this.shopId();
    if (!shopId) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.reservationService
      .getShopReservations(shopId, this.toFilters())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (reservations) => this.reservations.set(reservations),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  resetFilters(): void {
    this.filters.reset({ status: '', userId: null, shopProductId: null });
    this.loadReservations();
  }

  availableActions(reservation: ReservationResponse): ReservationAction[] {
    switch (reservation.status) {
      case 'PENDING':
        return [
          { status: 'ACCEPTED', labelKey: 'reservations.accept' },
          { status: 'REJECTED', labelKey: 'reservations.reject' }
        ];
      case 'ACCEPTED':
        return [
          { status: 'COMPLETED', labelKey: 'reservations.complete' },
          { status: 'CANCELLED', labelKey: 'reservations.cancel' }
        ];
      default:
        return [];
    }
  }

  updateStatus(reservation: ReservationResponse, action: ReservationAction): void {
    const shopId = this.shopId();
    if (
      !shopId ||
      !window.confirm(
        this.languageService.translate('reservations.updateConfirm', {
          status: this.languageService.translate(`enums.reservationStatus.${action.status}`)
        })
      )
    ) {
      return;
    }

    const shopResponse = this.shopResponseForm.getRawValue().shopResponse?.trim() || null;
    this.reservationService
      .updateShopReservationStatus(shopId, reservation.id, {
        status: action.status,
        shopResponse
      })
      .subscribe({
        next: (updated) => {
          this.reservations.set(
            this.reservations().map((candidate) =>
              candidate.id === updated.id ? updated : candidate
            )
          );
          this.shopResponseForm.reset({ shopResponse: '' });
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  formatDate(value: string | null): string {
    return value ? new Date(value).toLocaleString() : this.languageService.translate('common.notReported');
  }

  private toFilters(): ShopReservationSearchFilters {
    const value = this.filters.getRawValue();
    return {
      status: value.status ? (value.status as ReservationStatus) : null,
      userId: this.positiveNumberOrNull(value.userId),
      shopProductId: this.positiveNumberOrNull(value.shopProductId)
    };
  }

  private positiveNumberOrNull(value: number | null | undefined): number | null {
    const numberValue = Number(value);
    return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : null;
  }
}
