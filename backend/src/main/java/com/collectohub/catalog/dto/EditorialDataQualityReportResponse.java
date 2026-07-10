package com.collectohub.catalog.dto;

import java.time.Instant;
import java.util.List;

public record EditorialDataQualityReportResponse(Instant generatedAt, String scope, int totalChecks,
        int totalFindings, List<EditorialDataQualityCheckResponse> checks) {}
