package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AssignmentNotAtTimeToPrepareNextBookingException extends Exception {
  public AssignmentNotAtTimeToPrepareNextBookingException(String message) {
    super(message);
  }
}
