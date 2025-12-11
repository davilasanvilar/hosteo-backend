package com.viladevcorp.hosteo.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.AssignmentBeforeEndBookingException;
import com.viladevcorp.hosteo.exceptions.AssignmentNotAtTimeToPrepareNextBookingException;
import com.viladevcorp.hosteo.exceptions.BookingAndTaskNoMatchApartment;
import com.viladevcorp.hosteo.exceptions.CancelledBookingException;
import com.viladevcorp.hosteo.exceptions.CompleteTaskOnNotFinishedBookingException;
import com.viladevcorp.hosteo.exceptions.DuplicatedTaskForBookingException;
import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.exceptions.NotAvailableDatesException;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.forms.AssignmentCreateForm;
import com.viladevcorp.hosteo.model.forms.AssignmentSearchForm;
import com.viladevcorp.hosteo.model.forms.AssignmentUpdateForm;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final TaskService taskService;
  private final WorkerService workerService;
  private final BookingService bookingService;
  private final BookingRepository bookingRepository;

  @Autowired
  public AssignmentService(
      AssignmentRepository assignmentRepository,
      TaskService taskService,
      WorkerService workerService,
      BookingService bookingService,
      BookingRepository bookingRepository) {
    this.assignmentRepository = assignmentRepository;
    this.taskService = taskService;
    this.workerService = workerService;
    this.bookingService = bookingService;
    this.bookingRepository = bookingRepository;
  }

  private void validateAssignment(
      UUID assignmentId,
      Instant startDate,
      Instant endDate,
      AssignmentState assignmentState,
      Task task,
      Worker worker,
      Booking booking)
      throws DuplicatedTaskForBookingException,
          NotAvailableDatesException,
          AssignmentNotAtTimeToPrepareNextBookingException,
          BookingAndTaskNoMatchApartment,
          AssignmentBeforeEndBookingException,
          CancelledBookingException,
          CompleteTaskOnNotFinishedBookingException {
    if (booking.getState() == BookingState.CANCELLED) {
      log.error(
          "[AssignmentService.validateAssignment] - Cannot create assignment for cancelled booking ID {}",
          booking.getId());
      throw new CancelledBookingException("Cannot create assignment for a cancelled booking.");
    }

    // Validate that booking belongs to the same apartment as the task
    if (!booking.getApartment().getId().equals(task.getApartment().getId())) {
      log.error(
          "[AssignmentService.validateAssignment] - Booking apartment ID {} does not match task apartment ID {}",
          booking.getApartment().getId(),
          task.getApartment().getId());
      throw new BookingAndTaskNoMatchApartment(
          "Booking does not belong to the same apartment as the task.");
    }

    // Validate that assignment start date is after booking end date
    if (startDate.isBefore(booking.getEndDate())) {
      log.error(
          "[AssignmentService.validateAssignment] - Assignment start date {} is before booking end date {}",
          startDate,
          booking.getEndDate());
      throw new AssignmentBeforeEndBookingException(
          "Assignment start date cannot be before booking end date.");
    }

    // Validate the assignment is before the next booking for the same apartment
    if (task.isExtra()) {
      Optional<Booking> futureBookingOpt =
          bookingRepository.getNextBookingForApartment(booking.getApartment().getId(), startDate);
      if (futureBookingOpt.isPresent()
          && !endDate.isBefore(futureBookingOpt.get().getStartDate())) {
        log.error(
            "[AssignmentService.validateAssignment] - Assignment end date {} is after next booking start date {}",
            endDate,
            futureBookingOpt.get().getStartDate());
        throw new AssignmentNotAtTimeToPrepareNextBookingException(
            "The assigment won't prepare the apartment at time for next booking");
      }
    }

    // Validate that booking does not already have an assignment for the same task
    if (booking.getAssignments().stream().anyMatch(a -> a.getTask().getId().equals(task.getId()))) {
      log.error(
          "[AssignmentService.validateAssignment] - Booking ID {} already has an assignment for task ID {}",
          booking.getId(),
          task.getId());
      throw new DuplicatedTaskForBookingException(
          "This booking already has an assignment for the specified task.");
    }

    // Validate that apartment is available in the selected dates (not booked nor
    // assignments)
    ServiceUtils.checkApartmentAvailability(
        "[AssignmentService.validateAssignment]",
        bookingRepository,
        assignmentRepository,
        booking.getApartment().getId(),
        startDate,
        endDate,
        null,
        assignmentId);

    // Validate that worker is available in the selected dates
    if (assignmentRepository.checkWorkerAvailability(
        worker.getId(), startDate, endDate, assignmentId)) {
      log.error(
          "[AssignmentService.validateAssignment] - Worker {} is not available between {} and {}",
          worker.getId(),
          startDate,
          endDate);
      throw new NotAvailableDatesException("Worker is not available in the selected dates.");
    }

    // Validate the booking is finished to complete the task
    if (booking.getState() != BookingState.FINISHED
        && assignmentState == AssignmentState.FINISHED) {
      log.error(
          "[AssignmentService.validateAssignment] - Booking ID {} is not finished. Current state: {}",
          booking.getId(),
          booking.getState());
      throw new CompleteTaskOnNotFinishedBookingException(
          "Cannot complete tasks to bookings that are not finished.");
    }
  }

  public Assignment createAssignment(AssignmentCreateForm form)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          DuplicatedTaskForBookingException,
          AssignmentNotAtTimeToPrepareNextBookingException,
          NotAvailableDatesException,
          BookingAndTaskNoMatchApartment,
          AssignmentBeforeEndBookingException,
          CancelledBookingException,
          CompleteTaskOnNotFinishedBookingException {
    Task task;
    try {
      task = taskService.getTaskById(form.getTaskId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to create assignment for this task.");
    }

    Booking booking;
    try {
      booking = bookingService.getBookingById(form.getBookingId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to assign this booking.");
    }

    Worker worker;
    try {
      worker = workerService.getWorkerById(form.getWorkerId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to assign this worker.");
    }

    Instant endDate = form.getStartDate().plusSeconds(task.getDuration() * 60L);

    validateAssignment(null, form.getStartDate(), endDate, form.getState(), task, worker, booking);

    Assignment assignment =
        Assignment.builder()
            .task(task)
            .booking(booking)
            .startDate(form.getStartDate())
            .endDate(endDate)
            .worker(worker)
            .state(form.getState())
            .build();

    return assignmentRepository.save(assignment);
  }

  public Assignment updateAssignment(AssignmentUpdateForm form)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          DuplicatedTaskForBookingException,
          NotAvailableDatesException,
          AssignmentNotAtTimeToPrepareNextBookingException,
          BookingAndTaskNoMatchApartment,
          AssignmentBeforeEndBookingException,
          CancelledBookingException,
          CompleteTaskOnNotFinishedBookingException {
    Assignment assignment = getAssignmentById(form.getId());

    Task task;
    try {
      task = taskService.getTaskById(form.getTaskId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to assign this task.");
    }

    Worker worker;
    try {
      worker = workerService.getWorkerById(form.getWorkerId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to assign this worker.");
    }

    Instant endDate = form.getStartDate().plusSeconds(task.getDuration() * 60L);

    validateAssignment(
        form.getId(),
        form.getStartDate(),
        endDate,
        form.getState(),
        task,
        worker,
        assignment.getBooking());
    BeanUtils.copyProperties(form, assignment, "id");
    assignment.setTask(task);
    assignment.setWorker(worker);

    return assignmentRepository.save(assignment);
  }

  public Assignment getAssignmentById(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Assignment assignment =
        assignmentRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error(
                      "[AssignmentService.getAssignmentById] - Assignment not found with id: {}",
                      id);
                  return new InstanceNotFoundException("Assignment not found with id: " + id);
                });
    try {
      AuthUtils.checkIfCreator(assignment.getTask().getApartment(), "assignment");
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to access this assignment.");
    }
    return assignment;
  }

  public List<Assignment> findAssignments(AssignmentSearchForm form) {
    String taskName =
        form.getTaskName() == null || form.getTaskName().isEmpty()
            ? null
            : "%" + form.getTaskName().toLowerCase() + "%";
    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return assignmentRepository.advancedSearch(
        AuthUtils.getUsername(), taskName, form.getState(), pageRequest);
  }

  public PageMetadata getAssignmentsMetadata(AssignmentSearchForm form) {
    String taskName =
        form.getTaskName() == null || form.getTaskName().isEmpty()
            ? null
            : "%" + form.getTaskName().toLowerCase() + "%";
    int totalRows =
        assignmentRepository.advancedCount(AuthUtils.getUsername(), taskName, form.getState());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteAssignment(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Assignment assignment = getAssignmentById(id);
    assignmentRepository.delete(assignment);
  }
}
