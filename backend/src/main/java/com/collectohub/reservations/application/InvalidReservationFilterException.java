package com.collectohub.reservations.application;

public class InvalidReservationFilterException extends RuntimeException {

    public InvalidReservationFilterException(String message) {
        super(message);
    }
}
