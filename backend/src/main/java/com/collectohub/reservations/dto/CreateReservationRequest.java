package com.collectohub.reservations.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
        @NotNull
        Long shopProductId,

        @Positive
        Integer quantity,

        @Size(max = 1000)
        String userMessage
) {
}
