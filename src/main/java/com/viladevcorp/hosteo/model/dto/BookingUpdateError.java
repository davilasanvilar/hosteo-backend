package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.utils.CodeErrors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class BookingUpdateError extends BaseEntityDto {

  public BookingUpdateError(Booking booking, String error) {
    this.booking = booking == null ? null : booking.toDto();
    this.error = error;
  }

  BookingDto booking;
  String error;
}
