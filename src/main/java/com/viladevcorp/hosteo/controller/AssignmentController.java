package com.viladevcorp.hosteo.controller;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.dto.AssignmentDto;
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
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (NoBookingForAssigmentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NO_BOOKING_FOR_ASSIGNMENT, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_BOOKING, e.getMessage()));
    } catch (ChangeInAssignmentsOfPastBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_BOOKING, e.getMessage()));
    } catch (AssignChangeLastFinishedBookingAnotherBookingStartedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_BOOKING_ANOTHER_BOOKING_STARTED,
                  e.getMessage()));
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
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (NoBookingForAssigmentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NO_BOOKING_FOR_ASSIGNMENT, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_BOOKING, e.getMessage()));
    } catch (ChangeInAssignmentsOfPastBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_BOOKING, e.getMessage()));
    } catch (AssignChangeLastFinishedBookingAnotherBookingStartedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_BOOKING_ANOTHER_BOOKING_STARTED,
                  e.getMessage()));
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
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (NoBookingForAssigmentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NO_BOOKING_FOR_ASSIGNMENT, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_BOOKING, e.getMessage()));
    } catch (ChangeInAssignmentsOfPastBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_BOOKING, e.getMessage()));
    } catch (AssignChangeLastFinishedBookingAnotherBookingStartedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_BOOKING_ANOTHER_BOOKING_STARTED,
                  e.getMessage()));
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

  @PostMapping("/assignment/search")
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
    } catch (ChangeInAssignmentsOfPastBookingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_BOOKING, e.getMessage()));
    } catch (AssignChangeLastFinishedBookingAnotherBookingStartedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_BOOKING_ANOTHER_BOOKING_STARTED,
                  e.getMessage()));
    }
  }
}
