package com.template.backtemplate.repository;

import java.util.Calendar;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.template.backtemplate.model.UserSession;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    void deleteByUserId(UUID userId);

    void deleteByUserIdAndCreatedAtBefore(UUID userId, Calendar createdAt);
    
}
