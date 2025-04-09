package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Data
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String poster;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private String cinemaName;

    @Column(nullable = false)
    private String cinemaAddress;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String director;

    @Column(nullable = false)
    private String actors;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private String trailer;

    @Column(nullable = false)
    private Boolean status = true;
} 