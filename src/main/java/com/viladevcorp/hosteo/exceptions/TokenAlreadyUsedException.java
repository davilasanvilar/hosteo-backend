package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TokenAlreadyUsedException extends Exception {
    public TokenAlreadyUsedException(String message) {
        super(message);
    }
}