package com.viladevcorp.hosteo.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.ImpBooking;
import com.viladevcorp.hosteo.model.dto.BookingWithAssignmentsDto;
import com.viladevcorp.hosteo.model.dto.BookingDto;
import com.viladevcorp.hosteo.model.dto.ImpBookingDto;
import com.viladevcorp.hosteo.model.dto.ImportResultDto;
import com.viladevcorp.hosteo.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.service.BookingService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationUtils;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api")
public class BookingController {

  private final BookingService bookingService;

  private final ImportService importService;

  @Autowired
  public BookingController(BookingService bookingService, ImportService importService) {
    this.bookingService = bookingService;
    this.importService = importService;
  }

  @PostMapping("/booking")
  public ResponseEntity<ApiResponse<BookingDto>> createBooking(
      @Valid @RequestBody BookingCreateForm form, BindingResult bindingResult) {
    log.info("[BookingController.createBooking] - Creating booking");

    ResponseEntity<ApiResponse<BookingDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Booking booking = bookingService.createBooking(form);
      log.info("[BookingController.createBooking] - Booking created successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new BookingDto(booking)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, "Apartment not found"));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (NextOfPendingCannotBeInprogressOrFinished e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, e.getMessage()));
    } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, e.getMessage()));
    } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_FINISHED_CANNOT_BE_NOT_PENDING_OR_INPROGRESS, e.getMessage()));
    }
  }

  @PatchMapping("/booking")
  public ResponseEntity<ApiResponse<BookingDto>> updateBooking(
      @Valid @RequestBody BookingUpdateForm form, BindingResult bindingResult) {
    log.info("[BookingController.updateBooking] - Updating booking");

    ResponseEntity<ApiResponse<BookingDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Booking booking = bookingService.updateBooking(form);
      log.info("[BookingController.updateBooking] - Booking updated successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new BookingDto(booking)));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (NextOfPendingCannotBeInprogressOrFinished e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, e.getMessage()));
    } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, e.getMessage()));
    } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_FINISHED_CANNOT_BE_NOT_PENDING_OR_INPROGRESS, e.getMessage()));
    }
  }

  @PatchMapping("/booking/{id}/state/{state}")
  public ResponseEntity<ApiResponse<BookingDto>> updateBookingState(
      @PathVariable UUID id, @PathVariable BookingState state) {
    log.info("[BookingController.completeBooking] - Completing booking with id: {}", id);
    try {
      Booking booking = bookingService.updateBookingState(id, state);
      log.info("[BookingController.completeBooking] - Booking completed successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new BookingDto(booking)));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (NextOfPendingCannotBeInprogressOrFinished e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, e.getMessage()));
    } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, e.getMessage()));
    } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_FINISHED_CANNOT_BE_NOT_PENDING_OR_INPROGRESS, e.getMessage()));
    }
  }

  @GetMapping("/booking/{id}")
  public ResponseEntity<ApiResponse<BookingWithAssignmentsDto>> getBooking(@PathVariable UUID id) {
    log.info("[BookingController.getBooking] - Fetching booking with id: {}", id);

    try {
      BookingWithAssignmentsDto booking = bookingService.getBookingByIdWithAssigments(id);
      log.info("[BookingController.getBooking] - Booking found successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(booking));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/booking/search")
  public ResponseEntity<ApiResponse<Page<BookingDto>>> searchBookings(
      @RequestBody BookingSearchForm form) {
    log.info("[BookingController.searchBookings] - Searching bookings");

    List<Booking> bookings = bookingService.findBookings(form);
    PageMetadata pageMetadata = bookingService.getBookingsMetadata(form);
    Page<BookingDto> page =
        new Page<>(
            bookings.stream().map(BookingDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());

    log.info("[BookingController.searchBookings] - Found {} bookings", bookings.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/booking/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable UUID id) {
    log.info("[BookingController.deleteBooking] - Deleting booking with id: {}", id);
    try {
      bookingService.deleteBooking(id);
      log.info("[BookingController.deleteBooking] - Booking deleted successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Booking deleted successfully."));
    } catch (NotAllowedResourceException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @GetMapping("/booking/import/exists")
  public ResponseEntity<Void> checkExistentImports() {
    log.info("[BookingController.checkExistentImports] - Checking existent imports");
    if (importService.existsImportInProgress()) {
      log.info("[BookingController.checkExistentImports] - Import in progress found");
      return ResponseEntity.ok().build();
    } else {
      log.info("[BookingController.checkExistentImports] - No import in progress found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/booking/import")
  public ResponseEntity<ApiResponse<Page<ImpBookingDto>>> getImportedBookings(
            @RequestParam(defaultValue = "0") int pageNumber) {
    log.info("[BookingController.getImportedBookings] - Searching import bookings");
    List<ImpBooking> bookings = importService.searchUserImpBookings(pageNumber);
    PageMetadata pageMetadata = importService.getImpBookingsMetadata();
    Page<ImpBookingDto> page =
        new Page<>(
            bookings.stream().map(ImpBookingDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());

    log.info(
        "[BookingController.getImportedBookings] - Found {} imported bookings", bookings.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @PostMapping(value = "booking/import/airbnb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<ImpBookingDto>>> importAirbnbBookings(
      @RequestParam("file") MultipartFile multipartFile) {
    log.info("[BookingController.importAirbnbBookings] - Importing Airbnb bookings");
    List<ImpBooking> importedBookings;
    File tempFile = null;
    try {
      tempFile = File.createTempFile("uploaded", ".csv");
      multipartFile.transferTo(tempFile);
      importedBookings = importService.importAirbnbBookings(tempFile);
      log.info(
          "[BookingController.importAirbnbBookings] - Imported {} Airbnb bookings",
          importedBookings.size());
    } catch (Exception e) {
      log.error(
          "[BookingController.importAirbnbBookings] - Error importing Airbnb bookings: {}",
          e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "Error importing Airbnb bookings: " + e.getMessage()));
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
    return ResponseEntity.ok()
        .body(new ApiResponse<>(importedBookings.stream().map(ImpBookingDto::new).toList()));
  }

  @PostMapping(value = "booking/import/booking", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<ImpBookingDto>>> importBookingBookings(
      @RequestParam("file") MultipartFile multipartFile) {
    log.info("[BookingController.importBookingBookings] - Importing Booking bookings");
    List<ImpBooking> importedBookings;
    File tempFile = null;
    try {
      tempFile = File.createTempFile("uploaded", ".csv");
      tempFile.deleteOnExit();
      multipartFile.transferTo(tempFile);
      importedBookings = importService.importBookingBookings(tempFile);
      log.info(
          "[BookingController.importBookingBookings] - Imported {} Booking bookings",
          importedBookings.size());
    } catch (Exception e) {
      log.error(
          "[BookingController.importBookingBookings] - Error importing Booking bookings: {}",
          e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "Error importing Booking bookings: " + e.getMessage()));
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
    return ResponseEntity.ok()
        .body(new ApiResponse<>(importedBookings.stream().map(ImpBookingDto::new).toList()));
  }

  @PostMapping(value = "booking/import/execute")
  public ResponseEntity<ApiResponse<ImportResultDto>> executeImport() {
    log.info("[BookingController.executeImport] - Executing booking import");
    try {
      if (!importService.existsImportInProgress()) {
        log.info("[BookingController.executeImport] - No import in progress found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(null, "No import in progress found"));
      }
      ImportResultDto result = importService.executeImportBookings();
      log.info("[BookingController.executeImport] - Booking import executed successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(result));
    } catch (Exception e) {
      log.error(
          "[BookingController.executeImport] - Error executing booking import: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "Error executing booking import: " + e.getMessage()));
    }
  }

  @DeleteMapping("/booking/import")
  public ResponseEntity<Void> deleteUserImportData() {
    log.info("[BookingController.deleteUserImportData] - Deleting user import data");
    importService.deleteUserImpBookings();
    log.info("[BookingController.deleteUserImportData] - User import data deleted successfully");
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/booking/import/{id}")
  public ResponseEntity<Void> deleteImportedBooking(@PathVariable UUID id) {
    log.info(
        "[BookingController.deleteImportedBooking] - Deleting imported booking with id: {}", id);
    importService.deleteImpBookingById(id);
    log.info("[BookingController.deleteImportedBooking] - Imported booking deleted successfully");
    return ResponseEntity.ok().build();
  }
}
