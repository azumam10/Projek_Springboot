package com.ukm.ukm_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukm.ukm_app.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNim(String nim);

    long countByRole(String role);
}