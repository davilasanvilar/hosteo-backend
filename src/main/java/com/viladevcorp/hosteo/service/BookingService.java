package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.BookingUpdateError;
import com.viladevcorp.hosteo.model.dto.BookingWithAssignmentsDto;
import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import java.time.Instant;
import java.util.*;
import javax.management.InstanceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Apartment apartment =
        apartmentRepository.findByIdAndCreatedByUsername(
            form.getApartmentId(), AuthUtils.getUsername());
    if (apartment == null) {
      throw new InstanceNotFoundException("Apartment not found with id: " + form.getApartmentId());
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

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public Booking createBookingInNewTransaction(BookingCreateForm form)
      throws InstanceNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Apartment apartment =
        apartmentRepository.findByIdAndCreatedByUsername(
            form.getApartmentId(), AuthUtils.getUsername());
    if (apartment == null) {
      throw new InstanceNotFoundException("Apartment not found with id: " + form.getApartmentId());
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
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Booking booking = getBookingById(bookingId);
    UUID apartmentId = booking.getApartment().getId();
    booking.setState(state);
    Booking result = bookingRepository.save(booking);
    try {
      workflowService.calculateApartmentState(result.getApartment().getId());
      validateBookingState(bookingId, apartmentId, state, booking.getStartDate());
    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      throw e;
    }
    return result;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public Booking updateBookingStateInNewTransaction(UUID bookingId, BookingState state)
      throws InstanceNotFoundException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    return updateBookingState(bookingId, state);
  }

  public List<BookingUpdateError> updateBulkBookingState(
      List<UUID> bookingIds, BookingState state) {

    List<BookingUpdateError> errors = new ArrayList<>();

    // Retrieve the bookings from DB
    List<Booking> bookings =
        bookingRepository.findInIdsAndCreatedByUsername(bookingIds, AuthUtils.getUsername());

    // Now process the bookings
    for (Booking booking : bookings) {
      try {
        // Call the method that starts a new transaction for each booking.
        updateBookingStateInNewTransaction(booking.getId(), state);
      } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
        errors.add(
            new BookingUpdateError(
                booking, CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS));
      } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
        errors.add(
            new BookingUpdateError(
                booking, CodeErrors.PREV_OF_FINISHED_CANNOT_BE_NOT_PENDING_OR_INPROGRESS));
      } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
        errors.add(
            new BookingUpdateError(
                booking, CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS));
      } catch (NextOfPendingCannotBeInprogressOrFinished e) {
        errors.add(
            new BookingUpdateError(
                booking, CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED));
      } catch (InstanceNotFoundException e) {
        errors.add(new BookingUpdateError(booking, e.getMessage()));
      } catch (Exception e) {
        // Catch any other exception to prevent the main loop from stopping.
        errors.add(new BookingUpdateError(booking, e.getMessage()));
      }
    }

    return errors;
  }

  public Booking getBookingById(UUID id) throws InstanceNotFoundException {
    Booking result = bookingRepository.findByIdAndCreatedByUsername(id, AuthUtils.getUsername());
    if (result == null) {
      throw new InstanceNotFoundException("Booking not found with id: " + id);
    } else {
      return result;
    }
  }

  public BookingWithAssignmentsDto getBookingByIdWithAssigments(UUID id)
      throws InstanceNotFoundException {
    Booking booking = getBookingById(id);
    Set<Assignment> assignments = workflowService.getAssigmentsRelatedToBooking(booking.getId());
    return new BookingWithAssignmentsDto(booking, assignments);
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
        form.getStates(),
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
            form.getStates(),
            form.getStartDate(),
            form.getEndDate());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteBooking(UUID id) throws InstanceNotFoundException {
    Booking booking = getBookingById(id);
    bookingRepository.delete(booking);
    workflowService.calculateApartmentState(booking.getApartment().getId());
  }
}
