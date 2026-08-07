package com.collectohub.shops.infrastructure;

import com.collectohub.shops.domain.ShopMember;
import com.collectohub.shops.domain.ShopMemberRole;
import com.collectohub.shops.domain.ShopMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShopMemberRepository extends JpaRepository<ShopMember, Long> {

    List<ShopMember> findByUser_IdAndStatusAndDeletedAtIsNull(Long userId, ShopMemberStatus status);

    Optional<ShopMember> findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
            Long shopId,
            Long userId,
            ShopMemberStatus status
    );

    List<ShopMember> findByShop_IdAndStatusAndDeletedAtIsNullOrderByIdAsc(
            Long shopId,
            ShopMemberStatus status
    );

    boolean existsByShop_IdAndUser_IdAndRoleInAndStatusAndDeletedAtIsNull(
            Long shopId,
            Long userId,
            Collection<ShopMemberRole> roles,
            ShopMemberStatus status
    );
}
