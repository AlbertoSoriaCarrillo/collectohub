package com.collectohub;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

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
                        "006-create-editorial-catalog-items-and-editions"
                );
    }
}
