package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.dto.AssignmentDto;
import com.viladevcorp.hosteo.model.dto.BookingSchedulerDto;
import com.viladevcorp.hosteo.model.dto.TaskDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SchedulerInfo {

  private List<BookingSchedulerDto> bookings = new ArrayList<>();

  private List<BookingSchedulerDto> redAlertBookings = new ArrayList<>();

  private List<BookingSchedulerDto> yellowAlertBookings = new ArrayList<>();

  private Set<AssignmentDto> assignments = new HashSet<>();

  private List<TaskDto> extraTasks = new ArrayList<>();
}
