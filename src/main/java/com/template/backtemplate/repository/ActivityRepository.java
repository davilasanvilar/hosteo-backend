package com.template.backtemplate.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.template.backtemplate.model.Activity;
import com.template.backtemplate.model.dto.ActivityDto;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    @Query("SELECT new com.template.backtemplate.model.dto.ActivityDto(a) FROM Activity a WHERE a.user.username = :username AND (:name is null OR lower(a.name) like :name) ORDER BY a.createdAt DESC")
    List<ActivityDto> advancedSearch(String username, String name, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.user.username = :username AND (:name is null OR lower(a.name) like :name)")
    int advancedCount(String username, String name);

}
