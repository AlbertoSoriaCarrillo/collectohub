package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.MasterProduct;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record MasterProductResponse(
        Long id,
        String name,
        String description,
        ProductCategoryResponse category,
        String franchise,
        String collectionName,
        String volumeNumber,
        String publisher,
        String isbn,
        String ean,
        LocalDate releaseDate,
        LocalDate editionStartDate,
        LocalDate editionEndDate,
        String language,
        boolean limitedEdition,
        Integer limitedEditionTotalUnits,
        List<String> publicationCountries,
        String coverImageUrl,
        String status,
        Map<String, Object> attributes
) {

    private static final String LIMITED_EDITION_TOTAL_UNITS_ATTRIBUTE = "limitedEditionTotalUnits";

    public static MasterProductResponse from(MasterProduct product) {
        Map<String, Object> attributes = product.getAttributes();
        return new MasterProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                ProductCategoryResponse.from(product.getCategory()),
                product.getFranchise(),
                product.getCollectionName(),
                product.getVolumeNumber(),
                product.getPublisher(),
                product.getIsbn(),
                product.getEan(),
                product.getReleaseDate(),
                product.getEditionStartDate(),
                product.getEditionEndDate(),
                product.getProductLanguage(),
                product.isLimitedEdition(),
                limitedEditionTotalUnits(attributes),
                product.getPublicationCountries(),
                product.getCoverImageUrl(),
                product.getStatus().name(),
                attributes
        );
    }

    private static Integer limitedEditionTotalUnits(Map<String, Object> attributes) {
        Object value = attributes.get(LIMITED_EDITION_TOTAL_UNITS_ATTRIBUTE);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
