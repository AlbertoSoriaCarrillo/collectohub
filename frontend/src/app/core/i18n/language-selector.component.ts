import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { LanguageService } from './language.service';
import type { SupportedLanguage } from './language.service';
import { TranslatePipe } from './translate.pipe';

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [MatButtonModule, TranslatePipe],
  template: `
    <div class="language-selector" role="group" [attr.aria-label]="'language.selectorLabel' | translate">
      @for (language of languageService.availableLanguages(); track language) {
        <button
          mat-button
          type="button"
          [class.active-language]="languageService.currentLanguage() === language"
          [attr.aria-pressed]="languageService.currentLanguage() === language"
          [attr.data-testid]="'language-' + language"
          (click)="setLanguage(language)"
        >
          {{ language === 'es' ? ('language.shortEs' | translate) : ('language.shortEn' | translate) }}
        </button>
      }
    </div>
  `,
  styles: [
    `
      .language-selector {
        align-items: center;
        display: inline-flex;
        gap: 4px;
      }

      .language-selector button {
        border: 1px solid var(--ch-border);
        min-width: 44px;
        padding-inline: 10px;
      }

      .language-selector button.active-language {
        background: rgba(103, 213, 200, 0.14);
        border-color: rgba(103, 213, 200, 0.42);
        color: var(--ch-primary);
      }
    `
  ]
})
export class LanguageSelectorComponent {
  readonly languageService = inject(LanguageService);

  setLanguage(language: SupportedLanguage): void {
    this.languageService.setLanguage(language);
  }
}
