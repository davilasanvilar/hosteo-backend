package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.ImpBooking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.viladevcorp.hosteo.model.types.BookingState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImpBookingRepository extends JpaRepository<ImpBooking, UUID> {

  @Query(
      value =
          "SELECT b FROM ImpBooking b "
              + "WHERE b.createdBy.username = :username "
              + "ORDER BY b.startDate ASC")
  List<ImpBooking> getUserImpBookings(@Param("username") String username, Pageable pageable);

  @Query(
      "SELECT b FROM ImpBooking b "
          + "WHERE b.createdBy.username = :username "
          + "AND b.apartment.id = :apartmentId "
          + "AND b.startDate <= :endDate "
          + "AND b.endDate >= :startDate "
          + "AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId) ")
  List<ImpBooking> getImpBookingsInRangeDate(
      String username, UUID apartmentId, Instant endDate, Instant startDate, UUID excludeBookingId);

  @Query(value = "SELECT count(b) FROM ImpBooking b " + "WHERE b.createdBy.username = :username ")
  int countUserImpBookings(@Param("username") String username);

  boolean existsByCreatedByUsername(String username);

  List<ImpBooking> findByName(String name);
}
