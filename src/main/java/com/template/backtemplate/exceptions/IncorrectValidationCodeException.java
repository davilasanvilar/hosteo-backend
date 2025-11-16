package com.template.backtemplate.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IncorrectValidationCodeException extends Exception {
    public IncorrectValidationCodeException(String message) {
        super(message);
    }
}