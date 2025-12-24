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
public class SimpleBookingDto extends BaseEntityDto {

  public SimpleBookingDto(Booking booking) {
    if (booking == null) {
      return;
    }
    BeanUtils.copyProperties(booking, this, "apartment");
    this.apartment = new SimpleApartmentDto(booking.getApartment());
  }

  private SimpleApartmentDto apartment;

  private Instant startDate;

  private Instant endDate;

  private double price;

  private String name;

  private boolean paid;

  private BookingState state;

  private BookingSource source;
}
