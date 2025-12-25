package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;
import java.time.Instant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class BookingDto extends BaseEntityDto {

  public BookingDto(Booking booking) {
    if (booking == null) {
      return;
    }
    BeanUtils.copyProperties(booking, this, "apartment");
    this.apartment = new ApartmentDto(booking.getApartment());
  }

  private ApartmentDto apartment;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private BookingState state;

  private BookingSource source;
}
