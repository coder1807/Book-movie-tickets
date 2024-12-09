package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Country;
import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.Rating;

import java.util.List;
import java.util.Date;

public record FilmVM(Long id, String name, String trailer, String description, String poster, String director, String actor, Date openingday, String subtitle, int duration, String limit_age, String quanlity, String countryName,Double ratingAvg ,List<Category> categories,List<RatingVM> ratings) {
    public static FilmVM from (Film f){
        List<Category> categories = f.getCategories()
                .stream()
                .map(c ->  new Category(
                        c.getName()
                        )
                ).toList();
        List<RatingVM> ratings = f.getRatings().stream().map(RatingVM::from).toList();
        double averageRating = ratings.stream()
                .mapToDouble(RatingVM::ratingNumber)
                .average()
                .orElse(0.0);
        return new FilmVM(
                f.getId(),
                f.getName(),
                f.getTrailer(),
                f.getDescription(),
                f.getPoster(),
                f.getDirector(),
                f.getActor(),
                f.getOpeningday(),
                f.getSubtitle(),
                f.getDuration(),
                f.getLimit_age(),
                f.getQuanlity(),
                f.getCountry().getName(),
                averageRating,
                categories,
                ratings
        );
    }
    private record Category(String categoryName) {}
}
