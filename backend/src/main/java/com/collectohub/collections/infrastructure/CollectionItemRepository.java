package com.collectohub.collections.infrastructure;

import com.collectohub.collections.domain.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {

    List<CollectionItem> findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(Long collectionId);

    Optional<CollectionItem> findByIdAndCollection_IdAndDeletedAtIsNull(Long id, Long collectionId);
}
