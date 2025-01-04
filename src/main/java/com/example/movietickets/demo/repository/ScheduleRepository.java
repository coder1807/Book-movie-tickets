package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s JOIN FETCH s.room r JOIN FETCH r.cinema c ORDER BY s.id DESC")
    List<Schedule> findAllWithRoomAndCinema();

    void deleteByFilmId(Long filmId);

    List<Schedule> findByFilmId(Long filmId);
    // void getScheduleByFilmId(Long filmId);

    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.film f WHERE s.cinema.id = :cinemaId")
    List<Schedule> findSchedulesByCinemaId(@Param("cinemaId") Long cinemaId);
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.film f WHERE s.cinema.id = :cinemaId and s.film.id = :movieId")
    List<Schedule> findSchedulesByCinemaIdAndMovieId(@Param("cinemaId") Long cinemaId,@Param("movieId") Long movieId);
}
