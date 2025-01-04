package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.Cinema;
import com.example.movietickets.demo.model.ComboFood;
import com.example.movietickets.demo.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {
    @Query("SELECT cf FROM Cinema cf ORDER BY cf.id DESC")
    List<Cinema> findAllByOrderByIdDesc();
    @Query("SELECT c FROM Cinema c WHERE c.id =:cinemaId")
    List<Cinema> findCinemaByIdQuery(Long cinemaId);

    @Query("SELECT r FROM Room s JOIN s.cinema r WHERE s.id = :roomID")
    Cinema findCinemaByRoomId(Long roomID);

    @Query("SELECT c FROM Schedule s JOIN s.cinema c WHERE s.id = :scheduleId")
    List<Cinema> findCinemaByScheduleId(Long scheduleId);
    @Query("SELECT c FROM Schedule s JOIN s.cinema c WHERE s.film.id = :movieId")
    List<Cinema> findCinemaByMovieId(Long movieId);
}
