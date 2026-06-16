package com.collectohub.shops.infrastructure;

import com.collectohub.shops.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByIdAndDeletedAtIsNull(Long id);
}
