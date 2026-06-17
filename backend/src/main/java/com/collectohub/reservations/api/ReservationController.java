package com.collectohub.reservations.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.reservations.application.ReservationService;
import com.collectohub.reservations.dto.CreateReservationRequest;
import com.collectohub.reservations.dto.ReservationResponse;
import com.collectohub.reservations.dto.UpdateReservationStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a reservation for an available shop product")
    public ReservationResponse createReservation(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return reservationService.createReservation(user, request);
    }

    @GetMapping("/reservations/my")
    @Operation(summary = "List reservations owned by the authenticated user")
    public List<ReservationResponse> myReservations(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long shopId
    ) {
        return reservationService.myReservations(user, status, shopId);
    }

    @GetMapping("/reservations/{reservationId}")
    @Operation(summary = "Get a reservation readable by the authenticated user")
    public ReservationResponse getReservation(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long reservationId
    ) {
        return reservationService.getReservation(user, reservationId);
    }

    @GetMapping("/shops/{shopId}/reservations")
    @Operation(summary = "List reservations for a shop managed by the authenticated user")
    public List<ReservationResponse> shopReservations(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long shopProductId
    ) {
        return reservationService.shopReservations(user, shopId, status, userId, shopProductId);
    }

    @PutMapping("/shops/{shopId}/reservations/{reservationId}/status")
    @Operation(summary = "Update the status of a reservation for a managed shop")
    public ReservationResponse updateReservationStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @PathVariable Long reservationId,
            @Valid @RequestBody UpdateReservationStatusRequest request
    ) {
        return reservationService.updateReservationStatus(user, shopId, reservationId, request);
    }

    @PutMapping("/reservations/{reservationId}/cancel")
    @Operation(summary = "Cancel a reservation owned by the authenticated user")
    public ReservationResponse cancelReservation(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long reservationId
    ) {
        return reservationService.cancelReservation(user, reservationId);
    }
}
