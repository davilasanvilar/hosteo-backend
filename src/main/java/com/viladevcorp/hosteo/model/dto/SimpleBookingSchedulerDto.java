package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.types.Alert;
import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class SimpleBookingSchedulerDto extends BaseEntityDto {

  public SimpleBookingSchedulerDto(Booking booking) {
    if (booking == null) {
      return;
    }
    BeanUtils.copyProperties(booking, this);
  }

  public SimpleBookingSchedulerDto(BookingSchedulerDto booking) {
    if (booking == null || booking.getBooking() == null) {
      return;
    }
    BeanUtils.copyProperties(booking.getBooking(), this);
    this.alert = booking.getAlert();
  }

  private UUID bookingId;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private BookingSource source;

  private Alert alert;
}
