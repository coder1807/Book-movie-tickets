package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Room;

import java.util.List;

public record RoomVM(Long id, String roomName, List<ScheduleVM.detail> schedules) {

    public static  RoomVM from (Room r) {
        List<ScheduleVM.detail> schedules = r.getSchedules().stream().map(ScheduleVM.detail::from).toList();
        return new RoomVM(
                r.getId(),
                r.getName(),
                schedules
        );
    }
}
