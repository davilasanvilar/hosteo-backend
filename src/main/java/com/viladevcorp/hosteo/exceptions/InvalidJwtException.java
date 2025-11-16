package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidJwtException extends Exception {
    public InvalidJwtException(String message) {
        super(message);
    }
}