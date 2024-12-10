package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Schedule;
import com.example.movietickets.demo.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySymbolInAndRoom(List<String> symbols, Room room);

    List<Seat> findBySymbolIn(List<Long> symbols);

    List<Seat> findByRoomId(Long roomId);

    List<Seat> findByRoom(Room room);

    // This query should ensure distinct results
    @Query("SELECT DISTINCT s FROM Seat s WHERE s.room.id = :roomId")
    List<Seat> findDistinctSeatsByRoomId(@Param("roomId") Long roomId);


    // List<Seat> findBySeatTypeId(Long seattypeId);
    // List<Seat> findByRoomIdAndScheduleId(Long roomId, Long scheduleId);
}
