package com.viladevcorp.hosteo.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.forms.*;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final WorkflowService workflowService;
  private final WorkerService workerService;
  private final BookingRepository bookingRepository;
  private final TaskRepository taskRepository;

  @Autowired
  public AssignmentService(
      AssignmentRepository assignmentRepository,
      WorkflowService workflowService,
      WorkerService workerService,
      BookingRepository bookingRepository,
      TaskRepository taskRepository) {
    this.assignmentRepository = assignmentRepository;
    this.workflowService = workflowService;
    this.workerService = workerService;
    this.bookingRepository = bookingRepository;
    this.taskRepository = taskRepository;
  }

  private void validateAssignment(
      UUID assignmentId,
      Instant startDate,
      Instant endDate,
      AssignmentState assignmentState,
      Task task,
      Worker worker)
      throws DuplicatedTaskForBookingException,
          NotAvailableDatesException,
          NoBookingForAssigmentException,
          CompleteTaskOnNotFinishedBookingException,
          NotAllowedResourceException,
          InstanceNotFoundException {

    // Validate that apartment is available in the selected dates (not booked nor
    // assignments)
    ServiceUtils.checkApartmentAvailability(
        "[AssignmentService.validateAssignment]",
        bookingRepository,
        assignmentRepository,
        task.getApartment().getId(),
        startDate,
        endDate,
        null,
        assignmentId);

    // Validate that worker is available in the selected dates
    if (assignmentRepository.checkWorkerAvailability(
        AuthUtils.getUsername(), worker.getId(), startDate, endDate, assignmentId)) {
      log.error(
          "[AssignmentService.validateAssignment] - Worker {} is not available between {} and {}",
          worker.getId(),
          startDate,
          endDate);
      throw new NotAvailableDatesException("Worker is not available in the selected dates.");
    }

    if (task.isExtra()) {
      // Validate that the extra task only has one assignment and return at the end
      Set<Assignment> asssigmentsForTask = assignmentRepository.findByTaskId(task.getId());
      if (!asssigmentsForTask.isEmpty()
          && asssigmentsForTask.stream().anyMatch((a) -> !a.getId().equals(assignmentId))) {
        log.error(
            "[AssignmentService.validateAssignment] - Extra task ID {} already has an assignment",
            task.getId());
        throw new DuplicatedTaskForBookingException("This extra task already has an assignment.");
      }
      return;
    }

    // This checks are only for regular tasks

    // Get the just previous booking to the start date of the assigment of the  apartment (the one
    // that should be "cleaned" by the
    // assignment)
    Optional<Booking> bookingOpt =
        bookingRepository.findFirstBookingBeforeDateWithState(
            AuthUtils.getAuthUser().getId(), task.getApartment().getId(), startDate, null);

    if (bookingOpt.isEmpty()) {
      // If the no previous booking, there is no point in creating the assignment
      log.error(
          "[AssignmentService.validateAssignment] - No booking found for apartment ID {}",
          task.getApartment().getId());
      throw new NoBookingForAssigmentException("There is no booking to create the assigment.");
    }
    Booking booking = bookingOpt.get();

    // Validate that the booking does not already have an assignment for the same task
    Set<Assignment> bookingAssignments =
        workflowService.getAssigmentsRelatedToBooking(booking.getId());
    for (Assignment a : bookingAssignments) {
      if (!a.getId().equals(assignmentId) && a.getTask().getId().equals(task.getId())) {
        log.error(
            "[AssignmentService.validateAssignment] - Booking ID {} already has an assignment for task ID {}",
            booking.getId(),
            task.getId());
        throw new DuplicatedTaskForBookingException(
            "This booking already has an assignment for the specified task.");
      }
    }

    // Validate the booking is finished to complete the task
    if (!booking.getState().isFinished() && assignmentState.isFinished()) {
      log.error(
          "[AssignmentService.validateAssignment] - Booking ID {} is not finished. Current state: {}",
          booking.getId(),
          booking.getState());
      throw new CompleteTaskOnNotFinishedBookingException(
          "Cannot complete tasks to bookings that are not finished.");
    }
  }

  // Check whether the assignment can be modified (created/updated/deleted) based on the booking
  // state and time
  public void checkIfAssignmentAtThatTimeCanBeAltered(Instant assignmentStartDate, UUID apartmentId)
      throws AssignChangeLastFinishedBookingAnotherBookingStartedException,
          ChangeInAssignmentsOfPastBookingException {
    // We get the last finished booking for the apartment (the one that affects the apartment state)
    Optional<Booking> lastFinishedBookingOpt =
        bookingRepository.findFirstBookingByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateDesc(
            AuthUtils.getUsername(), apartmentId, BookingState.FINISHED);
    if (lastFinishedBookingOpt.isEmpty()) {
      return;
    }
    // If the assignment start date is before the last finished booking start date, we throw an
    // exception (we cannot modify past bookings)
    if (assignmentStartDate.isBefore(lastFinishedBookingOpt.get().getStartDate())) {
      log.error(
          "[AssignmentService.checkIfBookingAssignmentsCanBeModified] - Cannot modify assignments for past bookings as there are subsequent bookings.");
      throw new ChangeInAssignmentsOfPastBookingException(
          "Cannot modify assignments for a finished booking with subsequent bookings.");
    }
    // Get the next booking (no cancelled) after the last finished one (then we know the time range
    // that
    // affects the apartment state)
    Booking nextBooking =
        bookingRepository
            .findFirstBookingAfterDateWithState(
                AuthUtils.getAuthUser().getId(),
                apartmentId,
                lastFinishedBookingOpt.get().getStartDate(),
                null)
            .orElse(null);

    // If the assignment start date is in the range of the last finished booking and the next
    // booking, we check if the next booking is in progress (then we cannot modify the last finished
    // booking assignments, so the assignments in the range that affects the apartment state)
    if (nextBooking == null) {
      return;
    }
    if (assignmentStartDate.isBefore(nextBooking.getStartDate())) {
      if (nextBooking.getState().isInProgress()) {
        log.error(
            "[AssignmentService.checkIfBookingAssignmentsCanBeModified] - Cannot modify assignments for last finished booking ID {} as new bookings have started after it.",
            lastFinishedBookingOpt.get().getId());
        throw new AssignChangeLastFinishedBookingAnotherBookingStartedException(
            "Cannot modify assignments for a finished booking with subsequent bookings in progress.");
      }
    }
  }

  public Assignment createAssignment(BaseAssignmentCreateForm form)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          DuplicatedTaskForBookingException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedBookingException,
          ChangeInAssignmentsOfPastBookingException,
          AssignChangeLastFinishedBookingAnotherBookingStartedException,
          NoBookingForAssigmentException {
    Task task;
    try {
      task =
          ServiceUtils.getEntityById(
              form.getTaskId(), taskRepository, "AssignmentService.createAssignment", "Task");
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to create assignment for this task.");
    }

    if (!task.isExtra()) {
      checkIfAssignmentAtThatTimeCanBeAltered(form.getStartDate(), task.getApartment().getId());
    }

    Worker worker;
    try {
      worker = workerService.getWorkerById(form.getWorkerId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to assign this worker.");
    }

    validateAssignment(null, form.getStartDate(), form.getEndDate(), form.getState(), task, worker);

    Assignment assignment =
        Assignment.builder()
            .task(task)
            .startDate(form.getStartDate())
            .endDate(form.getEndDate())
            .worker(worker)
            .state(form.getState())
            .build();
    assignment = assignmentRepository.save(assignment);
    workflowService.calculateApartmentState(task.getApartment().getId());
    return assignment;
  }

  public Assignment updateAssignment(AssignmentUpdateForm form)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          DuplicatedTaskForBookingException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedBookingException,
          ChangeInAssignmentsOfPastBookingException,
          AssignChangeLastFinishedBookingAnotherBookingStartedException,
          NoBookingForAssigmentException {
    Assignment assignment = getAssignmentById(form.getId());
    checkIfAssignmentAtThatTimeCanBeAltered(
        assignment.getStartDate(), assignment.getTask().getApartment().getId());
    checkIfAssignmentAtThatTimeCanBeAltered(
        form.getStartDate(), assignment.getTask().getApartment().getId());

    Worker worker;
    try {
      worker = workerService.getWorkerById(form.getWorkerId());
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to assign this worker.");
    }

    Task task = assignment.getTask();

    validateAssignment(
        form.getId(), form.getStartDate(), form.getEndDate(), form.getState(), task, worker);
    BeanUtils.copyProperties(form, assignment, "id");
    assignment.setWorker(worker);
    Assignment result = assignmentRepository.save(assignment);
    workflowService.calculateApartmentState(task.getApartment().getId());
    return result;
  }

  public Assignment updateAssignmentState(UUID assignmentId, AssignmentState newState)
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          DuplicatedTaskForBookingException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedBookingException,
          ChangeInAssignmentsOfPastBookingException,
          AssignChangeLastFinishedBookingAnotherBookingStartedException,
          NoBookingForAssigmentException {
    Assignment assignment = getAssignmentById(assignmentId);
    checkIfAssignmentAtThatTimeCanBeAltered(
        assignment.getStartDate(), assignment.getTask().getApartment().getId());

    validateAssignment(
        assignment.getId(),
        assignment.getStartDate(),
        assignment.getEndDate(),
        newState,
        assignment.getTask(),
        assignment.getWorker());

    assignment.setState(newState);

    Assignment result = assignmentRepository.save(assignment);
    workflowService.calculateApartmentState(assignment.getTask().getApartment().getId());
    return result;
  }

  public Assignment getAssignmentById(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    return ServiceUtils.getEntityById(
        id, assignmentRepository, "AssignmentService.getAssignmentById", "Assignment");
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
      throws InstanceNotFoundException,
          NotAllowedResourceException,
          ChangeInAssignmentsOfPastBookingException,
          AssignChangeLastFinishedBookingAnotherBookingStartedException {
    Assignment assignment = getAssignmentById(id);
    checkIfAssignmentAtThatTimeCanBeAltered(
        assignment.getStartDate(), assignment.getTask().getApartment().getId());
    Apartment apartment = assignment.getTask().getApartment();
    assignmentRepository.delete(assignment);
    workflowService.calculateApartmentState(apartment.getId());
  }
}
