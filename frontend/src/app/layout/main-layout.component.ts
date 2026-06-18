import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../core/auth/auth.service';
import { LanguageSelectorComponent } from '../core/i18n/language-selector.component';
import { TranslatePipe } from '../core/i18n/translate.pipe';

interface NavItem {
  labelKey: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-main-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    LanguageSelectorComponent,
    TranslatePipe
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  readonly authService = inject(AuthService);
  readonly publicNavItems: NavItem[] = [
    { labelKey: 'layout.nav.catalog', route: '/catalog', icon: 'travel_explore' }
  ];
  readonly privateNavItems: NavItem[] = [
    { labelKey: 'layout.nav.dashboard', route: '/dashboard', icon: 'space_dashboard' },
    { labelKey: 'layout.nav.shops', route: '/shops', icon: 'storefront' },
    { labelKey: 'layout.nav.collections', route: '/collections', icon: 'collections_bookmark' },
    { labelKey: 'layout.nav.recommendations', route: '/recommendations', icon: 'auto_awesome' },
    { labelKey: 'layout.nav.reservations', route: '/reservations', icon: 'event_available' }
  ];
  readonly mobileNavItems: NavItem[] = [
    { labelKey: 'layout.nav.home', route: '/dashboard', icon: 'home' },
    { labelKey: 'layout.nav.catalog', route: '/catalog', icon: 'travel_explore' },
    { labelKey: 'layout.nav.shopsShort', route: '/shops', icon: 'storefront' },
    { labelKey: 'layout.nav.reservationsShort', route: '/reservations', icon: 'event_available' }
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
