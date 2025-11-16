package com.viladevcorp.hosteo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viladevcorp.hosteo.model.ValidationCode;

public interface ValidationCodeRepository extends JpaRepository<ValidationCode, UUID> {

    List<ValidationCode> findByUserUsernameAndTypeOrderByCreatedAtDesc(String username,
            String type);

}
