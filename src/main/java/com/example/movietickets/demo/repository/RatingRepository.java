package com.example.movietickets.demo.repository;


import com.example.movietickets.demo.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findAllByFilmId(Long FilmId);

    boolean existsByUserUsernameAndFilmId(String username, Long filmId);

    @Query("SELECT AVG(r.star) FROM Rating r WHERE r.film.id = :filmId")
    Double findAverageRatingByFilmId(Long filmId);
}
