package com.collectohub.reservations.application;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Long reservationId) {
        super("Reservation not found: " + reservationId);
    }
}
