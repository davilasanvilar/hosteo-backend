package com.viladevcorp.hosteo.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.AssignmentsFinishedForBookingException;
import com.viladevcorp.hosteo.exceptions.ExistsBookingAlreadyInProgress;
import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.exceptions.NotAvailableDatesException;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.PageMetadata;
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

    private BookingRepository bookingRepository;
    private ApartmentService apartmentService;
    private AssignmentRepository assignmentRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, ApartmentService apartmentService,
            AssignmentRepository assignmentRepository) {
        this.bookingRepository = bookingRepository;
        this.apartmentService = apartmentService;
        this.assignmentRepository = assignmentRepository;
    }

    private void validateBookingState(UUID bookingId, UUID apartmentId, BookingState state)
            throws ExistsBookingAlreadyInProgress, AssignmentsFinishedForBookingException {
        if (state.equals(BookingState.IN_PROGRESS)
                && bookingRepository.existsBookingByApartmentIdAndState(apartmentId, BookingState.IN_PROGRESS)) {
            log.error(
                    "[BookingService.updateBooking] - Apartment with id: {} already has a booking IN_PROGRESS",
                    apartmentId);
            throw new ExistsBookingAlreadyInProgress(
                    "Apartment already has a booking IN_PROGRESS.");
        }

        if ((BookingState.IN_PROGRESS.equals(state)
                || BookingState.PENDING.equals(state))
                && assignmentRepository.existsAssignmentByBookingIdAndState(bookingId, AssignmentState.FINISHED)) {
            log.error(
                    "[BookingService.updateBooking] - Booking with id: {} cannot be set to PENDING or IN_PROGRESS because it has finished assignments",
                    bookingId);
            throw new AssignmentsFinishedForBookingException(
                    "Booking with finished assignments cannot be set to PENDING or IN_PROGRESS.");
        }

    }

    public Booking createBooking(BookingCreateForm form)
            throws InstanceNotFoundException, NotAvailableDatesException, NotAllowedResourceException,
            ExistsBookingAlreadyInProgress, AssignmentsFinishedForBookingException {
        Apartment apartment;
        try {
            apartment = apartmentService.getApartmentById(form.getApartmentId());
        } catch (NotAllowedResourceException e) {
            throw new NotAllowedResourceException("Not allowed to create booking for this apartment.");
        }

        validateBookingState(null, form.getApartmentId(), form.getState());

        if (checkAvailability(form.getApartmentId(), form.getStartDate(), form.getEndDate(), null).size() > 0) {
            log.error("[BookingService.createBooking] - Apartment with id: {} is not available between {} and {}",
                    form.getApartmentId(), form.getStartDate(), form.getEndDate());
            throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
        }
        if (assignmentRepository.checkAvailability(form.getApartmentId(), form.getStartDate(), form.getEndDate(), null)
                .size() > 0) {
            log.error(
                    "[AssignmentService.validateAssignment] - Apartment {} is not available between {} and {} (assignment scheduled)",
                    form.getApartmentId(), form.getStartDate(), form.getEndDate());
            throw new NotAvailableDatesException(
                    "Apartment is not available in the selected dates.");
        }
        Booking booking = Booking.builder()
                .apartment(apartment)
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .price(form.getPrice())
                .name(form.getName())
                .paid(form.isPaid())
                .source(form.getSource())
                .build();

        return bookingRepository.save(booking);
    }

    public Booking updateBooking(BookingUpdateForm form)
            throws InstanceNotFoundException, NotAllowedResourceException, NotAvailableDatesException,
            AssignmentsFinishedForBookingException, ExistsBookingAlreadyInProgress {
        Booking booking = getBookingById(form.getId());
        UUID apartmentId = booking.getApartment().getId();
        if (checkAvailability(apartmentId, form.getStartDate(), form.getEndDate(), form.getId()).size() > 0) {
            log.error("[BookingService.createBooking] - Apartment with id: {} is not available between {} and {}",
                    apartmentId, form.getStartDate(), form.getEndDate());
            throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
        }
        if (assignmentRepository.checkAvailability(apartmentId, form.getStartDate(), form.getEndDate(), null)
                .size() > 0) {
            log.error(
                    "[AssignmentService.validateAssignment] - Apartment {} is not available between {} and {} (assignment scheduled)",
                    apartmentId, form.getStartDate(), form.getEndDate());
            throw new NotAvailableDatesException(
                    "Apartment is not available in the selected dates.");
        }
        validateBookingState(form.getId(), apartmentId, form.getState());

        BeanUtils.copyProperties(form, booking, "id");

        return bookingRepository.save(booking);
    }

    public Booking updateBookingState(UUID bookingId, BookingState state) throws ExistsBookingAlreadyInProgress,
            AssignmentsFinishedForBookingException, InstanceNotFoundException, NotAllowedResourceException {
        Booking booking = getBookingById(bookingId);
        UUID apartmentId = booking.getApartment().getId();
        validateBookingState(bookingId, apartmentId, state);
        booking.setState(state);
        return bookingRepository.save(booking);
    }

    public Booking getBookingById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
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
        String apartmentName = form.getApartmentName() == null || form.getApartmentName().isEmpty() ? null
                : "%" + form.getApartmentName().toLowerCase() + "%";
        PageRequest pageRequest = null;
        if (form.getPageSize() > 0) {
            int pageNumber = form.getPageNumber() <= 0 ? 0 : form.getPageNumber();
            pageRequest = PageRequest.of(pageNumber, form.getPageSize());
        }
        return bookingRepository.advancedSearch(
                AuthUtils.getUsername(), apartmentName,
                form.getState(),
                form.getStartDate(),
                form.getEndDate(),
                pageRequest);
    }

    public PageMetadata getBookingsMetadata(BookingSearchForm form) {
        String apartmentName = form.getApartmentName() == null || form.getApartmentName().isEmpty() ? null
                : "%" + form.getApartmentName().toLowerCase() + "%";
        int totalRows = bookingRepository.advancedCount(
                AuthUtils.getUsername(),
                apartmentName,
                form.getState(),
                form.getStartDate(),
                form.getEndDate());
        int totalPages = form.getPageSize() > 0 ? ((Double) Math.ceil((double) totalRows /
                form.getPageSize())).intValue() : 1;
        return new PageMetadata(totalPages, totalRows);
    }

    public void deleteBooking(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
    }

    public List<Booking> checkAvailability(UUID apartmentId, Instant startDate, Instant endDate,
            UUID excludeBookingId) {
        return bookingRepository.checkAvailability(apartmentId, startDate, endDate, excludeBookingId);
    }

    public Booking getNextBookingForApartment(UUID apartmentId, Instant fromDate) {
        return bookingRepository.getNextBookingForApartment(apartmentId, fromDate);
    }

}
