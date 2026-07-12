package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class EditorialDataQualityService {
    private final CreatorRepository creators;
    private final MasterProductCatalogLinkRepository links;
    private final PublisherRepository publishers;
    private final CatalogFranchiseRepository franchises;
    private final CatalogSeriesRepository series;
    private final CatalogItemRepository items;
    private final CatalogItemEditionRepository editions;
    public EditorialDataQualityService(CreatorRepository creators, MasterProductCatalogLinkRepository links, PublisherRepository publishers, CatalogFranchiseRepository franchises, CatalogSeriesRepository series, CatalogItemRepository items, CatalogItemEditionRepository editions) { this.creators = creators; this.links = links; this.publishers = publishers; this.franchises = franchises; this.series = series; this.items = items; this.editions = editions; }

    @Transactional(readOnly = true)
    public EditorialDataQualityReportResponse report(AuthenticatedUser user, String scope, int limit) {
        EditorialCatalogSupport.ensureAdmin(user);
        String normalized = scope == null || scope.isBlank() ? "ALL" : scope.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("ALL", "CREATORS", "MASTER_LINKS", "PUBLISHERS", "FRANCHISES", "SERIES", "ITEMS", "EDITIONS").contains(normalized))
            throw new InvalidEditorialCatalogRequestException("Unsupported data-quality scope");
        int capped = Math.min(Math.max(limit, 1), 200);
        List<EditorialDataQualityCheckResponse> checks = new ArrayList<>();
        if (normalized.equals("ALL") || normalized.equals("PUBLISHERS")) checks.add(group("PUBLISHER_NAME", "PUBLISHER", publishers.findDuplicateNameGroups(capped), capped));
        if (normalized.equals("ALL") || normalized.equals("FRANCHISES")) { checks.add(group("FRANCHISE_NAME", "FRANCHISE", franchises.findDuplicateNameGroups(capped), capped)); checks.add(group("FRANCHISE_SLUG", "FRANCHISE", franchises.findDuplicateSlugGroups(capped), capped)); }
        if (normalized.equals("ALL") || normalized.equals("SERIES")) checks.add(group("SERIES_TITLE_IN_FRANCHISE", "SERIES", series.findDuplicateTitleInFranchiseGroups(capped), capped));
        if (normalized.equals("ALL") || normalized.equals("ITEMS")) { checks.add(group("ITEM_TITLE_IN_SERIES", "ITEM", items.findDuplicateTitleInSeriesGroups(capped), capped)); checks.add(group("ITEM_SEQUENCE_IN_SERIES", "ITEM", items.findDuplicateSequenceInSeriesGroups(capped), capped)); }
        if (normalized.equals("ALL") || normalized.equals("EDITIONS")) { checks.add(group("EDITION_ISBN", "EDITION", editions.findDuplicateIsbnGroups(capped), capped)); checks.add(group("EDITION_EAN", "EDITION", editions.findDuplicateEanGroups(capped), capped)); checks.add(group("EDITION_NAME_IN_ITEM", "EDITION", editions.findDuplicateNameInItemGroups(capped), capped)); }
        if (normalized.equals("ALL") || normalized.equals("CREATORS")) { checks.add(group("CREATOR_NAME", "CREATOR", creators.findDuplicateNameGroups(capped), capped)); checks.add(group("CREATOR_SLUG", "CREATOR", creators.findDuplicateSlugGroups(capped), capped)); }
        if (normalized.equals("ALL") || normalized.equals("MASTER_LINKS")) { checks.add(group("MASTER_LINK_MULTIPLE_VERIFIED", "MASTER_LINK", links.findMultipleVerifiedGroups(capped), capped)); checks.add(group("MASTER_LINK_EXACT_DUPLICATE", "MASTER_LINK", links.findExactDuplicateGroups(capped), capped)); }
        int findings = checks.stream().mapToInt(EditorialDataQualityCheckResponse::totalFindings).sum();
        return new EditorialDataQualityReportResponse(Instant.now(), normalized, checks.size(), findings, checks);
    }
    private EditorialDataQualityCheckResponse group(String key, String entity, List<EditorialDataQualityGroup> groups, int limit) {
        List<EditorialDataQualityFindingResponse> findings = groups.stream().limit(limit).map(g -> new EditorialDataQualityFindingResponse(g.getGroupKey(), g.getDisplayValue(), Arrays.stream(g.getRecordIds().split(",")).map(Long::valueOf).toList(), Arrays.asList(g.getRecordLabels().split(" \\| ")), "Review records manually before any merge.")).toList();
        return new EditorialDataQualityCheckResponse(key, entity, "HIGH", key, "Exact normalized duplicate groups.", findings.size(), findings);
    }
}
