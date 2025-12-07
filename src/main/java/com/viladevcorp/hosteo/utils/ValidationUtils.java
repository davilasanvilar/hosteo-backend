package com.viladevcorp.hosteo.utils;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidationUtils {
  public static <T> ResponseEntity<ApiResponse<T>> handleFormValidation(
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      List<ValidationError> validationErrors =
          bindingResult.getAllErrors().stream()
              .map(
                  error -> {
                    if (error instanceof FieldError fe) {
                      return new ValidationError(fe.getField(), error.getDefaultMessage());
                    } else {
                      return new ValidationError(null, error.getDefaultMessage());
                    }
                  })
              .toList();
      log.error(
          "[ValidationUtils.handleFormValidation] - Validation failed with errors: {}",
          validationErrors);
      return ResponseEntity.badRequest()
          .body(new ApiResponse<T>("Validation Failed", validationErrors));
    }
    return null;
  }
}
