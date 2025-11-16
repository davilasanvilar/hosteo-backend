package com.template.backtemplate.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotAllowedResourceException extends Exception {
    public NotAllowedResourceException(String message) {
        super(message);
    }
}