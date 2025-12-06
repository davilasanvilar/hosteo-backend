package com.viladevcorp.hosteo.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.viladevcorp.hosteo.model.types.BookingState;

import com.viladevcorp.hosteo.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

        @Query(value = "SELECT b FROM Booking b " +
                        "WHERE b.createdBy.username = :username " +
                        "AND (:apartmentName IS NULL OR LOWER(b.apartment.name) LIKE :apartmentName) " +
                        "AND (:state IS NULL OR b.state = :state) " +
                        "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR b.endDate >= :startDate) " +
                        "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR b.startDate <= :endDate) " +
                        "ORDER BY b.startDate DESC")
        List<Booking> advancedSearch(@Param("username") String username,
                        @Param("apartmentName") String apartmentName,
                        @Param("state") BookingState state,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);

        @Query(value = "SELECT COUNT(b) FROM Booking b " +
                        "WHERE b.createdBy.username = :username " +
                        "AND (:apartmentName IS NULL OR LOWER(b.apartment.name) LIKE :apartmentName) " +
                        "AND (:state IS NULL OR b.state = :state) " +
                        "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR b.endDate >= :startDate) " +
                        "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR b.startDate <= :endDate) ")
        int advancedCount(@Param("username") String username,
                        @Param("apartmentName") String apartmentName,
                        @Param("state") BookingState state,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query(value = "SELECT b FROM Booking b " +
                        "WHERE b.apartment.id = :apartmentId " +
                        "AND b.startDate < :endDate " +
                        "AND b.endDate > :startDate " +
                        "AND b.state != BookingState.CANCELLED " +
                        "AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId) " +
                        "ORDER BY b.startDate ASC")
        List<Booking> checkAvailability(UUID apartmentId, Instant startDate, Instant endDate, UUID excludeBookingId);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.apartment.id = :apartmentId " +
                        "AND b.startDate > :date " +
                        "AND b.state != BookingState.CANCELLED " +
                        "ORDER BY b.startDate ASC")
        Booking getNextBookingForApartment(UUID apartmentId, Instant date);

}
