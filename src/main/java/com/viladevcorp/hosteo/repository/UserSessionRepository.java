package com.viladevcorp.hosteo.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.UserSession;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

  Optional<UserSession> findByIdAndDeletedAtIsNull(UUID id);

  void deleteByUserId(UUID userId);

  void deleteByUserIdAndCreatedAtBefore(UUID userId, Instant createdAt);
}
