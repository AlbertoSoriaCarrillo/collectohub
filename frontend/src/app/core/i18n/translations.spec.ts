import type { TranslationDictionary } from './translations';
import { TRANSLATIONS } from './translations';

describe('translation dictionaries', () => {
  it('keeps Spanish and English leaf keys aligned', () => {
    expect(leafKeys(TRANSLATIONS.es)).toEqual(leafKeys(TRANSLATIONS.en));
  });
});

function leafKeys(dictionary: TranslationDictionary, prefix = ''): string[] {
  return Object.entries(dictionary)
    .flatMap(([key, value]) => {
      const path = prefix ? `${prefix}.${key}` : key;
      return typeof value === 'string' ? [path] : leafKeys(value, path);
    })
    .sort();
}
