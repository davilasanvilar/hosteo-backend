package com.viladevcorp.hosteo.model.dto;

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

  public BookingDto(Booking booking) {
    BeanUtils.copyProperties(booking, this, "apartment", "assignments");
    this.apartment = new ApartmentDto(booking.getApartment());
    Set<AssignmentDto> assignments = new HashSet<>();
    Set<AssignmentDto> extraAssignments = new HashSet<>();

    booking
        .getAssignments()
        .forEach(
            assignment -> {
              if (assignment.getTask().isExtra()) {
                extraAssignments.add(new AssignmentDto(assignment));
              } else {
                assignments.add(new AssignmentDto(assignment));
              }
            });
    this.assignments =
        assignments.stream()
            .sorted(Comparator.comparing(AssignmentDto::getStartDate).reversed())
            .toList();
    this.extraAssignments =
        extraAssignments.stream()
            .sorted(Comparator.comparing(AssignmentDto::getStartDate).reversed())
            .toList();
  }

  private ApartmentDto apartment;

  private Instant startDate;

  private Instant endDate;

  private double price;

  private String name;

  private boolean paid;

  private BookingState state;

  private BookingSource source;

  private List<AssignmentDto> assignments = new ArrayList<>();

  private List<AssignmentDto> extraAssignments = new ArrayList<>();
}
