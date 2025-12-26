package com.viladevcorp.hosteo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.types.ApartmentState;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

  @Query(
      "SELECT a FROM Apartment a WHERE a.createdBy.username = :username AND (:visible is null OR a.visible = :visible) "
          + "AND (:name is null OR lower(a.name) like :name) AND (:state is null OR a.state = :state) ORDER BY a.visible DESC, a.createdAt DESC ")
  List<Apartment> advancedSearch(
      String username, String name, ApartmentState state, Boolean visible, Pageable pageable);

  @Query(
      "SELECT COUNT(a) FROM Apartment a WHERE a.createdBy.username = :username AND (:visible is null OR a.visible = :visible) "
          + "AND (:name is null OR lower(a.name) like :name) AND (:state is null OR a.state = :state)")
  int advancedCount(String username, String name, ApartmentState state, Boolean visible);

  @Query(
      "SELECT a FROM Apartment a LEFT JOIN FETCH a.tasks t WHERE a.id = :id AND (t.extra=false OR "
          + "NOT EXISTS (SELECT 1 FROM Assignment assig WHERE assig.task=t AND assig.state='FINISHED')) ")
  @NonNull
  Optional<Apartment> findById(@NonNull UUID id);

  Optional<Apartment> findByAirbnbIdAndCreatedByUsername(String airbnbId, String username);

  Optional<Apartment> findByBookingIdAndCreatedByUsername(String bookingId, String username);
}
