package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UsernameAlreadyInUseException extends Exception {
    public UsernameAlreadyInUseException(String message) {
        super(message);
    }
}