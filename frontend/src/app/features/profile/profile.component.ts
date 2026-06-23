import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../core/auth/auth.service';
import { ErrorMessageService } from '../../core/http/error-message.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { UserMeResponse } from '../../core/models/user-me-response.model';

@Component({
  selector: 'app-profile',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule, TranslatePipe],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly user = signal<UserMeResponse | null>(this.authService.currentUser());
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.authService
      .getMe()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (user) => this.user.set(user),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }
}
