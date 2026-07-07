import type { TranslationDictionary } from './translations';
import { TRANSLATIONS } from './translations';

describe('translation dictionaries', () => {
  it('keeps Spanish and English leaf keys aligned', () => {
    expect(leafKeys(TRANSLATIONS.es)).toEqual(leafKeys(TRANSLATIONS.en));
  });

  it('includes the public editorial catalog vocabulary in both languages', () => {
    const keys = leafKeys(TRANSLATIONS.es);
    expect(keys).toContain('editorial.searchTitle');
    expect(keys).toContain('editorial.viewSeries');
    expect(keys).toContain('editorial.viewItem');
    expect(keys).toContain('editorial.viewEdition');
    expect(keys).toContain('enums.editorialResultType.EDITION');
    expect(keys).toContain('collections.editorialReference');
    expect(keys).toContain('collections.verifiedBridge');
    expect(keys).toContain('collections.seriesNotAllowed');
    expect(keys).toContain('collections.searchLegacyProduct');
    expect(keys).toContain('collections.selectLegacyProduct');
    expect(keys).toContain('collections.legacyReferenceRequired');
    expect(keys).toContain('collections.editorialReferenceRequired');
    expect(keys).toContain('inventory.editorialReference');
    expect(keys).toContain('inventory.seriesNotAllowed');
    expect(keys).toContain('inventory.referenceSource.VERIFIED_BRIDGE');
    expect(leafKeys(TRANSLATIONS.en)).toEqual(keys);
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
