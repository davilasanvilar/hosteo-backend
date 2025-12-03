package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AssignmentBeforeEndBookingException extends Exception {
    public AssignmentBeforeEndBookingException(String message) {
        super(message);
    }
}