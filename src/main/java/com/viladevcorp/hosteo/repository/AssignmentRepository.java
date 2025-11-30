package com.viladevcorp.hosteo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.types.TaskState;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    @Query("SELECT a FROM Assignment a WHERE a.createdBy.username = :username "
            + "AND (:taskName IS NULL OR LOWER(a.task.name) LIKE :taskName) "
            + "AND (:workerName IS NULL OR LOWER(a.worker.name) LIKE :workerName) "
            + "AND (:state IS NULL OR a.state = :state) "
            + "ORDER BY a.startDate DESC")
    List<Assignment> advancedSearch(@Param("username") String username,
            @Param("taskName") String taskName,
            @Param("workerName") String workerName,
            @Param("state") TaskState state,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.createdBy.username = :username "
            + "AND (:taskName IS NULL OR LOWER(a.task.name) LIKE :taskName) "
            + "AND (:workerName IS NULL OR LOWER(a.worker.name) LIKE :workerName) "
            + "AND (:state IS NULL OR a.state = :state)")
    int advancedCount(@Param("username") String username,
            @Param("taskName") String taskName,
            @Param("workerName") String workerName,
            @Param("state") TaskState state);

    List<Assignment> findByTaskId(UUID taskId);

    List<Assignment> findByWorkerId(UUID workerId);
}
