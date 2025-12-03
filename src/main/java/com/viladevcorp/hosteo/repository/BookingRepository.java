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

        @Query(value = "SELECT b.* FROM bookings b " +
                        "JOIN apartments a ON a.id = b.apartment_id " +
                        "JOIN users u ON u.id = b.created_by " +
                        "WHERE u.username = :username " +
                        "AND (:apartmentName IS NULL OR LOWER(a.name) LIKE :apartmentName) " +
                        "AND (CAST(:state AS VARCHAR) IS NULL OR b.state = CAST(:state AS VARCHAR)) " +
                        "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR b.end_date >= CAST(:startDate AS TIMESTAMP)) " +
                        "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR b.start_date <= CAST(:endDate AS TIMESTAMP)) " +
                        "ORDER BY b.start_date DESC", nativeQuery = true)
        List<Booking> advancedSearch(@Param("username") String username,
                        @Param("apartmentName") String apartmentName,
                        @Param("state") String state,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM bookings b " +
                        "JOIN apartments a ON a.id = b.apartment_id " +
                        "JOIN users u ON u.id = b.created_by " +
                        "WHERE u.username = :username " +
                        "AND (:apartmentName IS NULL OR LOWER(a.name) LIKE :apartmentName) " +
                        "AND (CAST(:state AS VARCHAR) IS NULL OR b.state = CAST(:state AS VARCHAR)) " +
                        "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR b.end_date >= CAST(:startDate AS TIMESTAMP)) " +
                        "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR b.start_date <= CAST(:endDate AS TIMESTAMP)) ", nativeQuery = true)
        int advancedCount(@Param("username") String username,
                        @Param("apartmentName") String apartmentName,
                        @Param("state") String state,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query(value = "SELECT b FROM Booking b " +
                        "WHERE b.apartment.id = :apartmentId " +
                        "AND b.startDate < :endDate " +
                        "AND b.endDate > :startDate " +
                        "AND b.state != BookingState.CANCELLED " +
                        "ORDER BY b.startDate ASC")
        List<Booking> checkAvailability(UUID apartmentId, Instant startDate, Instant endDate);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.apartment.id = :apartmentId " +
                        "AND b.startDate > :date " +
                        "AND b.state != BookingState.CANCELLED " +
                        "ORDER BY b.startDate ASC")
        Booking getNextBookingForApartment(UUID apartmentId, Instant date);

}
