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
import {
  ReservationResponse,
  USER_CANCELLABLE_RESERVATION_STATUSES
} from '../../../core/models/reservation.model';
import { ReservationService } from '../../../core/services/reservation.service';

@Component({
  selector: 'app-reservation-detail',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule, TranslatePipe],
  templateUrl: './reservation-detail.component.html',
  styleUrl: './reservation-detail.component.scss'
})
export class ReservationDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly reservationService = inject(ReservationService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);

  readonly reservation = signal<ReservationResponse | null>(null);
  readonly currentUserId = signal<number | null>(this.authService.currentUser()?.id ?? null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(history.state?.successMessage ?? null);

  ngOnInit(): void {
    this.loadCurrentUser();

    const reservationId = Number(this.route.snapshot.paramMap.get('reservationId'));
    if (!Number.isFinite(reservationId) || reservationId <= 0) {
      this.errorMessage.set(this.languageService.translate('reservations.notFound'));
      return;
    }

    this.loading.set(true);
    this.reservationService
      .getReservation(reservationId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (reservation) => this.reservation.set(reservation),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }

  canCancel(reservation: ReservationResponse): boolean {
    return (
      reservation.userId === this.currentUserId() &&
      USER_CANCELLABLE_RESERVATION_STATUSES.includes(reservation.status)
    );
  }

  cancelReservation(reservation: ReservationResponse): void {
    if (
      !this.canCancel(reservation) ||
      !window.confirm(this.languageService.translate('reservations.cancelConfirm'))
    ) {
      return;
    }

    this.reservationService.cancelMyReservation(reservation.id).subscribe({
      next: (updated) => this.reservation.set(updated),
      error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
    });
  }

  formatDate(value: string | null): string {
    return value ? new Date(value).toLocaleString() : this.languageService.translate('common.notReported');
  }

  private loadCurrentUser(): void {
    if (this.currentUserId() || !this.authService.hasToken()) {
      return;
    }

    this.authService.getMe().subscribe({
      next: (user) => this.currentUserId.set(user.id),
      error: () => this.currentUserId.set(null)
    });
  }
}
