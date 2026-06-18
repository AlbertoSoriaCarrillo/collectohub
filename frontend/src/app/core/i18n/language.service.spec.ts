import { TestBed } from '@angular/core/testing';
import { LanguageService } from './language.service';

const LANGUAGE_STORAGE_KEY = 'collectohub.language';

describe('LanguageService', () => {
  const originalNavigatorLanguage = window.navigator.language;

  afterEach(() => {
    localStorage.clear();
    Object.defineProperty(window.navigator, 'language', {
      configurable: true,
      get: () => originalNavigatorLanguage
    });
    TestBed.resetTestingModule();
  });

  it('uses Spanish when localStorage is empty and the browser is not English', () => {
    setBrowserLanguage('es-ES');

    const service = TestBed.inject(LanguageService);

    expect(service.currentLanguage()).toBe('es');
    expect(service.translate('actions.search')).toBe('Buscar');
  });

  it('uses English from the browser when no saved language exists', () => {
    setBrowserLanguage('en-US');

    const service = TestBed.inject(LanguageService);

    expect(service.currentLanguage()).toBe('en');
    expect(service.translate('actions.search')).toBe('Search');
  });

  it('falls back to Spanish when localStorage contains an invalid language', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, 'fr');
    setBrowserLanguage('en-US');

    const service = TestBed.inject(LanguageService);

    expect(service.currentLanguage()).toBe('es');
  });

  it('persists language changes and switches translations immediately', () => {
    setBrowserLanguage('es-ES');
    const service = TestBed.inject(LanguageService);

    service.setLanguage('en');

    expect(service.currentLanguage()).toBe('en');
    expect(localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe('en');
    expect(service.translate('actions.search')).toBe('Search');

    service.setLanguage('es');

    expect(service.translate('actions.search')).toBe('Buscar');
  });

  it('falls back safely for missing keys and interpolates params', () => {
    setBrowserLanguage('es-ES');
    const service = TestBed.inject(LanguageService);

    expect(service.translate('dashboard.greeting', { name: 'Ada' })).toBe('Hola, Ada');
    expect(service.translate('missing.key')).toBe('missing.key');
  });

  it('exposes available languages', () => {
    const service = TestBed.inject(LanguageService);

    expect(service.availableLanguages()).toEqual(['es', 'en']);
  });
});

function setBrowserLanguage(language: string): void {
  Object.defineProperty(window.navigator, 'language', {
    configurable: true,
    get: () => language
  });
}
