package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Template;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends EntityRepository<Template> {

  @Query(
      "SELECT t FROM Template t WHERE t.createdBy.username = :username "
          + "AND (:name IS NULL OR LOWER(t.name) LIKE :name) "
          + "ORDER BY t.createdAt DESC")
  List<Template> advancedSearch(
      @Param("username") String username, @Param("name") String name, Pageable pageable);

  @Query(
      "SELECT COUNT(t) FROM Template t WHERE t.createdBy.username = :username "
          + "AND (:name IS NULL OR LOWER(t.name) LIKE :name) ")
  int advancedCount(@Param("username") String username, @Param("name") String name);
}
