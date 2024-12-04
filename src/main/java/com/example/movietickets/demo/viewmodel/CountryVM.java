package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Country;

public record CountryVM(Long id, String name) {
    public static CountryVM from (Country c){
        return new CountryVM(
                c.getId(),
                c.getName()
        );
    }
}
