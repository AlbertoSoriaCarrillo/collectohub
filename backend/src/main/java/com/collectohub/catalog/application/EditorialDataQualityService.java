package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.Creator;
import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.CreatorRepository;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EditorialDataQualityService {
    private final CreatorRepository creators;
    private final MasterProductCatalogLinkRepository links;
    public EditorialDataQualityService(CreatorRepository creators, MasterProductCatalogLinkRepository links) { this.creators = creators; this.links = links; }

    @Transactional(readOnly = true)
    public EditorialDataQualityReportResponse report(AuthenticatedUser user, String scope, int limit) {
        EditorialCatalogSupport.ensureAdmin(user);
        String normalized = scope == null || scope.isBlank() ? "ALL" : scope.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("ALL", "CREATORS", "MASTER_LINKS", "PUBLISHERS", "FRANCHISES", "SERIES", "ITEMS", "EDITIONS").contains(normalized))
            throw new InvalidEditorialCatalogRequestException("Unsupported data-quality scope");
        int capped = Math.min(Math.max(limit, 1), 200);
        List<EditorialDataQualityCheckResponse> checks = new ArrayList<>();
        if (normalized.equals("ALL") || normalized.equals("CREATORS")) checks.add(creatorNames(capped));
        if (normalized.equals("ALL") || normalized.equals("MASTER_LINKS")) checks.add(verifiedLinks(capped));
        int findings = checks.stream().mapToInt(EditorialDataQualityCheckResponse::totalFindings).sum();
        return new EditorialDataQualityReportResponse(Instant.now(), normalized, checks.size(), findings, checks);
    }
    private EditorialDataQualityCheckResponse creatorNames(int limit) {
        var grouped = creators.findAll().stream().filter(c -> c.getDeletedAt() == null)
                .collect(Collectors.groupingBy(c -> c.getName().trim().toLowerCase(Locale.ROOT)));
        return check("CREATOR_NAME", "CREATOR", "HIGH", "Duplicate creator names", grouped, limit,
                Creator::getId, Creator::getName, "Review and merge only after manual verification.");
    }
    private EditorialDataQualityCheckResponse verifiedLinks(int limit) {
        var grouped = links.findAll().stream().filter(l -> l.getDeletedAt() == null && "VERIFIED".equals(l.getLinkStatus().name()))
                .collect(Collectors.groupingBy(l -> String.valueOf(l.getMasterProduct().getId())));
        return check("MASTER_LINK_VERIFIED", "MASTER_LINK", "HIGH", "Multiple verified master links", grouped, limit,
                MasterProductCatalogLink::getId, l -> l.getMasterProduct().getName(), "Keep one verified link after manual review.");
    }
    private <T> EditorialDataQualityCheckResponse check(String key, String entity, String severity, String title,
            Map<String,List<T>> groups, int limit, Function<T,Long> id, Function<T,String> label, String recommendation) {
        List<EditorialDataQualityFindingResponse> findings = groups.entrySet().stream().filter(e -> e.getValue().size() > 1).limit(limit)
                .map(e -> new EditorialDataQualityFindingResponse(e.getKey(), e.getKey(), e.getValue().stream().map(id).toList(), e.getValue().stream().map(label).toList(), recommendation)).toList();
        return new EditorialDataQualityCheckResponse(key, entity, severity, title, "Exact normalized duplicate groups.", findings.size(), findings);
    }
}
