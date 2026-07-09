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
    expect(keys).toContain('editorial.creators.title');
    expect(keys).toContain('editorial.creators.roles.AUTHOR');
    expect(keys).toContain('editorial.creators.roles.TRANSLATOR');
    expect(keys).toContain('editorial.relationships.title');
    expect(keys).toContain('editorial.relationships.direction.OUTGOING');
    expect(keys).toContain('editorial.relationships.direction.INCOMING');
    expect(keys).toContain('editorial.relationships.types.ADAPTATION');
    expect(keys).toContain('editorial.relationships.types.SEQUEL');
    expect(keys).toContain('editorial.relationships.types.SAME_WORK');
    expect(keys).toContain('layout.nav.adminEditorial');
    expect(keys).toContain('admin.editorial.title');
    expect(keys).toContain('admin.editorial.subtitle');
    expect(keys).toContain('admin.editorial.adminOnly');
    expect(keys).toContain('admin.editorial.nextSectionsTitle');
    expect(keys).toContain('admin.editorial.noCrudYet');
    expect(keys).toContain('admin.editorial.publishers.title');
    expect(keys).toContain('admin.editorial.publishers.subtitle');
    expect(keys).toContain('admin.editorial.franchises.title');
    expect(keys).toContain('admin.editorial.franchises.subtitle');
    expect(keys).toContain('admin.editorial.series.title');
    expect(keys).toContain('admin.editorial.series.subtitle');
    expect(keys).toContain('admin.editorial.actions.search');
    expect(keys).toContain('admin.editorial.actions.create');
    expect(keys).toContain('admin.editorial.actions.edit');
    expect(keys).toContain('admin.editorial.actions.save');
    expect(keys).toContain('admin.editorial.actions.cancel');
    expect(keys).toContain('admin.editorial.actions.previous');
    expect(keys).toContain('admin.editorial.actions.next');
    expect(keys).toContain('admin.editorial.actions.refresh');
    expect(keys).toContain('admin.editorial.fields.name');
    expect(keys).toContain('admin.editorial.fields.country');
    expect(keys).toContain('admin.editorial.fields.slug');
    expect(keys).toContain('admin.editorial.fields.description');
    expect(keys).toContain('admin.editorial.fields.status');
    expect(keys).toContain('admin.editorial.fields.title');
    expect(keys).toContain('admin.editorial.fields.originalTitle');
    expect(keys).toContain('admin.editorial.fields.type');
    expect(keys).toContain('admin.editorial.fields.publicationStatus');
    expect(keys).toContain('admin.editorial.fields.franchiseId');
    expect(keys).toContain('admin.editorial.fields.primaryPublisherId');
    expect(keys).toContain('admin.editorial.fields.originCountry');
    expect(keys).toContain('admin.editorial.fields.originalLanguage');
    expect(keys).toContain('admin.editorial.fields.startYear');
    expect(keys).toContain('admin.editorial.fields.endYear');
    expect(keys).toContain('admin.editorial.status.DRAFT');
    expect(keys).toContain('admin.editorial.status.ACTIVE');
    expect(keys).toContain('admin.editorial.status.ARCHIVED');
    expect(keys).toContain('admin.editorial.seriesType.BOOK');
    expect(keys).toContain('admin.editorial.seriesType.COMIC');
    expect(keys).toContain('admin.editorial.seriesType.MANGA');
    expect(keys).toContain('admin.editorial.publicationStatus.ONGOING');
    expect(keys).toContain('admin.editorial.publicationStatus.COMPLETED');
    expect(keys).toContain('admin.editorial.publicationStatus.CANCELLED');
    expect(keys).toContain('admin.editorial.publicationStatus.HIATUS');
    expect(keys).toContain('admin.editorial.publicationStatus.UNKNOWN');
    expect(keys).toContain('admin.editorial.messages.saved');
    expect(keys).toContain('admin.editorial.messages.loadError');
    expect(keys).toContain('admin.editorial.messages.saveError');
    expect(keys).toContain('admin.editorial.messages.empty');
    expect(keys).toContain('admin.editorial.sections.publishers');
    expect(keys).toContain('admin.editorial.sections.franchises');
    expect(keys).toContain('admin.editorial.sections.series');
    expect(keys).toContain('admin.editorial.sections.items');
    expect(keys).toContain('admin.editorial.sections.editions');
    expect(keys).toContain('admin.editorial.sections.creators');
    expect(keys).toContain('admin.editorial.sections.credits');
    expect(keys).toContain('admin.editorial.sections.relationships');
    expect(keys).toContain('admin.editorial.sections.reconciliation');
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
