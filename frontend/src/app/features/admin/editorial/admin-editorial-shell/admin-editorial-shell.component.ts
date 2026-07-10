import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

interface AdminSection {
  labelKey: string;
  icon: string;
  route?: string;
}

@Component({
  selector: 'app-admin-editorial-shell',
  imports: [RouterLink, MatCardModule, MatIconModule, TranslatePipe],
  templateUrl: './admin-editorial-shell.component.html',
  styleUrl: './admin-editorial-shell.component.scss'
})
export class AdminEditorialShellComponent {
  readonly sections: AdminSection[] = [
    {
      labelKey: 'admin.editorial.sections.publishers',
      icon: 'business',
      route: '/admin/editorial/publishers'
    },
    {
      labelKey: 'admin.editorial.sections.franchises',
      icon: 'hub',
      route: '/admin/editorial/franchises'
    },
    {
      labelKey: 'admin.editorial.sections.series',
      icon: 'view_list',
      route: '/admin/editorial/series'
    },
    {
      labelKey: 'admin.editorial.sections.items',
      icon: 'menu_book',
      route: '/admin/editorial/items'
    },
    {
      labelKey: 'admin.editorial.sections.editions',
      icon: 'auto_stories',
      route: '/admin/editorial/editions'
    },
    {
      labelKey: 'admin.editorial.sections.creators',
      icon: 'groups',
      route: '/admin/editorial/creators'
    },
    {
      labelKey: 'admin.editorial.sections.credits',
      icon: 'badge',
      route: '/admin/editorial/credits'
    },
    {
      labelKey: 'admin.editorial.sections.relationships',
      icon: 'account_tree',
      route: '/admin/editorial/relationships'
    },
    { labelKey: 'admin.editorial.sections.reconciliation', icon: 'sync_alt' }
  ];
}
