import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
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
    MatIconModule,
    MatMenuModule,
    LanguageSelectorComponent,
    TranslatePipe
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  readonly authService = inject(AuthService);
  readonly publicNavItems: NavItem[] = [
    { labelKey: 'layout.nav.home', route: '/home', icon: 'home' },
    { labelKey: 'layout.nav.catalog', route: '/catalog', icon: 'travel_explore' }
  ];
  readonly privateNavItems: NavItem[] = [
    { labelKey: 'layout.nav.collections', route: '/collections', icon: 'collections_bookmark' },
    { labelKey: 'layout.nav.wanted', route: '/wanted', icon: 'bookmark_search' }
  ];
  readonly mobileNavItems: NavItem[] = [
    { labelKey: 'layout.nav.home', route: '/home', icon: 'home' },
    { labelKey: 'layout.nav.catalog', route: '/catalog', icon: 'travel_explore' },
    { labelKey: 'layout.nav.collectionsShort', route: '/collections', icon: 'collections_bookmark' },
    { labelKey: 'layout.nav.wanted', route: '/wanted', icon: 'bookmark_search' }
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
