package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotAllowedResourceException extends Exception {
  public NotAllowedResourceException(String message) {
    super(message);
  }
}
