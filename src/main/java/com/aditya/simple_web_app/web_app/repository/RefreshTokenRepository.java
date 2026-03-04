package com.aditya.simple_web_app.web_app.repository;

import com.aditya.simple_web_app.web_app.Domain.RefreshToken;
import com.aditya.simple_web_app.web_app.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    List<RefreshToken> findByUserAndRevokedFalse(User user);

}
