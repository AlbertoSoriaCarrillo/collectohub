package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByDeletedAtIsNullOrderByCodeAsc();

    Optional<ProductCategory> findByCodeAndDeletedAtIsNull(String code);
}
