package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.MasterProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MasterProductRepository extends JpaRepository<MasterProduct, Long>, JpaSpecificationExecutor<MasterProduct> {

    Optional<MasterProduct> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByIsbnIgnoreCaseAndStatusAndDeletedAtIsNull(String isbn, MasterProductStatus status);

    boolean existsByIsbnIgnoreCaseAndStatusAndDeletedAtIsNullAndIdNot(
            String isbn,
            MasterProductStatus status,
            Long excludedId
    );

    boolean existsByEanIgnoreCaseAndStatusAndDeletedAtIsNull(String ean, MasterProductStatus status);

    boolean existsByEanIgnoreCaseAndStatusAndDeletedAtIsNullAndIdNot(
            String ean,
            MasterProductStatus status,
            Long excludedId
    );

    @Query("""
            select count(product) > 0
            from MasterProduct product
            where product.status = :status
              and product.deletedAt is null
              and lower(trim(product.name)) = :name
              and coalesce(lower(trim(product.franchise)), '') = :franchise
              and coalesce(lower(trim(product.volumeNumber)), '') = :volumeNumber
              and coalesce(lower(trim(product.productLanguage)), '') = :language
            """)
    boolean existsLogicalDuplicate(
            @Param("name") String name,
            @Param("franchise") String franchise,
            @Param("volumeNumber") String volumeNumber,
            @Param("language") String language,
            @Param("status") MasterProductStatus status
    );

    @Query("""
            select count(product) > 0
            from MasterProduct product
            where product.status = :status
              and product.deletedAt is null
              and product.id <> :excludedId
              and lower(trim(product.name)) = :name
              and coalesce(lower(trim(product.franchise)), '') = :franchise
              and coalesce(lower(trim(product.volumeNumber)), '') = :volumeNumber
              and coalesce(lower(trim(product.productLanguage)), '') = :language
            """)
    boolean existsLogicalDuplicateExcludingId(
            @Param("name") String name,
            @Param("franchise") String franchise,
            @Param("volumeNumber") String volumeNumber,
            @Param("language") String language,
            @Param("status") MasterProductStatus status,
            @Param("excludedId") Long excludedId
    );
}
