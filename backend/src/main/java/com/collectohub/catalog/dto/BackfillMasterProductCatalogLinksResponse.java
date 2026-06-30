package com.collectohub.catalog.dto;

public record BackfillMasterProductCatalogLinksResponse(
        int scanned,
        int proposed,
        int skipped,
        int ambiguous
) {
}
