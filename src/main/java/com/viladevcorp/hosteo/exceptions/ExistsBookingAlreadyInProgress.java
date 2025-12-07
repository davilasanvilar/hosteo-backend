package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExistsBookingAlreadyInProgress extends Exception {
  public ExistsBookingAlreadyInProgress(String message) {
    super(message);
  }
}
