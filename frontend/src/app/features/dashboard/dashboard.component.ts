import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../core/auth/auth.service';
import { ErrorMessageService } from '../../core/http/error-message.service';
import { UserMeResponse } from '../../core/models/user-me-response.model';

interface DashboardSection {
  title: string;
  description: string;
  route: string | null;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatChipsModule],
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
    { title: 'Mis tiendas', description: 'Gestion de tiendas y miembros.', route: '/shops' },
    { title: 'Catalogo', description: 'Productos maestros reutilizables.', route: '/catalog' },
    { title: 'Inventario', description: 'Stock visible y estados comerciales.', route: '/shops' },
    { title: 'Mis colecciones', description: 'Colecciones privadas y publicas.', route: '/collections' },
    {
      title: 'Recomendaciones',
      description: 'Coincidencias con productos buscados.',
      route: '/recommendations'
    },
    { title: 'Reservas', description: 'Solicitudes sin pago del MVP.', route: '/reservations' }
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
