import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { LanguageService } from './language.service';
import { LanguageSelectorComponent } from './language-selector.component';

describe('LanguageSelectorComponent', () => {
  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [LanguageSelectorComponent],
      providers: [provideAnimationsAsync('noop')]
    }).compileComponents();
  });

  afterEach(() => {
    document.body.querySelectorAll('.cdk-overlay-container').forEach((element) => element.remove());
    localStorage.clear();
  });

  it('switches language from the compact menu', async () => {
    const fixture = TestBed.createComponent(LanguageSelectorComponent);
    const languageService = TestBed.inject(LanguageService);
    fixture.detectChanges();

    const trigger = fixture.nativeElement.querySelector(
      '[data-testid="language-selector"]'
    ) as HTMLButtonElement;
    trigger.click();
    fixture.detectChanges();
    await fixture.whenStable();

    const englishOption = document.body.querySelector(
      '[data-testid="language-en"]'
    ) as HTMLButtonElement;
    englishOption.click();
    fixture.detectChanges();

    expect(languageService.currentLanguage()).toBe('en');
  });
});
