import { isPlatformBrowser } from '@angular/common';
import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { DEFAULT_LANGUAGE, SUPPORTED_LANGUAGES, TRANSLATIONS } from './translations';
import type { SupportedLanguage, TranslationDictionary } from './translations';

type TranslationParams = Record<string, string | number>;

const LANGUAGE_STORAGE_KEY = 'collectohub.language';

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly languageState = signal<SupportedLanguage>(this.resolveInitialLanguage());

  readonly currentLanguage = computed(() => this.languageState());

  setLanguage(language: SupportedLanguage): void {
    const normalizedLanguage = this.isSupportedLanguage(language) ? language : DEFAULT_LANGUAGE;
    this.languageState.set(normalizedLanguage);
    this.storage()?.setItem(LANGUAGE_STORAGE_KEY, normalizedLanguage);
  }

  translate(key: string, params?: TranslationParams): string {
    const language = this.languageState();
    const translatedValue =
      this.lookup(TRANSLATIONS[language], key) ?? this.lookup(TRANSLATIONS[DEFAULT_LANGUAGE], key);
    const normalizedValue = typeof translatedValue === 'string' ? translatedValue : key;
    return this.interpolate(normalizedValue, params);
  }

  availableLanguages(): SupportedLanguage[] {
    return [...SUPPORTED_LANGUAGES];
  }

  private resolveInitialLanguage(): SupportedLanguage {
    const storedLanguage = this.storage()?.getItem(LANGUAGE_STORAGE_KEY);
    if (storedLanguage !== null && storedLanguage !== undefined) {
      return this.isSupportedLanguage(storedLanguage) ? storedLanguage : DEFAULT_LANGUAGE;
    }

    const browserLanguage = this.browserLanguage();
    return browserLanguage === 'en' ? 'en' : DEFAULT_LANGUAGE;
  }

  private browserLanguage(): SupportedLanguage | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const language = globalThis.navigator?.language?.toLowerCase();
    if (!language) {
      return null;
    }

    return language.startsWith('en') ? 'en' : DEFAULT_LANGUAGE;
  }

  private isSupportedLanguage(language: unknown): language is SupportedLanguage {
    return typeof language === 'string' && SUPPORTED_LANGUAGES.includes(language as SupportedLanguage);
  }

  private lookup(dictionary: TranslationDictionary, key: string): string | null {
    const value = key.split('.').reduce<unknown>((currentValue, segment) => {
      if (!currentValue || typeof currentValue !== 'object') {
        return null;
      }

      return (currentValue as Record<string, unknown>)[segment] ?? null;
    }, dictionary);

    return typeof value === 'string' ? value : null;
  }

  private interpolate(value: string, params?: TranslationParams): string {
    if (!params) {
      return value;
    }

    return Object.entries(params).reduce(
      (currentValue, [key, paramValue]) =>
        currentValue.replaceAll(`{{${key}}}`, String(paramValue)),
      value
    );
  }

  private storage(): Storage | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    try {
      return globalThis.localStorage;
    } catch {
      return null;
    }
  }
}

export type { SupportedLanguage };
