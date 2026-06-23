import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth/auth.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

interface HomeFeature {
  icon: string;
  titleKey: string;
  descriptionKey: string;
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatIconModule, TranslatePipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  readonly authService = inject(AuthService);
  readonly features: HomeFeature[] = [
    {
      icon: 'travel_explore',
      titleKey: 'home.features.catalogTitle',
      descriptionKey: 'home.features.catalogDescription'
    },
    {
      icon: 'collections_bookmark',
      titleKey: 'home.features.collectionsTitle',
      descriptionKey: 'home.features.collectionsDescription'
    },
    {
      icon: 'checklist',
      titleKey: 'home.features.statusTitle',
      descriptionKey: 'home.features.statusDescription'
    },
    {
      icon: 'bookmark_search',
      titleKey: 'home.features.wantedTitle',
      descriptionKey: 'home.features.wantedDescription'
    }
  ];
}
