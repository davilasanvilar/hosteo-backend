package com.viladevcorp.hosteo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  User findByEmail(String email);

  User findByUsername(String username);

}
