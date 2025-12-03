package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotAvailableDatesException extends Exception {
    public NotAvailableDatesException(String message) {
        super(message);
    }
}