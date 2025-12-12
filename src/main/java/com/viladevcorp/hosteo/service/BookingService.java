package com.viladevcorp.hosteo.service;

import java.time.Instant;
import java.util.*;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.AssignmentsFinishedForBookingException;
import com.viladevcorp.hosteo.exceptions.ExistsBookingAlreadyInProgress;
import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.exceptions.NotAvailableDatesException;
import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BookingService {

  private final BookingRepository bookingRepository;
  private final ApartmentService apartmentService;
  private final AssignmentRepository assignmentRepository;
  private final ApartmentRepository apartmentRepository;

  @Autowired
  public BookingService(
      BookingRepository bookingRepository,
      ApartmentService apartmentService,
      AssignmentRepository assignmentRepository,
      ApartmentRepository apartmentRepository) {
    this.bookingRepository = bookingRepository;
    this.apartmentService = apartmentService;
    this.assignmentRepository = assignmentRepository;
    this.apartmentRepository = apartmentRepository;
  }

  private void validateBookingState(UUID bookingId, UUID apartmentId, BookingState state)
      throws ExistsBookingAlreadyInProgress, AssignmentsFinishedForBookingException {
    if (state.equals(BookingState.IN_PROGRESS)
        && bookingRepository.existsBookingByApartmentIdAndState(
            apartmentId, BookingState.IN_PROGRESS)) {
      log.error(
          "[BookingService.updateBooking] - Apartment with id: {} already has a booking IN_PROGRESS",
          apartmentId);
      throw new ExistsBookingAlreadyInProgress("Apartment already has a booking IN_PROGRESS.");
    }

    if ((BookingState.IN_PROGRESS.equals(state) || BookingState.PENDING.equals(state))
        && assignmentRepository.existsAssignmentByBookingIdAndState(
            bookingId, AssignmentState.FINISHED)) {
      log.error(
          "[BookingService.updateBooking] - Booking with id: {} cannot be set to PENDING or IN_PROGRESS because it has finished assignments",
          bookingId);
      throw new AssignmentsFinishedForBookingException(
          "Booking with finished assignments cannot be set to PENDING or IN_PROGRESS.");
    }
  }

  public Booking createBooking(BookingCreateForm form)
      throws InstanceNotFoundException,
          NotAvailableDatesException,
          NotAllowedResourceException,
          ExistsBookingAlreadyInProgress,
          AssignmentsFinishedForBookingException {
    Apartment apartment;
    try {
      apartment = apartmentService.getApartmentById(form.getApartmentId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to create booking for this apartment.");
    }

    validateBookingState(null, form.getApartmentId(), form.getState());

    ServiceUtils.checkApartmentAvailability(
        "BookingService.createBooking",
        bookingRepository,
        assignmentRepository,
        form.getApartmentId(),
        form.getStartDate(),
        form.getEndDate(),
        null,
        null);

    Booking booking =
        Booking.builder()
            .apartment(apartment)
            .startDate(form.getStartDate())
            .endDate(form.getEndDate())
            .price(form.getPrice())
            .name(form.getName())
            .paid(form.isPaid())
            .source(form.getSource())
            .build();

    Booking result = bookingRepository.save(booking);
    // If the inserted booking is finished, recalculate the apartment state (only state that can
    // modify the apartment state on creation)
    if (form.getState().isFinished()) {
      calculateApartmentState(form.getApartmentId());
    }
    return result;
  }

  public Booking updateBooking(BookingUpdateForm form)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          NotAvailableDatesException,
          AssignmentsFinishedForBookingException,
          ExistsBookingAlreadyInProgress {
    Booking booking = getBookingById(form.getId());
    UUID apartmentId = booking.getApartment().getId();
    ServiceUtils.checkApartmentAvailability(
        "BookingService.updateBooking",
        bookingRepository,
        assignmentRepository,
        apartmentId,
        form.getStartDate(),
        form.getEndDate(),
        form.getId(),
        null);

    validateBookingState(form.getId(), apartmentId, form.getState());

    BeanUtils.copyProperties(form, booking, "id");

    Booking result = bookingRepository.save(booking);
    calculateApartmentState(result.getApartment().getId());
    return result;
  }

  public Booking updateBookingState(UUID bookingId, BookingState state)
      throws ExistsBookingAlreadyInProgress,
          AssignmentsFinishedForBookingException,
          InstanceNotFoundException,
          NotAllowedResourceException {
    Booking booking = getBookingById(bookingId);
    UUID apartmentId = booking.getApartment().getId();
    validateBookingState(bookingId, apartmentId, state);
    booking.setState(state);
    Booking result = bookingRepository.save(booking);
    calculateApartmentState(result.getApartment().getId());
    return result;
  }

  public Booking getBookingById(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Booking booking =
        bookingRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("[BookingService.getBookingById] - Booking not found with id: {}", id);
                  return new InstanceNotFoundException("Booking not found with id: " + id);
                });
    try {
      AuthUtils.checkIfCreator(booking, "booking");
    } catch (NotAllowedResourceException e) {
      log.error("[BookingService.getBookingById] - Not allowed to access booking with id: {}", id);
      throw e;
    }
    return booking;
  }

  public List<Booking> findBookings(BookingSearchForm form) {
    String apartmentName =
        form.getApartmentName() == null || form.getApartmentName().isEmpty()
            ? null
            : "%" + form.getApartmentName().toLowerCase() + "%";
    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return bookingRepository.advancedSearch(
        AuthUtils.getUsername(),
        apartmentName,
        form.getState(),
        form.getStartDate(),
        form.getEndDate(),
        pageRequest);
  }

  public PageMetadata getBookingsMetadata(BookingSearchForm form) {
    String apartmentName =
        form.getApartmentName() == null || form.getApartmentName().isEmpty()
            ? null
            : "%" + form.getApartmentName().toLowerCase() + "%";
    int totalRows =
        bookingRepository.advancedCount(
            AuthUtils.getUsername(),
            apartmentName,
            form.getState(),
            form.getStartDate(),
            form.getEndDate());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteBooking(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
    Booking booking = getBookingById(id);
    bookingRepository.delete(booking);
  }

  public List<Booking> checkAvailability(
      UUID apartmentId, Instant startDate, Instant endDate, UUID excludeBookingId) {
    return bookingRepository.checkAvailability(apartmentId, startDate, endDate, excludeBookingId);
  }

  public Booking findMostRecentBookingByApartmentIdAndState(UUID apartmentId, BookingState state)
      throws NotAllowedResourceException, InstanceNotFoundException {
    Optional<Booking> booking =
        bookingRepository.findMostRecentBookingByApartmentIdAndState(apartmentId, state);
    if (booking.isPresent()) {
      try {
        AuthUtils.checkIfCreator(booking.get(), "booking");
      } catch (NotAllowedResourceException e) {
        log.error(
            "[BookingService.findMostRecentBookingByApartmentIdAndState] - Not allowed to access booking with id: {}",
            booking.get().getId());
        throw e;
      }
    } else {
      log.info(
          "[BookingService.findMostRecentBookingByApartmentIdAndState] - No booking found for apartment id: {} with state: {}",
          apartmentId,
          state);
      throw new InstanceNotFoundException(
          "Booking not found for apartment id: " + apartmentId + " with state: " + state);
    }
    return booking.get();
  }

  public void calculateApartmentState(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Apartment apartment = apartmentService.getApartmentById(id);
    ApartmentState resultState = ApartmentState.READY;

    if (bookingRepository.existsBookingByApartmentIdAndState(id, BookingState.IN_PROGRESS)) {
      apartment.setState(ApartmentState.OCCUPIED);
      apartmentRepository.save(apartment);
      return;
    }

    Booking lastFinishedBooking;
    try {
      lastFinishedBooking = findMostRecentBookingByApartmentIdAndState(id, BookingState.FINISHED);
    } catch (InstanceNotFoundException e) {
      // No finished bookings found
      apartment.setState(ApartmentState.READY);
      apartmentRepository.save(apartment);
      return;
    }
    Map<UUID, Task> regularTasksMap = new HashMap<>();
    lastFinishedBooking
        .getApartment()
        .getTasks()
        .forEach(regTask -> regularTasksMap.put(regTask.getId(), regTask));

    Set<Assignment> bookingAssignments = lastFinishedBooking.getAssignments();

    // If there are no assignments, either because there are no tasks (user should manually set
    // READY) or because no tasks were assigned, set USED
    if (bookingAssignments.isEmpty()) {
      apartment.setState(ApartmentState.USED);
      apartmentRepository.save(apartment);
      return;
    }
    for (Assignment assignment : bookingAssignments) {
      if (assignment.getTask().isExtra() && !assignment.getState().isFinished()) {
        resultState = ApartmentState.USED;
        break;
      }
      if (!assignment.getTask().isExtra() && assignment.getState().isFinished()) {
        regularTasksMap.remove(assignment.getTask().getId());
      }
    }
    if (!regularTasksMap.isEmpty()) {
      resultState = ApartmentState.USED;
    }
    apartment.setState(resultState);
    apartmentRepository.save(apartment);
  }
}
