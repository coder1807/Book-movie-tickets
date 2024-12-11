package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Schedule;

import java.util.ArrayList;
import java.util.List;

public record RoomVM(Long id, String name, String description, Long cinemaId, List<ScheduleVM> schedules) {
    public static RoomVM from(Room room) {

        List<ScheduleVM> schedules =  room.getSchedules().stream().map(ScheduleVM::from).toList();

        return new RoomVM(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getCinema().getId(),
                schedules
        );
    }
//    private record Room(List<ScheduleVM> schedules) {}
}
