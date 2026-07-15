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
            select item from CollectionItem item
            join fetch item.collection collection
            join fetch item.catalogItem catalogItem
            join fetch catalogItem.series
            left join fetch item.catalogItemEdition
            where collection.id = :collectionId and collection.deletedAt is null
              and catalogItem.series.id = :seriesId and item.deletedAt is null
            order by item.id asc
            """)
    List<CollectionItem> findProgressItemsByCollectionIdAndSeriesId(@Param("collectionId") Long collectionId, @Param("seriesId") Long seriesId);

    @Query("""
            select item
            from CollectionItem item
            join fetch item.collection collection
            left join fetch item.masterProduct masterProduct
            left join fetch masterProduct.category
            left join fetch item.catalogItem catalogItem
            left join fetch catalogItem.series
            left join fetch item.catalogItemEdition
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
