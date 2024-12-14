package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.ComboFood;
import com.example.movietickets.demo.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT cf FROM Room cf ORDER BY cf.id DESC")
    List<Room> findAllByOrderByIdDesc();


    @Query("SELECT r FROM Schedule s JOIN s.room r WHERE s.id = :scheduleId")
    Room findRoomByScheduleId (Long scheduleId);

    Room findByName(String name);

    List<Room> findRoomsByCinemaId(Long cinemaId);

}
