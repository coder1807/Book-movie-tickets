package com.example.movietickets.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinemaDistance {
    private Cinema cinema;
    private int distance; // khoảng cách tính bằng mét

    // Constructor
    public CinemaDistance(Cinema cinema, int distance) {
        this.cinema = cinema;
        this.distance = distance;
    }

}

