import { Component, OnInit, inject, signal } from '@angular/core';
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
}

@Component({
  selector: 'app-dashboard',
  imports: [MatButtonModule, MatCardModule, MatChipsModule],
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
    { title: 'Mis tiendas', description: 'Gestion de tiendas y miembros.' },
    { title: 'Catalogo', description: 'Productos maestros reutilizables.' },
    { title: 'Inventario', description: 'Stock visible y estados comerciales.' },
    { title: 'Mis colecciones', description: 'Colecciones privadas y publicas.' },
    { title: 'Recomendaciones', description: 'Coincidencias con productos buscados.' },
    { title: 'Reservas', description: 'Solicitudes sin pago del MVP.' }
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
