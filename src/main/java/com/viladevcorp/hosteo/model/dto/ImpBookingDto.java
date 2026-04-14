package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Conflict;
import com.viladevcorp.hosteo.model.ImpBooking;
import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;
import java.time.Instant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class ImpBookingDto extends BaseEntityDto {

  public ImpBookingDto(ImpBooking booking) {
    if (booking == null) {
      return;
    }
    BeanUtils.copyProperties(booking, this, "apartment", "conflict");
    this.apartment = new ApartmentDto(booking.getApartment());
    this.conflict = booking.getConflict();
  }

  private ApartmentDto apartment;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private EventState state;

  private EventSource source;

  private Conflict conflict;

  private String creationError;
}
