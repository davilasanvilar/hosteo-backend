package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CancelledBookingException extends Exception {
  public CancelledBookingException(String message) {
    super(message);
  }
}
