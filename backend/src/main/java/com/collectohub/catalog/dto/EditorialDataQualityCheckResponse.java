package com.collectohub.catalog.dto;

import java.util.List;

public record EditorialDataQualityCheckResponse(String key, String entityType, String severity,
        String title, String description, int totalFindings, List<EditorialDataQualityFindingResponse> findings) {}
