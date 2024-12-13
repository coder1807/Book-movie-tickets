package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Schedule;

import java.util.Date;
import java.util.List;

public record ScheduleVM(Long scheduleId, Long roomId, Long filmId, Date start) {
    public static ScheduleVM from (Schedule s)
    {
        return new ScheduleVM(
                s.getId(),
                s.getRoom().getId(),
                s.getFilm().getId(),
                s.getStartTime()
        );
    }
    public  record  detail(Long id ,Date startDate){

        public static detail from (Schedule s)
        {

            return new detail(
                    s.getId(),
                    s.getStartTime()
            );
        }
    }
}
