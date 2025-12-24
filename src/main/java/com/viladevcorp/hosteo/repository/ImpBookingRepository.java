package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.ImpBooking;
import java.util.List;
import java.util.UUID;
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
              + "ORDER BY b.apartment.name DESC")
  List<Booking> advancedSearch(@Param("username") String username, Pageable pageable);
}
