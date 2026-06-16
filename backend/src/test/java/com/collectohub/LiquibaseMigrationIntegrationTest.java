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
            "reservations"
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
            "idx_reservations_status"
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
            "fk_reservations_shop_product"
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
