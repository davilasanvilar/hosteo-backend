package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BookingAndTaskNoMatchApartment extends Exception {
  public BookingAndTaskNoMatchApartment(String message) {
    super(message);
  }
}
