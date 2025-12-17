package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChangeInAssignmentsOfPastBookingException extends Exception {
  public ChangeInAssignmentsOfPastBookingException(String message) {
    super(message);
  }
}
