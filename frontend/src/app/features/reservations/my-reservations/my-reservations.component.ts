import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
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
  ReservationSearchFilters,
  ReservationStatus,
  USER_CANCELLABLE_RESERVATION_STATUSES
} from '../../../core/models/reservation.model';
import { ReservationService } from '../../../core/services/reservation.service';

@Component({
  selector: 'app-my-reservations',
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
  templateUrl: './my-reservations.component.html',
  styleUrl: './my-reservations.component.scss'
})
export class MyReservationsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reservationService = inject(ReservationService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly statuses = RESERVATION_STATUSES;
  readonly reservations = signal<ReservationResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly filters = this.fb.group({
    status: [''],
    shopId: [null as number | null]
  });

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.reservationService
      .getMyReservations(this.toFilters())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (reservations) => this.reservations.set(reservations),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  resetFilters(): void {
    this.filters.reset({ status: '', shopId: null });
    this.loadReservations();
  }

  canCancel(reservation: ReservationResponse): boolean {
    return USER_CANCELLABLE_RESERVATION_STATUSES.includes(reservation.status);
  }

  cancelReservation(reservation: ReservationResponse): void {
    if (
      !this.canCancel(reservation) ||
      !window.confirm(this.languageService.translate('reservations.cancelConfirm'))
    ) {
      return;
    }

    this.reservationService.cancelMyReservation(reservation.id).subscribe({
      next: (updated) =>
        this.reservations.set(
          this.reservations().map((candidate) =>
            candidate.id === updated.id ? updated : candidate
          )
        ),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  formatDate(value: string | null): string {
    return value ? new Date(value).toLocaleString() : this.languageService.translate('common.notReported');
  }

  private toFilters(): ReservationSearchFilters {
    const value = this.filters.getRawValue();
    return {
      status: value.status ? (value.status as ReservationStatus) : null,
      shopId: this.positiveNumberOrNull(value.shopId)
    };
  }

  private positiveNumberOrNull(value: number | null | undefined): number | null {
    const numberValue = Number(value);
    return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : null;
  }
}
