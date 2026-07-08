package com.collectohub;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseChangelogParsingTest {

    private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";

    @Test
    void changelogCanBeParsed() throws Exception {
        var resourceAccessor = new ClassLoaderResourceAccessor();
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance()
                .getParser(CHANGELOG, resourceAccessor)
                .parse(CHANGELOG, new ChangeLogParameters(), resourceAccessor);

        assertThat(changeLog.getChangeSets())
                .extracting(ChangeSet::getId)
                .contains(
                        "000-initial-backend-baseline",
                        "001-create-mvp-schema",
                        "002-seed-reference-data",
                        "003-create-refresh-tokens",
                        "004-alter-shops-country-nullable",
                        "005-create-editorial-catalog-foundations",
                        "006-create-editorial-catalog-items-and-editions",
                        "007-create-master-product-catalog-links",
                        "008-add-editorial-references-to-collection-items",
                        "009-add-editorial-references-to-shop-products",
                        "010-create-catalog-creators",
                        "011-create-catalog-item-relationships"
                );
    }

    @Test
    void collectionEditorialBackfillUsesOnlyVerifiedActiveLinks() throws Exception {
        String migration;
        try (var input = getClass().getClassLoader().getResourceAsStream(
                "db/changelog/changes/008-add-editorial-references-to-collection-items.sql")) {
            assertThat(input).isNotNull();
            migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(migration)
                .contains("verified_link.link_status = 'VERIFIED'")
                .contains("verified_link.deleted_at IS NULL")
                .contains("collection_item.catalog_item_id IS NULL")
                .doesNotContain("link_status = 'PROPOSED'")
                .doesNotContain("link_status = 'REJECTED'");
    }

    @Test
    void shopProductEditorialBackfillUsesOnlyVerifiedActiveLinks() throws Exception {
        String migration;
        try (var input = getClass().getClassLoader().getResourceAsStream(
                "db/changelog/changes/009-add-editorial-references-to-shop-products.sql")) {
            assertThat(input).isNotNull();
            migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(migration)
                .contains("verified_link.link_status = 'VERIFIED'")
                .contains("verified_link.deleted_at IS NULL")
                .contains("shop_product.catalog_item_id IS NULL")
                .doesNotContain("link_status = 'PROPOSED'")
                .doesNotContain("link_status = 'REJECTED'");
    }
}
