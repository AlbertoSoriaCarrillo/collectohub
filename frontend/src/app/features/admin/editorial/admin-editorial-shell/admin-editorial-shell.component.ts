import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

interface AdminSection {
  labelKey: string;
  icon: string;
}

@Component({
  selector: 'app-admin-editorial-shell',
  imports: [MatCardModule, MatIconModule, TranslatePipe],
  templateUrl: './admin-editorial-shell.component.html',
  styleUrl: './admin-editorial-shell.component.scss'
})
export class AdminEditorialShellComponent {
  readonly sections: AdminSection[] = [
    { labelKey: 'admin.editorial.sections.publishers', icon: 'business' },
    { labelKey: 'admin.editorial.sections.franchises', icon: 'hub' },
    { labelKey: 'admin.editorial.sections.series', icon: 'view_list' },
    { labelKey: 'admin.editorial.sections.items', icon: 'menu_book' },
    { labelKey: 'admin.editorial.sections.editions', icon: 'auto_stories' },
    { labelKey: 'admin.editorial.sections.creators', icon: 'groups' },
    { labelKey: 'admin.editorial.sections.credits', icon: 'badge' },
    { labelKey: 'admin.editorial.sections.relationships', icon: 'account_tree' },
    { labelKey: 'admin.editorial.sections.reconciliation', icon: 'sync_alt' }
  ];
}
