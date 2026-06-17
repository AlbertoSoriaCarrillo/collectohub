package com.collectohub.reservations.application;

public class ReservationUnavailableException extends RuntimeException {

    public ReservationUnavailableException(String message) {
        super(message);
    }
}
