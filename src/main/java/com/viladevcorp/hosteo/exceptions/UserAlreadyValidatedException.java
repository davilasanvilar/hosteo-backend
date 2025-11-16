package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserAlreadyValidatedException extends Exception {
    public UserAlreadyValidatedException(String message) {
        super(message);
    }
}