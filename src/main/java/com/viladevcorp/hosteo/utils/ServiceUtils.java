package com.viladevcorp.hosteo.utils;

import java.time.Instant;
import java.util.UUID;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.exceptions.NotAvailableDatesException;
import com.viladevcorp.hosteo.model.BaseEntity;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.management.InstanceNotFoundException;

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
            .checkAvailability(
                AuthUtils.getUsername(), apartmentId, startDate, endDate, excludeBookingId)
            .isEmpty()
        || !assignmentRepository
            .checkAvailability(
                AuthUtils.getUsername(), apartmentId, startDate, endDate, excludeAssignmentId)
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

  public static <T extends BaseEntity> T getEntityById(
      UUID id, JpaRepository<T, UUID> repository, String methodPath, String entityName)
      throws InstanceNotFoundException, NotAllowedResourceException {
    T entity =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("[{}] - {} not found with id: {}", methodPath, entityName, id);
                  return new InstanceNotFoundException(entityName + " not found with id: " + id);
                });
    try {
      AuthUtils.checkIfCreator(entity, entityName);
    } catch (NotAllowedResourceException e) {
      log.error("[{}] - Not allowed to access {} with id: {}", methodPath, entityName, id);
      throw e;
    }
    return entity;
  }
}
