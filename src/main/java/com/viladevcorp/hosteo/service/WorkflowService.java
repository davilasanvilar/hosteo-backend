package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.AssignmentDto;
import com.viladevcorp.hosteo.model.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.model.forms.ApartmentUpdateForm;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import javax.management.InstanceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkflowService {

  private final ApartmentRepository apartmentRepository;
  private final BookingRepository bookingRepository;
  private final AssignmentRepository assignmentRepository;

  @Autowired
  public WorkflowService(
      ApartmentRepository apartmentRepository,
      BookingRepository bookingRepository,
      AssignmentRepository assignmentRepository) {
    this.apartmentRepository = apartmentRepository;
    this.bookingRepository = bookingRepository;
    this.assignmentRepository = assignmentRepository;
  }

  public void calculateApartmentState(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {

    Apartment apartment =
        ServiceUtils.getEntityById(
            id, apartmentRepository, "WorkflowService.calculateApartmentState", "Apartment");
    ApartmentState resultState = ApartmentState.READY;

    if (bookingRepository.existsBookingByApartmentIdAndState(id, BookingState.IN_PROGRESS)) {
      apartment.setState(ApartmentState.OCCUPIED);
      apartmentRepository.save(apartment);
      return;
    }

    Map<UUID, Task> regularTasksMap = new HashMap<>();

    apartment
        .getTasks()
        .forEach(
            task -> {
              if (!task.isExtra()) {
                regularTasksMap.put(task.getId(), task);
              }
            });
    // If there are no tasks apartment is READY
    if (regularTasksMap.isEmpty()) {
      apartment.setState(ApartmentState.READY);
      apartmentRepository.save(apartment);
      return;
    }

    Set<Assignment> assignmentsAffectingApartmentState = getAssignmentsThatAffectApartmentState(id);

    boolean hasBookingFinished =
        bookingRepository.existsBookingByApartmentIdAndState(id, BookingState.FINISHED);

    for (Assignment assignment : assignmentsAffectingApartmentState) {
      if (assignment.getState().isFinished()) {
        regularTasksMap.remove(assignment.getTask().getId());
      }
    }
    if (hasBookingFinished && !regularTasksMap.isEmpty()) {
      resultState = ApartmentState.USED;
    }
    apartment.setState(resultState);
    apartmentRepository.save(apartment);
  }

  public Set<Assignment> getAssignmentsThatAffectApartmentState(UUID apartmentId)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Set<Assignment> result = Set.of();
    Optional<Booking> lastFinishedBookingOpt =
        bookingRepository.findFirstBookingByApartmentIdAndStateOrderByEndDateDesc(
            apartmentId, BookingState.FINISHED);
    if (lastFinishedBookingOpt.isEmpty()) {
      return result;
    }
    return getAssigmentsRelatedToBooking(lastFinishedBookingOpt.get().getId());
  }

  public Set<Assignment> getAssigmentsRelatedToBooking(UUID bookingId)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Booking booking =
        ServiceUtils.getEntityById(
            bookingId,
            bookingRepository,
            "WorkflowService.getAssigmentsRelatedToBooking",
            "Booking");
    Booking nextBooking =
        bookingRepository
            .findFirstBookingAfterDateWithState(
                booking.getApartment().getId(), booking.getStartDate(), null)
            .orElse(null);

    return assignmentRepository.findByApartmentAndStateAndDateRangeAndExtra(
        booking.getApartment().getId(),
        null,
        booking.getEndDate(),
        nextBooking == null ? null : nextBooking.getStartDate(),
        false);
  }

  //  public SchedulerInfo getSchedulerInfo(Instant startDate, Instant endDate)
  //      throws NotAllowedResourceException, InstanceNotFoundException {
  //    SchedulerInfo schedulerInfo = new SchedulerInfo();
  //    List<Booking> allBookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
  //    for (Booking booking : allBookings) {
  //      Set<Assignment> bookingAssignments = getAssigmentsRelatedToBooking(booking.getId());
  //    }
  //    schedulerInfo.setAssignments(
  //        assignmentRepository
  //            .findByApartmentAndStateAndDateRange(null, null, startDate, endDate)
  //            .stream()
  //            .map(AssignmentDto::new)
  //            .collect(Collectors.toSet()));
  //
  //    return schedulerInfo;
  //  }
}
