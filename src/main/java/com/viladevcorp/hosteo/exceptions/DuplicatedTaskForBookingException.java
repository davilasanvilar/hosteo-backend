package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DuplicatedTaskForBookingException extends Exception {
  public DuplicatedTaskForBookingException(String message) {
    super(message);
  }
}
