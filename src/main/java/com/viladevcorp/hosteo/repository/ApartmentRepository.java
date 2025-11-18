package com.viladevcorp.hosteo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.types.ApartmentState;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

        @Query("SELECT a FROM Apartment a WHERE a.createdBy.username = :username AND (:visible is null OR a.visible = :visible) "
                        + "AND (:name is null OR lower(a.name) like :name) AND (:state is null OR a.state = :state) ORDER BY a.createdAt DESC, a.visible DESC")
        List<Apartment> advancedSearch(String username, String name, ApartmentState state, Boolean visible,
                        Pageable pageable);

        @Query("SELECT COUNT(a) FROM Apartment a WHERE a.createdBy.username = :username AND (:visible is null OR a.visible = :visible) "
                        + "AND (:name is null OR lower(a.name) like :name) AND (:state is null OR a.state = :state)")
        int advancedCount(String username, String name, ApartmentState state, Boolean visible);

}
