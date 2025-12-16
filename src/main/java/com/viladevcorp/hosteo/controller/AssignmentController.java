package com.viladevcorp.hosteo.controller;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.model.dto.AssignmentDto;
import com.viladevcorp.hosteo.model.forms.ExtraTaskWithAssignmentCreateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viladevcorp.hosteo.exceptions.AssignmentBeforeEndBookingException;
import com.viladevcorp.hosteo.exceptions.AssignmentNotAtTimeToPrepareNextBookingException;
import com.viladevcorp.hosteo.exceptions.BookingAndTaskNoMatchApartment;
import com.viladevcorp.hosteo.exceptions.CancelledBookingException;
import com.viladevcorp.hosteo.exceptions.CompleteTaskOnNotFinishedBookingException;
import com.viladevcorp.hosteo.exceptions.DuplicatedTaskForBookingException;
import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.exceptions.NotAvailableDatesException;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.forms.AssignmentCreateForm;
import com.viladevcorp.hosteo.model.forms.AssignmentSearchForm;
import com.viladevcorp.hosteo.model.forms.AssignmentUpdateForm;
import com.viladevcorp.hosteo.service.AssignmentService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationUtils;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class AssignmentController {

  private final AssignmentService assignmentService;

  @Autowired
  public AssignmentController(AssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  @PostMapping("/assignment")
  public ResponseEntity<ApiResponse<AssignmentDto>> createAssignment(
      @Valid @RequestBody AssignmentCreateForm form, BindingResult bindingResult) {
    log.info("[AssignmentController.createAssignment] - Creating assignment");

    ResponseEntity<ApiResponse<AssignmentDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Assignment assignment = assignmentService.createAssignment(form);
      log.info("[AssignmentController.createAssignment] - Assignment created successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new AssignmentDto(assignment)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (DuplicatedTaskForBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.DUPLICATED_TASK_FOR_BOOKING, e.getMessage()));
    } catch (AssignmentNotAtTimeToPrepareNextBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGNMENT_NOT_AT_TIME_TO_PREPARE_NEXT_BOOKING, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (BookingAndTaskNoMatchApartment e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.BOOKING_AND_TASK_APARTMENT_NOT_MATCH, e.getMessage()));
    } catch (AssignmentBeforeEndBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ASSIGNMENT_BEFORE_END_DATE_BOOKING, e.getMessage()));
    } catch (CancelledBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.CANCELLED_BOOKING, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_BOOKING, e.getMessage()));
    }
  }

  @PostMapping("/assignment/extra")
  public ResponseEntity<ApiResponse<AssignmentDto>> createAssignmentExtra(
      @Valid @RequestBody ExtraTaskWithAssignmentCreateForm form, BindingResult bindingResult) {
    log.info("[AssignmentController.createAssignmentExtra] - Creating extra assignment");

    ResponseEntity<ApiResponse<AssignmentDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }
    try {
      Assignment assignment = assignmentService.createExtraAssignment(form);
      log.info(
          "[AssignmentController.createAssignmentExtra] - Extra assignment created successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new AssignmentDto(assignment)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (DuplicatedTaskForBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.DUPLICATED_TASK_FOR_BOOKING, e.getMessage()));
    } catch (AssignmentNotAtTimeToPrepareNextBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGNMENT_NOT_AT_TIME_TO_PREPARE_NEXT_BOOKING, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (BookingAndTaskNoMatchApartment e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.BOOKING_AND_TASK_APARTMENT_NOT_MATCH, e.getMessage()));
    } catch (AssignmentBeforeEndBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ASSIGNMENT_BEFORE_END_DATE_BOOKING, e.getMessage()));
    } catch (CancelledBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.CANCELLED_BOOKING, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_BOOKING, e.getMessage()));
    }
  }

  @PatchMapping("/assignment")
  public ResponseEntity<ApiResponse<AssignmentDto>> updateAssignment(
      @Valid @RequestBody AssignmentUpdateForm form, BindingResult bindingResult) {
    log.info("[AssignmentController.updateAssignment] - Updating assignment");

    ResponseEntity<ApiResponse<AssignmentDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Assignment assignment = assignmentService.updateAssignment(form);
      log.info("[AssignmentController.updateAssignment] - Assignment updated successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new AssignmentDto(assignment)));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (DuplicatedTaskForBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.DUPLICATED_TASK_FOR_BOOKING, e.getMessage()));
    } catch (AssignmentNotAtTimeToPrepareNextBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGNMENT_NOT_AT_TIME_TO_PREPARE_NEXT_BOOKING, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (BookingAndTaskNoMatchApartment e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.BOOKING_AND_TASK_APARTMENT_NOT_MATCH, e.getMessage()));
    } catch (AssignmentBeforeEndBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ASSIGNMENT_BEFORE_END_DATE_BOOKING, e.getMessage()));
    } catch (CancelledBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.CANCELLED_BOOKING, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_BOOKING, e.getMessage()));
    }
  }

  @PatchMapping("/assignment/{id}/state/{state}")
  public ResponseEntity<ApiResponse<AssignmentDto>> updateAssignmentState(
      @PathVariable UUID id, @PathVariable AssignmentState state) {

    log.info("[AssignmentController.updateAssignmentState] - Updating assignment");

    try {
      Assignment assignment = assignmentService.updateAssignmentState(id, state);
      log.info(
          "[AssignmentController.updateAssignmentState] - Assignment state updated successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new AssignmentDto(assignment)));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (DuplicatedTaskForBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.DUPLICATED_TASK_FOR_BOOKING, e.getMessage()));
    } catch (AssignmentNotAtTimeToPrepareNextBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGNMENT_NOT_AT_TIME_TO_PREPARE_NEXT_BOOKING, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (BookingAndTaskNoMatchApartment e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.BOOKING_AND_TASK_APARTMENT_NOT_MATCH, e.getMessage()));
    } catch (AssignmentBeforeEndBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ASSIGNMENT_BEFORE_END_DATE_BOOKING, e.getMessage()));
    } catch (CancelledBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.CANCELLED_BOOKING, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_BOOKING, e.getMessage()));
    }
  }

  @GetMapping("/assignment/{id}")
  public ResponseEntity<ApiResponse<AssignmentDto>> getAssignment(@PathVariable UUID id) {
    log.info("[AssignmentController.getAssignment] - Fetching assignment with id: {}", id);

    try {
      Assignment assignment = assignmentService.getAssignmentById(id);
      log.info("[AssignmentController.getAssignment] - Assignment found successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new AssignmentDto(assignment)));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/assignments/search")
  public ResponseEntity<ApiResponse<Page<AssignmentDto>>> searchAssignments(
      @RequestBody AssignmentSearchForm form) {
    log.info("[AssignmentController.searchAssignments] - Searching assignments");

    List<Assignment> assignments = assignmentService.findAssignments(form);
    PageMetadata pageMetadata = assignmentService.getAssignmentsMetadata(form);
    Page<AssignmentDto> page =
        new Page<>(
            assignments.stream().map(AssignmentDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());

    log.info("[AssignmentController.searchAssignments] - Found {} assignments", assignments.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/assignment/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable UUID id) {
    log.info("[AssignmentController.deleteAssignment] - Deleting assignment with id: {}", id);
    try {
      assignmentService.deleteAssignment(id);
      log.info("[AssignmentController.deleteAssignment] - Assignment deleted successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Assignment deleted successfully."));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }
}
