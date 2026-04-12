package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.types.WorkerState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerRepository extends EntityRepository<Worker> {

  @Query(
      "SELECT w FROM Worker w WHERE w.createdBy.username = :username AND (:visible is null OR w.visible = :visible) "
          + "AND (:state is null OR w.state = :state) AND (:name is null OR lower(w.name) like :name) "
          + "ORDER BY w.visible DESC, w.createdAt DESC")
  List<Worker> advancedSearch(
      String username, String name, WorkerState state, Boolean visible, Pageable pageable);

  @Query(
      "SELECT COUNT(w) FROM Worker w WHERE w.createdBy.username = :username "
          + "AND (:visible is null OR w.visible = :visible) AND (:state is null OR w.state = :state) "
          + "AND (:name is null OR lower(w.name) like :name)")
  int advancedCount(String username, String name, WorkerState state, Boolean visible);
}
