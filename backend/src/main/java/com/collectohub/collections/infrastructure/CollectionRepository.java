package com.collectohub.collections.infrastructure;

import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long>, JpaSpecificationExecutor<Collection> {

    List<Collection> findByUser_IdAndDeletedAtIsNullOrderByIdAsc(Long userId);

    Optional<Collection> findByIdAndDeletedAtIsNull(Long id);

    List<Collection> findByVisibilityAndDeletedAtIsNullOrderByIdAsc(CollectionVisibility visibility);
}
