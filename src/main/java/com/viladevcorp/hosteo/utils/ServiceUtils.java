package com.viladevcorp.hosteo.utils;

import java.time.Instant;
import java.util.UUID;

import com.viladevcorp.hosteo.exceptions.NotAvailableDatesException;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

@Slf4j
public class ServiceUtils {
  public static int calculateTotalPages(int pageSize, long totalRows) {
    return pageSize > 0 ? ((Double) Math.ceil((double) totalRows / pageSize)).intValue() : 1;
  }

  public static PageRequest createPageRequest(int pageNumber, int pageSize) {
    if (pageSize > 0) {
      pageNumber = Math.max(0, pageNumber);
      return PageRequest.of(pageNumber, pageSize);
    } else {
      return null;
    }
  }

  public static void checkApartmentAvailability(
      String methodName,
      BookingRepository bookingRepository,
      AssignmentRepository assignmentRepository,
      UUID apartmentId,
      Instant startDate,
      Instant endDate,
      UUID excludeBookingId,
      UUID excludeAssignmentId)
      throws NotAvailableDatesException {
    if (!bookingRepository
            .checkAvailability(apartmentId, startDate, endDate, excludeBookingId)
            .isEmpty()
        || !assignmentRepository
            .checkAvailability(apartmentId, startDate, endDate, excludeAssignmentId)
            .isEmpty()) {
      log.error(
          "[{}] - Apartment with id: {} is not available between {} and {}",
          methodName,
          apartmentId,
          startDate,
          endDate);
      throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
    }
  }
}
