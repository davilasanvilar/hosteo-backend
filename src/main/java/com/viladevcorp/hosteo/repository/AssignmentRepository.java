package com.viladevcorp.hosteo.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.types.AssignmentState;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

  @Query(
      "SELECT a FROM Assignment a WHERE a.createdBy.username = :username "
          + "AND (:taskName IS NULL OR LOWER(a.task.name) LIKE :taskName) "
          + "AND (:state IS NULL OR a.state = :state) "
          + "ORDER BY a.startDate DESC")
  List<Assignment> advancedSearch(
      @Param("username") String username,
      @Param("taskName") String taskName,
      @Param("state") AssignmentState state,
      Pageable pageable);

  @Query(
      "SELECT COUNT(a) FROM Assignment a WHERE a.createdBy.username = :username "
          + "AND (:taskName IS NULL OR LOWER(a.task.name) LIKE :taskName) "
          + "AND (:state IS NULL OR a.state = :state)")
  int advancedCount(
      @Param("username") String username,
      @Param("taskName") String taskName,
      @Param("state") AssignmentState state);

  List<Assignment> findByTaskId(UUID taskId);

  List<Assignment> findByWorkerId(UUID workerId);

  @Query(
      value =
          "SELECT a FROM Assignment a "
              + "WHERE a.task.apartment.id = :apartmentId "
              + "AND a.startDate < :endDate "
              + "AND a.endDate > :startDate "
              + "AND (:excludeAssignmentId IS NULL OR a.id != :excludeAssignmentId) ")
  List<Assignment> checkAvailability(
      UUID apartmentId, Instant startDate, Instant endDate, UUID excludeAssignmentId);

  @Query(
      value =
          "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Assignment a "
              + "WHERE a.worker.id = :workerId "
              + "AND a.startDate < :endDate "
              + "AND a.endDate > :startDate "
              + "AND (:excludeAssignmentId IS NULL OR a.id != :excludeAssignmentId) ")
  boolean checkWorkerAvailability(
      UUID workerId, Instant startDate, Instant endDate, UUID excludeAssignmentId);


  boolean existsAssignmentByBookingIdAndState(UUID bookingId, AssignmentState state);
}
