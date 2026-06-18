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

interface DashboardSection {
  titleKey: string;
  descriptionKey: string;
  route: string | null;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule, TranslatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly user = signal<UserMeResponse | null>(this.authService.currentUser());
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly sections: DashboardSection[] = [
    { titleKey: 'dashboard.sections.shopsTitle', descriptionKey: 'dashboard.sections.shopsDescription', route: '/shops' },
    { titleKey: 'dashboard.sections.catalogTitle', descriptionKey: 'dashboard.sections.catalogDescription', route: '/catalog' },
    { titleKey: 'dashboard.sections.inventoryTitle', descriptionKey: 'dashboard.sections.inventoryDescription', route: '/shops' },
    {
      titleKey: 'dashboard.sections.collectionsTitle',
      descriptionKey: 'dashboard.sections.collectionsDescription',
      route: '/collections'
    },
    {
      titleKey: 'dashboard.sections.recommendationsTitle',
      descriptionKey: 'dashboard.sections.recommendationsDescription',
      route: '/recommendations'
    },
    {
      titleKey: 'dashboard.sections.reservationsTitle',
      descriptionKey: 'dashboard.sections.reservationsDescription',
      route: '/reservations'
    }
  ];

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

  logout(): void {
    this.authService.logout();
  }
}
