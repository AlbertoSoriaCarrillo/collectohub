package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface CatalogItemCreatorRepository extends JpaRepository<CatalogItemCreator, Long> {
    Optional<CatalogItemCreator> findByIdAndCatalogItem_IdAndDeletedAtIsNull(Long id, Long itemId);
    List<CatalogItemCreator> findByCatalogItem_IdAndDeletedAtIsNullOrderByCreditOrderAscCreator_NameAscIdAsc(Long itemId);
    List<CatalogItemCreator> findByCreator_IdAndDeletedAtIsNullOrderByCreditOrderAsc(Long creatorId);
    boolean existsByCatalogItem_IdAndCreator_IdAndCreditRoleAndDeletedAtIsNull(
            Long itemId, Long creatorId, CreatorCreditRole role);
    boolean existsByCatalogItem_IdAndCreator_IdAndCreditRoleAndDeletedAtIsNullAndIdNot(
            Long itemId, Long creatorId, CreatorCreditRole role, Long id);
}
