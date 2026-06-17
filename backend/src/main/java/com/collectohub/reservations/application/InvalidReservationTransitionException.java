package com.collectohub.reservations.application;

public class InvalidReservationTransitionException extends RuntimeException {

    public InvalidReservationTransitionException(String message) {
        super(message);
    }
}
