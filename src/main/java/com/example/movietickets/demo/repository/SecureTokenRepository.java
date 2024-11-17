package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.SecureToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecureTokenRepository extends JpaRepository<SecureToken, Long> {
    SecureToken findByToken(final String token);

    Long removeByToken(final String token);
}
