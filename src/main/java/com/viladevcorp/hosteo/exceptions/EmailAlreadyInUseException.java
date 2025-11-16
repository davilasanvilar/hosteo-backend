package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmailAlreadyInUseException extends Exception {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}