package com.collectohub.collections.dto;

import com.collectohub.collections.domain.CollectionSeriesProgressStatus;

import java.math.BigDecimal;
import java.util.List;

public record CollectionSeriesProgressItemResponse(
        Long catalogItemId, String title, String sequenceLabel, BigDecimal sortOrder,
        Integer firstPublicationYear, CollectionSeriesProgressStatus calculatedStatus,
        List<Long> ownedCollectionItemIds, List<Long> wantedCollectionItemIds,
        List<Long> selectedEditionIds, boolean legacyStatusWarning
) { }
