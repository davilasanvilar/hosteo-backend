package com.template.backtemplate.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SendEmailException extends Exception {
    public SendEmailException(String message) {
        super(message);
    }
}