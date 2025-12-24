package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;

import java.time.Instant;
import java.util.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class BookingDto extends BaseEntityDto {

  public BookingDto(Booking booking, Set<Assignment> assignments) {
    if (booking == null) {
      return;
    }
    BeanUtils.copyProperties(booking, this, "apartment", "assignments");
    this.apartment = new ApartmentDto(booking.getApartment());

    List<AssignmentDto> assignmentsDto = new ArrayList<>();
    assignments.forEach(
        assignment -> {
          this.assignments.add(new AssignmentDto(assignment));
        });
    this.assignments =
        this.assignments.stream()
            .sorted(Comparator.comparing(AssignmentDto::getStartDate).reversed())
            .toList();
  }

  private ApartmentDto apartment;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private BookingState state;

  private BookingSource source;

  private List<AssignmentDto> assignments = new ArrayList<>();
}
