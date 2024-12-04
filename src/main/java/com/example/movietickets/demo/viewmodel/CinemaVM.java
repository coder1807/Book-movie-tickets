package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Cinema;

public record CinemaVM(Long id, String address, String map, String name) {
    public static CinemaVM from(Cinema c){
        return new CinemaVM(
                c.getId(),
                c.getAddress(),
                c.getMap(),
                c.getName()

        );
    }
}
