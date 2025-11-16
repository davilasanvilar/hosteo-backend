package com.template.backtemplate.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}