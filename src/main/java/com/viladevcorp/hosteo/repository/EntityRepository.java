package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.BaseEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EntityRepository<T extends BaseEntity> extends JpaRepository<T, UUID> {

  /**
   * Finds an entity by its ID and the username of its creator. The `#{#entityName}` expression is
   * a Spring Data JPA feature that gets replaced with the actual entity name at runtime.
   */
  @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.createdBy.username = :username")
  T findByIdAndCreatedByUsername(UUID id, String username);
}