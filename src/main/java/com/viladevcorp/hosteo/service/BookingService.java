package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BookingService {

    private BookingRepository bookingRepository;
    private ApartmentService apartmentService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, ApartmentService apartmentService) {
        this.bookingRepository = bookingRepository;
        this.apartmentService = apartmentService;
    }

    public Booking createBooking(BookingCreateForm form) throws InstanceNotFoundException {
        Apartment apartment;
        try {
            apartment = apartmentService.getApartmentById(form.getApartmentId());
        } catch (NotAllowedResourceException e) {
            throw new InstanceNotFoundException("Apartment not found with id: " + form.getApartmentId());
        } catch (InstanceNotFoundException e) {
            throw new InstanceNotFoundException("Apartment not found with id: " + form.getApartmentId());
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
            throws InstanceNotFoundException, NotAllowedResourceException {
        Booking booking = bookingRepository.findById(form.getId())
                .orElseThrow(InstanceNotFoundException::new);

        if (!booking.getCreatedBy().getUsername().equals(AuthUtils.getUsername())) {
            throw new NotAllowedResourceException("You are not allowed to update this booking.");
        }

        booking.setStartDate(form.getStartDate());
        booking.setEndDate(form.getEndDate());
        booking.setPrice(form.getPrice());
        booking.setName(form.getName());
        booking.setPaid(form.isPaid());
        booking.setState(form.getState());
        booking.setSource(form.getSource());

        return bookingRepository.save(booking);
    }

    public Booking getBookingById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        try {

            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("[BookingService.getBookingById] - Booking not found with id: {}", id);
                        return new InstanceNotFoundException("Booking not found with id: " + id);
                    });
            try {
                apartmentService.getApartmentById(booking.getApartment().getId());
            } catch (InstanceNotFoundException e) {
                throw new InstanceNotFoundException("Apartment of booking not found with id: " + id);
            }
            return booking;
        } catch (NotAllowedResourceException e) {
            throw new NotAllowedResourceException("You are not allowed to access this booking.");
        }
    }

    public List<Booking> findBookings(BookingSearchForm form) {
        String apartmentName = form.getApartmentName() == null || form.getApartmentName().isEmpty() ? null
                : "%" + form.getApartmentName().toLowerCase() + "%";
        PageRequest pageRequest = null;
        if (form.getPageSize() > 0) {
            int pageNumber = form.getPageNumber() <= 0 ? 0 : form.getPageNumber();
            pageRequest = PageRequest.of(pageNumber, form.getPageSize());
        }
        String state = form.getState() == null ? null : form.getState().name();
        return bookingRepository.advancedSearch(
                AuthUtils.getUsername(), apartmentName,
                state,
                form.getStartDate(),
                form.getEndDate(),
                pageRequest);
    }

    public PageMetadata getBookingsMetadata(BookingSearchForm form) {
        String apartmentName = form.getApartmentName() == null || form.getApartmentName().isEmpty() ? null
                : "%" + form.getApartmentName().toLowerCase() + "%";
        String state = form.getState() == null ? null : form.getState().name();
        int totalRows = bookingRepository.advancedCount(
                AuthUtils.getUsername(),
                apartmentName,
                state,
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

}
