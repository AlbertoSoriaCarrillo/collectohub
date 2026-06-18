import { en } from './translations/en';
import { es } from './translations/es';

export type SupportedLanguage = 'es' | 'en';

export type TranslationValue = string | TranslationDictionary;

export interface TranslationDictionary {
  [key: string]: TranslationValue;
}

export const DEFAULT_LANGUAGE: SupportedLanguage = 'es';
export const SUPPORTED_LANGUAGES: SupportedLanguage[] = ['es', 'en'];

export const TRANSLATIONS: Record<SupportedLanguage, TranslationDictionary> = {
  es,
  en
};
