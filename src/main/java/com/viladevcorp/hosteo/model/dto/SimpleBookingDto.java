package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.BaseEntity;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class SimpleBookingDto extends BaseEntityDto {

  public SimpleBookingDto(Booking booking) {
    BeanUtils.copyProperties(booking, this);
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
