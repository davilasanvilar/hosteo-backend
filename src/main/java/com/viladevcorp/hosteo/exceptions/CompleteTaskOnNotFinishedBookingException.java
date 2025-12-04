package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CompleteTaskOnNotFinishedBookingException extends Exception {
    public CompleteTaskOnNotFinishedBookingException(String message) {
        super(message);
    }
}