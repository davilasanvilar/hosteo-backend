package com.viladevcorp.hosteo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.Template;
import com.viladevcorp.hosteo.model.types.CategoryEnum;

@Repository
public interface TemplateRepository extends JpaRepository<Template, UUID> {

    @Query("SELECT t FROM Template t WHERE t.createdBy.username = :username "
            + "AND (:name IS NULL OR LOWER(t.name) LIKE :name) "
            + "AND (:category IS NULL OR t.category = :category) "
            + "ORDER BY t.createdAt DESC")
    List<Template> advancedSearch(@Param("username") String username,
            @Param("name") String name,
            @Param("category") CategoryEnum category,
            Pageable pageable);

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy.username = :username "
            + "AND (:name IS NULL OR LOWER(t.name) LIKE :name) "
            + "AND (:category IS NULL OR t.category = :category)")
    int advancedCount(@Param("username") String username,
            @Param("name") String name,
            @Param("category") CategoryEnum category);
}
