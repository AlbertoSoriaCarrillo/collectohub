package com.collectohub.catalog.dto;

import java.util.List;

public record EditorialDataQualityFindingResponse(String groupKey, String displayValue,
        List<Long> recordIds, List<String> recordLabels, String recommendation) {}
