package com.template.jwtstarter.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.template.jwtstarter.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
