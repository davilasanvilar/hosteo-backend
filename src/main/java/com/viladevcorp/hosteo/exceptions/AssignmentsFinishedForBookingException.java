package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AssignmentsFinishedForBookingException extends Exception {
  public AssignmentsFinishedForBookingException(String message) {
    super(message);
  }
}
