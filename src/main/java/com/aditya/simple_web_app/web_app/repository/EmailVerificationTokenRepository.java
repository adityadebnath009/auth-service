package com.aditya.simple_web_app.web_app.repository;

import com.aditya.simple_web_app.web_app.Domain.EmailVerificationToken;
import com.aditya.simple_web_app.web_app.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken,Long> {


    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findByUser(User user);
}
