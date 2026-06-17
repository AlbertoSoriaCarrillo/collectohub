package com.collectohub.reservations.dto;

import com.collectohub.reservations.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReservationStatusRequest(
        @NotNull
        ReservationStatus status,

        @Size(max = 1000)
        String shopResponse
) {
}
