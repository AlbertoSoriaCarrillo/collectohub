package com.collectohub.catalog.dto;

import java.math.BigDecimal;

public record EditorialLegacyBridgeResponse(
        Long linkId,
        Long masterProductId,
        String masterProductName,
        String linkStatus,
        String linkSource,
        BigDecimal confidenceScore,
        String matchReason,
        Long catalogItemId,
        String catalogItemTitle,
        Long catalogItemEditionId,
        String catalogItemEditionLabel
) {
}
