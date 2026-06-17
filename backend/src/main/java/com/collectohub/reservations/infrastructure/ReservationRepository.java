package com.collectohub.reservations.infrastructure;

import com.collectohub.reservations.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    Optional<Reservation> findByIdAndDeletedAtIsNull(Long id);

    List<Reservation> findByUser_IdAndDeletedAtIsNullOrderByIdDesc(Long userId);

    List<Reservation> findByShop_IdAndDeletedAtIsNullOrderByIdDesc(Long shopId);
}
