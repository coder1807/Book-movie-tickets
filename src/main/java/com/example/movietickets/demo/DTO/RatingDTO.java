package com.example.movietickets.demo.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingDTO {
    private Long filmId;
    private Long userId;
    private String content;
    private int ratingNumber;
}
