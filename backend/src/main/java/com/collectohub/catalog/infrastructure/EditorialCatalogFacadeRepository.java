package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.EditorialCatalogResultType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class EditorialCatalogFacadeRepository {

    private static final String SERIES_SELECT = """
            SELECT 'SERIES' AS result_type,
                   s.id AS series_id, s.title AS series_title,
                   CAST(NULL AS BIGINT) AS item_id, CAST(NULL AS VARCHAR) AS item_title,
                   CAST(NULL AS BIGINT) AS edition_id, CAST(NULL AS VARCHAR) AS edition_name,
                   sp.name AS publisher_name, f.name AS franchise_name, s.type AS series_type,
                   s.original_language AS language, s.origin_country AS country,
                   s.start_year AS publication_year, CAST(NULL AS VARCHAR) AS cover_image_url,
                   CAST(NULL AS BIGINT) AS linked_master_product_id,
                   CAST(NULL AS VARCHAR) AS linked_master_product_name,
                   s.title AS sort_title, s.franchise_id AS filter_franchise_id,
                   s.primary_publisher_id AS filter_publisher_id,
                   CONCAT_WS(' ', s.title, s.original_title) AS search_text
              FROM catalog_series s
              LEFT JOIN catalog_franchises f ON f.id = s.franchise_id
              LEFT JOIN publishers sp ON sp.id = s.primary_publisher_id
             WHERE s.record_status = 'ACTIVE' AND s.deleted_at IS NULL
               AND (f.id IS NULL OR (f.record_status = 'ACTIVE' AND f.deleted_at IS NULL))
               AND (sp.id IS NULL OR (sp.record_status = 'ACTIVE' AND sp.deleted_at IS NULL))
            """;

    private static final String ITEM_SELECT = """
            SELECT 'ITEM' AS result_type,
                   s.id AS series_id, s.title AS series_title,
                   i.id AS item_id, i.title AS item_title,
                   CAST(NULL AS BIGINT) AS edition_id, CAST(NULL AS VARCHAR) AS edition_name,
                   sp.name AS publisher_name, f.name AS franchise_name, s.type AS series_type,
                   i.original_language AS language, i.origin_country AS country,
                   i.first_publication_year AS publication_year,
                   CAST(NULL AS VARCHAR) AS cover_image_url,
                   CAST(NULL AS BIGINT) AS linked_master_product_id,
                   CAST(NULL AS VARCHAR) AS linked_master_product_name,
                   i.title AS sort_title, s.franchise_id AS filter_franchise_id,
                   s.primary_publisher_id AS filter_publisher_id,
                   CONCAT_WS(' ', i.title, i.original_title) AS search_text
              FROM catalog_items i
              JOIN catalog_series s ON s.id = i.series_id
              LEFT JOIN catalog_franchises f ON f.id = s.franchise_id
              LEFT JOIN publishers sp ON sp.id = s.primary_publisher_id
             WHERE i.record_status = 'ACTIVE' AND i.deleted_at IS NULL
               AND s.record_status = 'ACTIVE' AND s.deleted_at IS NULL
               AND (f.id IS NULL OR (f.record_status = 'ACTIVE' AND f.deleted_at IS NULL))
               AND (sp.id IS NULL OR (sp.record_status = 'ACTIVE' AND sp.deleted_at IS NULL))
            """;

    private static final String EDITION_SELECT = """
            SELECT 'EDITION' AS result_type,
                   s.id AS series_id, s.title AS series_title,
                   i.id AS item_id, i.title AS item_title,
                   e.id AS edition_id, e.edition_name AS edition_name,
                   p.name AS publisher_name, f.name AS franchise_name, s.type AS series_type,
                   e.language AS language, e.country AS country,
                   e.publication_year AS publication_year, e.cover_image_url AS cover_image_url,
                   CAST(NULL AS BIGINT) AS linked_master_product_id,
                   CAST(NULL AS VARCHAR) AS linked_master_product_name,
                   COALESCE(e.edition_name, i.title) AS sort_title,
                   s.franchise_id AS filter_franchise_id,
                   e.publisher_id AS filter_publisher_id,
                   CONCAT_WS(' ', e.isbn, e.ean, e.edition_name, i.title, i.original_title) AS search_text
              FROM catalog_item_editions e
              JOIN catalog_items i ON i.id = e.catalog_item_id
              JOIN catalog_series s ON s.id = i.series_id
              LEFT JOIN catalog_franchises f ON f.id = s.franchise_id
              LEFT JOIN publishers p ON p.id = e.publisher_id
              LEFT JOIN publishers sp ON sp.id = s.primary_publisher_id
             WHERE e.record_status = 'ACTIVE' AND e.deleted_at IS NULL
               AND i.record_status = 'ACTIVE' AND i.deleted_at IS NULL
               AND s.record_status = 'ACTIVE' AND s.deleted_at IS NULL
               AND (f.id IS NULL OR (f.record_status = 'ACTIVE' AND f.deleted_at IS NULL))
               AND (p.id IS NULL OR (p.record_status = 'ACTIVE' AND p.deleted_at IS NULL))
               AND (sp.id IS NULL OR (sp.record_status = 'ACTIVE' AND sp.deleted_at IS NULL))
            """;

    private static final String LINK_SELECT = """
            SELECT 'MASTER_PRODUCT_LINK' AS result_type,
                   s.id AS series_id, s.title AS series_title,
                   i.id AS item_id, i.title AS item_title,
                   e.id AS edition_id, e.edition_name AS edition_name,
                   COALESCE(p.name, sp.name) AS publisher_name,
                   f.name AS franchise_name, s.type AS series_type,
                   COALESCE(e.language, i.original_language) AS language,
                   COALESCE(e.country, i.origin_country) AS country,
                   COALESCE(e.publication_year, i.first_publication_year) AS publication_year,
                   e.cover_image_url AS cover_image_url,
                   m.id AS linked_master_product_id, m.name AS linked_master_product_name,
                   m.name AS sort_title, s.franchise_id AS filter_franchise_id,
                   COALESCE(e.publisher_id, s.primary_publisher_id) AS filter_publisher_id,
                   CONCAT_WS(' ', m.name, i.title, i.original_title, e.edition_name, e.isbn, e.ean) AS search_text
              FROM master_product_catalog_links l
              JOIN master_products m ON m.id = l.master_product_id
              JOIN catalog_items i ON i.id = l.catalog_item_id
              JOIN catalog_series s ON s.id = i.series_id
              LEFT JOIN catalog_item_editions e ON e.id = l.catalog_item_edition_id
              LEFT JOIN catalog_franchises f ON f.id = s.franchise_id
              LEFT JOIN publishers p ON p.id = e.publisher_id
              LEFT JOIN publishers sp ON sp.id = s.primary_publisher_id
             WHERE l.link_status IN ('VERIFIED', 'PROPOSED') AND l.deleted_at IS NULL
               AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
               AND i.record_status = 'ACTIVE' AND i.deleted_at IS NULL
               AND s.record_status = 'ACTIVE' AND s.deleted_at IS NULL
               AND (e.id IS NULL OR (e.record_status = 'ACTIVE' AND e.deleted_at IS NULL))
               AND (f.id IS NULL OR (f.record_status = 'ACTIVE' AND f.deleted_at IS NULL))
               AND (p.id IS NULL OR (p.record_status = 'ACTIVE' AND p.deleted_at IS NULL))
               AND (sp.id IS NULL OR (sp.record_status = 'ACTIVE' AND sp.deleted_at IS NULL))
            """;

    private final EntityManager entityManager;

    public EditorialCatalogFacadeRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public SearchPage search(SearchCriteria criteria, Pageable pageable) {
        List<String> branches = branches(criteria.resultType());
        Map<String, Object> parameters = new LinkedHashMap<>();
        String where = filters(criteria, parameters);
        String cte = "WITH editorial_results AS (" + String.join(" UNION ALL ", branches) + ") ";
        String select = cte + "SELECT result_type, series_id, series_title, item_id, item_title, "
                + "edition_id, edition_name, publisher_name, franchise_name, series_type, language, country, "
                + "publication_year, cover_image_url, linked_master_product_id, linked_master_product_name "
                + "FROM editorial_results" + where + orderBy(pageable);
        String count = cte + "SELECT COUNT(*) FROM editorial_results" + where;

        Query dataQuery = entityManager.createNativeQuery(select);
        Query countQuery = entityManager.createNativeQuery(count);
        parameters.forEach((name, value) -> {
            dataQuery.setParameter(name, value);
            countQuery.setParameter(name, value);
        });
        dataQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        long total = ((Number) countQuery.getSingleResult()).longValue();
        return new SearchPage(rows.stream().map(this::map).toList(), total);
    }

    private List<String> branches(EditorialCatalogResultType resultType) {
        if (resultType == null) {
            return List.of(SERIES_SELECT, ITEM_SELECT, EDITION_SELECT);
        }
        return switch (resultType) {
            case SERIES -> List.of(SERIES_SELECT);
            case ITEM -> List.of(ITEM_SELECT);
            case EDITION -> List.of(EDITION_SELECT);
            case MASTER_PRODUCT_LINK -> List.of(LINK_SELECT);
        };
    }

    private String filters(SearchCriteria criteria, Map<String, Object> parameters) {
        List<String> filters = new ArrayList<>();
        if (criteria.query() != null) {
            filters.add("LOWER(search_text) LIKE :query");
            parameters.put("query", "%" + criteria.query().toLowerCase(Locale.ROOT) + "%");
        }
        add(filters, parameters, "series_type = :seriesType", "seriesType", criteria.seriesType());
        add(filters, parameters, "filter_franchise_id = :franchiseId", "franchiseId", criteria.franchiseId());
        add(filters, parameters, "series_id = :seriesId", "seriesId", criteria.seriesId());
        add(filters, parameters, "filter_publisher_id = :publisherId", "publisherId", criteria.publisherId());
        add(filters, parameters, "LOWER(language) = :language", "language", criteria.language());
        add(filters, parameters, "UPPER(country) = :country", "country", criteria.country());
        add(filters, parameters, "publication_year = :publicationYear", "publicationYear", criteria.publicationYear());
        return filters.isEmpty() ? "" : " WHERE " + String.join(" AND ", filters);
    }

    private void add(
            List<String> filters,
            Map<String, Object> parameters,
            String clause,
            String name,
            Object value
    ) {
        if (value != null) {
            filters.add(clause);
            parameters.put(name, value);
        }
    }

    private String orderBy(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return " ORDER BY sort_title ASC NULLS LAST, result_type ASC";
        }
        var order = pageable.getSort().iterator().next();
        String column = switch (order.getProperty()) {
            case "publicationYear" -> "publication_year";
            case "resultType" -> "result_type";
            default -> "sort_title";
        };
        return " ORDER BY " + column + " " + order.getDirection().name() + " NULLS LAST, result_type ASC";
    }

    private SearchRow map(Object[] row) {
        return new SearchRow(
                (String) row[0], number(row[1]), (String) row[2], number(row[3]), (String) row[4],
                number(row[5]), (String) row[6], (String) row[7], (String) row[8], (String) row[9],
                (String) row[10], (String) row[11], integer(row[12]), (String) row[13],
                number(row[14]), (String) row[15]
        );
    }

    private Long number(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer integer(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    public record SearchCriteria(
            String query,
            String seriesType,
            Long franchiseId,
            Long seriesId,
            Long publisherId,
            String language,
            String country,
            Integer publicationYear,
            EditorialCatalogResultType resultType
    ) {
    }

    public record SearchPage(List<SearchRow> content, long totalElements) {
    }

    public record SearchRow(
            String resultType,
            Long seriesId,
            String seriesTitle,
            Long itemId,
            String itemTitle,
            Long editionId,
            String editionName,
            String publisherName,
            String franchiseName,
            String type,
            String language,
            String country,
            Integer publicationYear,
            String coverImageUrl,
            Long linkedMasterProductId,
            String linkedMasterProductName
    ) {
    }
}
