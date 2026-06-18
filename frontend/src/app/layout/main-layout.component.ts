import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../core/auth/auth.service';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatCardModule, MatIconModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  readonly authService = inject(AuthService);
  readonly publicNavItems: NavItem[] = [
    { label: 'Catalogo', route: '/catalog', icon: 'travel_explore' }
  ];
  readonly privateNavItems: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'space_dashboard' },
    { label: 'Mis tiendas', route: '/shops', icon: 'storefront' },
    { label: 'Colecciones', route: '/collections', icon: 'collections_bookmark' },
    { label: 'Recomendaciones', route: '/recommendations', icon: 'auto_awesome' },
    { label: 'Mis reservas', route: '/reservations', icon: 'event_available' }
  ];
  readonly mobileNavItems: NavItem[] = [
    { label: 'Inicio', route: '/dashboard', icon: 'home' },
    { label: 'Catalogo', route: '/catalog', icon: 'travel_explore' },
    { label: 'Tiendas', route: '/shops', icon: 'storefront' },
    { label: 'Reservas', route: '/reservations', icon: 'event_available' }
  ];

  userInitials(): string {
    const user = this.authService.currentUser();
    const source = user?.displayName || user?.email || 'C';
    return source
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  }

  logout(): void {
    this.authService.logout();
  }
}
