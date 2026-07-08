package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.Creator;

public record CreatorResponse(Long id, String name, String slug, String sortName, String biography,
                              String country, Integer birthYear, Integer deathYear, String recordStatus) {
    public static CreatorResponse from(Creator creator) {
        return new CreatorResponse(creator.getId(), creator.getName(), creator.getSlug(),
                creator.getSortName(), creator.getBiography(), creator.getCountry(),
                creator.getBirthYear(), creator.getDeathYear(), creator.getRecordStatus().name());
    }
}
