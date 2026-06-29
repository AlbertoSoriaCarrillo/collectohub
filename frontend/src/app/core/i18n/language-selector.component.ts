import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { LanguageService } from './language.service';
import type { SupportedLanguage } from './language.service';
import { TranslatePipe } from './translate.pipe';

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatMenuModule, TranslatePipe],
  template: `
    <div class="language-selector" [attr.aria-label]="'language.selectorLabel' | translate">
      <button
        mat-stroked-button
        type="button"
        class="language-trigger"
        [matMenuTriggerFor]="languageMenu"
        data-testid="language-selector"
        [attr.aria-label]="'language.selectorLabel' | translate"
      >
        <mat-icon aria-hidden="true">language</mat-icon>
        <span>{{ activeLanguageLabel() }}</span>
      </button>

      <mat-menu #languageMenu="matMenu">
      @for (language of languageService.availableLanguages(); track language) {
        <button
          mat-menu-item
          type="button"
          [class.active-language]="languageService.currentLanguage() === language"
          [attr.data-testid]="'language-' + language"
          (click)="setLanguage(language)"
        >
          <span>{{ language === 'es' ? ('language.es' | translate) : ('language.en' | translate) }}</span>
        </button>
      }
      </mat-menu>
    </div>
  `,
  styles: [
    `
      .language-selector {
        align-items: center;
        display: inline-flex;
      }

      .language-trigger {
        border: 1px solid var(--ch-border);
        min-height: 40px;
        padding-inline: 10px;
      }

      .language-trigger mat-icon {
        font-size: 18px;
        height: 18px;
        margin-right: 6px;
        width: 18px;
      }

      [mat-menu-item].active-language {
        color: var(--ch-primary);
      }
    `
  ]
})
export class LanguageSelectorComponent {
  readonly languageService = inject(LanguageService);

  activeLanguageLabel(): string {
    return this.languageService.currentLanguage() === 'es'
      ? this.languageService.translate('language.shortEs')
      : this.languageService.translate('language.shortEn');
  }

  setLanguage(language: SupportedLanguage): void {
    this.languageService.setLanguage(language);
  }
}
