package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.SecureToken;

public interface SecureTokenService {
    SecureToken createToken();

    void saveSecureToken(SecureToken secureToken);

    SecureToken findByToken(String token);

    void removeToken(SecureToken token);
}
