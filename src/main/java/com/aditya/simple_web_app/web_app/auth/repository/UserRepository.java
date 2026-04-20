package com.aditya.simple_web_app.web_app.auth.repository;

import com.aditya.simple_web_app.web_app.auth.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String Email);
}
