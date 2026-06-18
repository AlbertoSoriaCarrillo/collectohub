import { TestBed } from '@angular/core/testing';
import { LanguageService } from './language.service';
import { TranslatePipe } from './translate.pipe';

describe('TranslatePipe', () => {
  afterEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('translates keys with params', () => {
    localStorage.setItem('collectohub.language', 'es');

    const pipe = TestBed.runInInjectionContext(() => new TranslatePipe());

    expect(pipe.transform('dashboard.greeting', { name: 'Ada' })).toBe('Hola, Ada');
  });

  it('updates when the active language changes', () => {
    localStorage.setItem('collectohub.language', 'es');
    const service = TestBed.inject(LanguageService);
    const pipe = TestBed.runInInjectionContext(() => new TranslatePipe());

    expect(pipe.transform('actions.search')).toBe('Buscar');

    service.setLanguage('en');

    expect(pipe.transform('actions.search')).toBe('Search');
  });

  it('returns the key when the translation is missing', () => {
    localStorage.setItem('collectohub.language', 'es');

    const pipe = TestBed.runInInjectionContext(() => new TranslatePipe());

    expect(pipe.transform('not.available')).toBe('not.available');
  });
});
