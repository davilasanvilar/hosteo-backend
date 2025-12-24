package com.viladevcorp.hosteo.service;

import java.time.Instant;
import java.util.*;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.BookingDto;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
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
  private final AssignmentRepository assignmentRepository;
  private final WorkflowService workflowService;
  private final ApartmentRepository apartmentRepository;

  @Autowired
  public BookingService(
      BookingRepository bookingRepository,
      WorkflowService workflowService,
      AssignmentRepository assignmentRepository,
      ApartmentRepository apartmentRepository,
      AssignmentService assignmentService) {
    this.bookingRepository = bookingRepository;
    this.workflowService = workflowService;
    this.assignmentRepository = assignmentRepository;
    this.apartmentRepository = apartmentRepository;
  }

  private void validateBookingState(
      UUID bookingId, UUID apartmentId, BookingState state, Instant startDate)
      throws NextOfPendingCannotBeInprogressOrFinished,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfInProgressCannotBeFinishedOrInProgress {
    if (state.isPending()) {
      Optional<Booking> nextBookingOpt =
          bookingRepository.findFirstBookingAfterDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Booking nextBooking = nextBookingOpt.orElse(null);
      if (nextBooking != null
          && (nextBooking.getState().isInProgress() || nextBooking.getState().isFinished())) {
        log.error(
            "[BookingService.validateBookingState] - Booking cannot be set to PENDING because there is a next booking IN_PROGRESS or FINISHED for apartment id: {}",
            apartmentId);
        throw new NextOfPendingCannotBeInprogressOrFinished();
      }
    }
    if (state.isInProgress()) {
      Optional<Booking> previousBookingOpt =
          bookingRepository.findFirstBookingBeforeDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Booking previousBooking = previousBookingOpt.orElse(null);
      if (previousBooking != null
          && (previousBooking.getState().isPending()
              || previousBooking.getState().isInProgress())) {
        log.error(
            "[BookingService.validateBookingState] - Booking cannot be set to IN_PROGRESS because there is a previous booking PENDING or IN_PROGRESS for apartment id: {}",
            apartmentId);
        throw new PrevOfInProgressCannotBePendingOrInProgress();
      }
      Optional<Booking> nextBookingOpt =
          bookingRepository.findFirstBookingAfterDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Booking nextBooking = nextBookingOpt.orElse(null);
      if (nextBooking != null
          && (nextBooking.getState().isInProgress() || nextBooking.getState().isFinished())) {
        log.error(
            "[BookingService.validateBookingState] - Booking cannot be set to IN_PROGRESS because there is a next booking IN_PROGRESS or FINISHED for apartment id: {}",
            apartmentId);
        throw new NextOfInProgressCannotBeFinishedOrInProgress();
      }
    }
    if (state.isFinished()) {
      Optional<Booking> previousBookingOpt =
          bookingRepository.findFirstBookingBeforeDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Booking previousBooking = previousBookingOpt.orElse(null);
      if (previousBooking != null
          && (previousBooking.getState().isPending()
              || previousBooking.getState().isInProgress())) {
        log.error(
            "[BookingService.validateBookingState] - Booking cannot be set to FINISHED because there is a previous booking PENDING or IN_PROGRESS for apartment id: {}",
            apartmentId);
        throw new PrevOfFinishedCannotBeNotPendingOrInProgress();
      }
    }
  }

  public Booking createBooking(BookingCreateForm form)
      throws InstanceNotFoundException,
          NotAvailableDatesException,
          NotAllowedResourceException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Apartment apartment;
    try {
      apartment =
          ServiceUtils.getEntityById(
              form.getApartmentId(),
              apartmentRepository,
              "BookingService.createBooking",
              "Apartment");
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to create booking for this apartment.");
    }

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
            .name(form.getName())
            .state(form.getState())
            .source(form.getSource())
            .build();

    Booking result = bookingRepository.save(booking);
    workflowService.calculateApartmentState(form.getApartmentId());
    validateBookingState(
        result.getId(), form.getApartmentId(), form.getState(), form.getStartDate());

    return result;
  }

  public Booking updateBooking(BookingUpdateForm form)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
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

    BeanUtils.copyProperties(form, booking, "id");

    Booking result = bookingRepository.save(booking);
    workflowService.calculateApartmentState(result.getApartment().getId());
    validateBookingState(form.getId(), apartmentId, form.getState(), form.getStartDate());

    return result;
  }

  public Booking updateBookingState(UUID bookingId, BookingState state)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Booking booking = getBookingById(bookingId);
    UUID apartmentId = booking.getApartment().getId();
    booking.setState(state);
    Booking result = bookingRepository.save(booking);
    workflowService.calculateApartmentState(result.getApartment().getId());
    validateBookingState(bookingId, apartmentId, state, booking.getStartDate());

    return result;
  }

  public Booking getBookingById(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    return ServiceUtils.getEntityById(
        id, bookingRepository, "BookingService.getBookingById", "Booking");
  }

  public BookingDto getBookingByIdWithAssigments(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Booking booking = getBookingById(id);
    Set<Assignment> assignments = workflowService.getAssigmentsRelatedToBooking(booking.getId());
    return new BookingDto(booking, assignments);
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
    workflowService.calculateApartmentState(booking.getApartment().getId());
  }

  public List<Booking> checkAvailability(
      UUID apartmentId, Instant startDate, Instant endDate, UUID excludeBookingId) {
    return bookingRepository.checkAvailability(
        AuthUtils.getUsername(), apartmentId, startDate, endDate, excludeBookingId);
  }
}
