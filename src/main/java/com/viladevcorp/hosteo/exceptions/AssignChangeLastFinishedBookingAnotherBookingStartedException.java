package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AssignChangeLastFinishedBookingAnotherBookingStartedException extends Exception {
  public AssignChangeLastFinishedBookingAnotherBookingStartedException(String message) {
    super(message);
  }
}
