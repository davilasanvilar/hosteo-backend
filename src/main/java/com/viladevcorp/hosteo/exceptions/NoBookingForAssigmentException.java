package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NoBookingForAssigmentException extends Exception {
  public NoBookingForAssigmentException(String message) {
    super(message);
  }
}
