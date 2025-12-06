package com.viladevcorp.hosteo.controller;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

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

import com.viladevcorp.hosteo.exceptions.AssignmentsFinishedForBookingException;
import com.viladevcorp.hosteo.exceptions.ExistsBookingAlreadyInProgress;
import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.exceptions.NotAvailableDatesException;
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

@Slf4j
@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking")
    public ResponseEntity<ApiResponse<Booking>> createBooking(@Valid @RequestBody BookingCreateForm form,
            BindingResult bindingResult) {
        log.info("[BookingController.createBooking] - Creating booking");

        ResponseEntity<ApiResponse<Booking>> validationResponse = ValidationUtils.handleFormValidation(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }

        try {
            Booking booking = bookingService.createBooking(form);
            log.info("[BookingController.createBooking] - Booking created successfully");
            return ResponseEntity.ok().body(new ApiResponse<>(booking));
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Apartment not found"));
        } catch (NotAllowedResourceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, e.getMessage()));
        } catch (NotAvailableDatesException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
        } catch (AssignmentsFinishedForBookingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ASSIGNMENTS_FINISHED_FOR_BOOKING, e.getMessage()));
        } catch (ExistsBookingAlreadyInProgress e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.EXISTS_BOOKING_ALREADY_IN_PROGRESS, e.getMessage()));
        }
    }

    @PatchMapping("/booking")
    public ResponseEntity<ApiResponse<Booking>> updateBooking(@Valid @RequestBody BookingUpdateForm form,
            BindingResult bindingResult) {
        log.info("[BookingController.updateBooking] - Updating booking");

        ResponseEntity<ApiResponse<Booking>> validationResponse = ValidationUtils.handleFormValidation(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }

        try {
            Booking booking = bookingService.updateBooking(form);
            log.info("[BookingController.updateBooking] - Booking updated successfully");
            return ResponseEntity.ok().body(new ApiResponse<>(booking));
        } catch (NotAllowedResourceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, e.getMessage()));
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, e.getMessage()));
        } catch (NotAvailableDatesException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
        } catch (AssignmentsFinishedForBookingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ASSIGNMENTS_FINISHED_FOR_BOOKING, e.getMessage()));
        } catch (ExistsBookingAlreadyInProgress e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.EXISTS_BOOKING_ALREADY_IN_PROGRESS, e.getMessage()));
        }
    }

    @PatchMapping("/booking/{id}/state/{state}")
    public ResponseEntity<ApiResponse<Booking>> completeBooking(@PathVariable UUID id, @PathVariable BookingState state) {
        log.info("[BookingController.completeBooking] - Completing booking with id: {}", id);
        try {
            Booking booking = bookingService.updateBookingState(id, state);
            log.info("[BookingController.completeBooking] - Booking completed successfully");
            return ResponseEntity.ok().body(new ApiResponse<>(booking));
        } catch (NotAllowedResourceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, e.getMessage()));
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, e.getMessage()));
        } catch (AssignmentsFinishedForBookingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ASSIGNMENTS_FINISHED_FOR_BOOKING, e.getMessage()));
        } catch (ExistsBookingAlreadyInProgress e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.EXISTS_BOOKING_ALREADY_IN_PROGRESS, e.getMessage()));
        }
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBooking(@PathVariable UUID id) {
        log.info("[BookingController.getBooking] - Fetching booking with id: {}", id);

        try {
            Booking booking = bookingService.getBookingById(id);
            log.info("[BookingController.getBooking] - Booking found successfully");
            return ResponseEntity.ok().body(new ApiResponse<>(booking));
        } catch (NotAllowedResourceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, e.getMessage()));
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, e.getMessage()));
        }
    }

    @PostMapping("/bookings/search")
    public ResponseEntity<ApiResponse<Page<Booking>>> searchBookings(@RequestBody BookingSearchForm form) {
        log.info("[BookingController.searchBookings] - Searching bookings");

        List<Booking> bookings = bookingService.findBookings(form);
        PageMetadata pageMetadata = bookingService.getBookingsMetadata(form);
        Page<Booking> page = new Page<>(bookings, pageMetadata.getTotalPages(), pageMetadata.getTotalRows());

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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, e.getMessage()));
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, e.getMessage()));
        }
    }

}
