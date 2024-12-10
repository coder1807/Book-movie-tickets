package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Cinema;
import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Schedule;

import java.sql.Date;
import java.util.List;

public record CinemaVM(Long id, String address, String map, String name, List<Room> room) {
    public static CinemaVM from(Cinema c){

        List<Room> listRoom = c.getRooms()
                .stream()
                .map(r -> new Room(
                        r.getSchedules().stream().map(ScheduleVM::from).toList()
                )
                ).toList();

        return new CinemaVM(
                c.getId(),
                c.getAddress(),
                c.getMap(),
                c.getName(),
                listRoom
        );
    }
    private record Room(List<ScheduleVM> schedules) {}
}

