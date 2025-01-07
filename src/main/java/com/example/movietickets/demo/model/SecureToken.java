package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "secureToken")
public class SecureToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String token;

    @Column(updatable = false)
    @Basic(optional = false)
    private LocalDateTime expiredAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "USER_ID")
    private User user;

    public boolean isExpired() {
        return getExpiredAt().isBefore(LocalDateTime.now());
    }

    public SecureToken(String token, LocalDateTime expiredAt, User user) {
        this.token = token;
        this.expiredAt = expiredAt;
        this.user = user;
    }

    public SecureToken() {
    }
}
