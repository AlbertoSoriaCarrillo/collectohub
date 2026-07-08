package com.collectohub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
class LiquibaseMigrationIntegrationTest {

    private static final List<String> MVP_TABLES = List.of(
            "users",
            "roles",
            "user_roles",
            "shops",
            "shop_members",
            "product_categories",
            "master_products",
            "product_suggestions",
            "shop_products",
            "collections",
            "collection_items",
            "reservations",
            "refresh_tokens",
            "publishers",
            "catalog_franchises",
            "catalog_series",
            "catalog_items",
            "catalog_item_editions",
            "master_product_catalog_links",
            "creators",
            "catalog_item_creators"
    );

    private static final List<String> REQUIRED_INDEXES = List.of(
            "uk_users_email",
            "uk_roles_code",
            "uk_product_categories_code",
            "idx_shops_owner_user_id",
            "idx_shop_products_shop_id",
            "idx_shop_products_master_product_id",
            "idx_master_products_isbn",
            "idx_master_products_ean",
            "idx_master_products_name",
            "idx_master_products_franchise",
            "idx_collections_user_id",
            "idx_collection_items_collection_id",
            "idx_collection_items_master_product_id",
            "idx_reservations_user_id",
            "idx_reservations_shop_id",
            "idx_reservations_status",
            "uk_refresh_tokens_token_hash",
            "idx_refresh_tokens_user_id",
            "idx_refresh_tokens_expires_at",
            "idx_publishers_name",
            "idx_publishers_record_status",
            "idx_catalog_franchises_name",
            "idx_catalog_franchises_slug",
            "uk_catalog_franchises_slug_active",
            "idx_catalog_franchises_record_status",
            "idx_catalog_series_franchise_id",
            "idx_catalog_series_primary_publisher_id",
            "idx_catalog_series_type",
            "idx_catalog_series_publication_status",
            "idx_catalog_series_record_status",
            "idx_catalog_series_title",
            "idx_catalog_items_series_id",
            "idx_catalog_items_record_status",
            "idx_catalog_items_title",
            "idx_catalog_items_sort_order",
            "idx_catalog_items_first_publication_year",
            "idx_catalog_items_original_language",
            "idx_catalog_items_origin_country",
            "idx_catalog_item_editions_catalog_item_id",
            "idx_catalog_item_editions_publisher_id",
            "idx_catalog_item_editions_record_status",
            "uk_catalog_item_editions_isbn_active",
            "uk_catalog_item_editions_ean_active",
            "idx_catalog_item_editions_format",
            "idx_catalog_item_editions_language",
            "idx_catalog_item_editions_country",
            "idx_catalog_item_editions_publication_year",
            "idx_master_product_catalog_links_master_product_id",
            "idx_master_product_catalog_links_catalog_item_id",
            "idx_master_product_catalog_links_catalog_item_edition_id",
            "idx_master_product_catalog_links_status",
            "idx_master_product_catalog_links_source",
            "idx_master_product_catalog_links_confidence",
            "uk_master_product_catalog_links_verified_master",
            "idx_collection_items_catalog_item_id",
            "idx_collection_items_catalog_item_edition_id",
            "idx_collection_items_editorial_reference_source",
            "idx_shop_products_catalog_item_id",
            "idx_shop_products_catalog_item_edition_id",
            "idx_shop_products_editorial_reference_source",
            "uk_creators_slug_active",
            "idx_creators_slug",
            "idx_creators_record_status",
            "idx_creators_name_lower",
            "uk_catalog_item_creators_active",
            "idx_catalog_item_creators_item_id",
            "idx_catalog_item_creators_creator_id",
            "idx_catalog_item_creators_role",
            "idx_catalog_item_creators_order"
    );

    private static final List<String> REQUIRED_CONSTRAINTS = List.of(
            "uk_users_email",
            "uk_roles_code",
            "uk_product_categories_code",
            "pk_user_roles",
            "fk_user_roles_user",
            "fk_user_roles_role",
            "fk_shops_owner_user",
            "fk_shop_products_shop",
            "fk_shop_products_master_product",
            "fk_reservations_user",
            "fk_reservations_shop",
            "fk_reservations_shop_product",
            "uk_refresh_tokens_token_hash",
            "fk_refresh_tokens_user",
            "fk_catalog_series_franchise",
            "fk_catalog_series_primary_publisher",
            "chk_publishers_record_status",
            "chk_catalog_franchises_record_status",
            "chk_catalog_series_type",
            "chk_catalog_series_publication_status",
            "chk_catalog_series_record_status",
            "chk_catalog_series_start_year",
            "chk_catalog_series_end_year",
            "chk_catalog_series_year_range",
            "fk_catalog_items_series",
            "chk_catalog_items_sort_order",
            "chk_catalog_items_first_publication_year",
            "chk_catalog_items_record_status",
            "fk_catalog_item_editions_item",
            "fk_catalog_item_editions_publisher",
            "chk_catalog_item_editions_format",
            "chk_catalog_item_editions_page_count",
            "chk_catalog_item_editions_publication_year",
            "chk_catalog_item_editions_record_status",
            "fk_master_product_catalog_links_master_product",
            "fk_master_product_catalog_links_catalog_item",
            "fk_master_product_catalog_links_catalog_item_edition",
            "chk_master_product_catalog_links_status",
            "chk_master_product_catalog_links_source",
            "chk_master_product_catalog_links_confidence",
            "fk_collection_items_catalog_item",
            "fk_collection_items_catalog_item_edition",
            "chk_collection_items_reference",
            "chk_collection_items_edition_requires_item",
            "chk_collection_items_editorial_reference_source",
            "fk_shop_products_catalog_item",
            "fk_shop_products_catalog_item_edition",
            "chk_shop_products_reference",
            "chk_shop_products_edition_requires_item",
            "chk_shop_products_editorial_reference_source",
            "fk_catalog_item_creators_item",
            "fk_catalog_item_creators_creator",
            "chk_creators_record_status",
            "chk_creators_birth_year",
            "chk_creators_death_year",
            "chk_creators_year_range",
            "chk_creators_country",
            "chk_catalog_item_creators_role",
            "chk_catalog_item_creators_order"
    );

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName("collectohub_test")
            .withUsername("collectohub")
            .withPassword("collectohub");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerPostgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @Test
    void liquibaseAppliesInitialMvpSchema() {
        assertThat(tableNames()).containsAll(MVP_TABLES);
        assertThat(indexNames()).containsAll(REQUIRED_INDEXES);
        assertThat(constraintNames()).containsAll(REQUIRED_CONSTRAINTS);
        assertThat(roleCodes()).containsExactlyInAnyOrder("ADMIN", "USER", "SHOP_OWNER", "CONTENT_CREATOR");
        assertThat(categoryCodes()).containsExactlyInAnyOrder(
                "MANGA_COMIC",
                "TRADING_CARD",
                "FIGURE",
                "VIDEOGAME",
                "MERCHANDISING",
                "MOVIE_SERIES"
        );
    }

    private List<String> tableNames() {
        return jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class);
    }

    private List<String> indexNames() {
        return jdbcTemplate.queryForList("""
                SELECT indexname
                FROM pg_indexes
                WHERE schemaname = 'public'
                """, String.class);
    }

    private List<String> constraintNames() {
        return jdbcTemplate.queryForList("""
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                """, String.class);
    }

    private List<String> roleCodes() {
        return jdbcTemplate.queryForList("SELECT code FROM roles", String.class);
    }

    private List<String> categoryCodes() {
        return jdbcTemplate.queryForList("SELECT code FROM product_categories", String.class);
    }
}
