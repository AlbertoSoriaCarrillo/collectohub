package com.collectohub.collections.infrastructure;

import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {

    List<CollectionItem> findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(Long collectionId);

    Optional<CollectionItem> findByIdAndCollection_IdAndDeletedAtIsNull(Long id, Long collectionId);

    @Query("""
            select item
            from CollectionItem item
            join fetch item.collection collection
            join fetch item.masterProduct masterProduct
            join fetch masterProduct.category
            where collection.user.id = :userId
              and collection.deletedAt is null
              and item.deletedAt is null
              and item.collectionStatus in :statuses
            order by item.id asc
            """)
    List<CollectionItem> findRecommendationItemsForUser(
            @Param("userId") Long userId,
            @Param("statuses") Set<CollectionItemStatus> statuses
    );
}
