package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Rating;

import java.time.LocalDateTime;

public record RatingVM(Long ratingID, String ratingContent, LocalDateTime ratingDate, int ratingNumber, String fullName) {
    public static  RatingVM from (Rating r){
        return new RatingVM(
                r.getId(),
                r.getContent(),
                r.getDate(),
                r.getStar(),
                r.getUser().getFullname()
        );
    }

}
