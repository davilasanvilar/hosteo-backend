package com.viladevcorp.hosteo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.Worker;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, UUID> {

        @Query("SELECT w FROM Worker w WHERE w.createdBy.username = :username AND (:visible is null OR w.visible = :visible) "
                        + "AND (:name is null OR lower(w.name) like :name) ORDER BY w.visible DESC, w.createdAt DESC")
        List<Worker> advancedSearch(String username, String name, Boolean visible, Pageable pageable);

        @Query("SELECT COUNT(w) FROM Worker w WHERE w.createdBy.username = :username AND (:visible is null OR w.visible = :visible) "
                        + "AND (:name is null OR lower(w.name) like :name)")
        int advancedCount(String username, String name, Boolean visible);

}
