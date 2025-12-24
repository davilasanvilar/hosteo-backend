package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.types.Alert;

import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingSchedulerDto extends BaseEntityDto {

  private SimpleBookingDto booking;

  private Alert alert;

  private Set<TaskDto> assignedTasks = new HashSet<>();

  private List<TaskDto> unassignedTasks = new ArrayList<>();

  private boolean hasUnfinishedTasks;

  // Flag used when the booking is next to start and the apartment is ready for the guest (dont care
  // about cleaning tasks)
  private boolean apartmentReady;
}
