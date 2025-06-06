package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Schedule;
import com.example.movietickets.demo.repository.ScheduleRepository;
import com.example.movietickets.demo.viewmodel.ScheduleVM;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public List<Schedule> getAllSchedules() {
        // return scheduleRepository.findAll();
        return scheduleRepository.findAllWithRoomAndCinema();
    }

    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }
    public void addSchedule(Schedule schedule) {
        scheduleRepository.save(schedule);
    }

    public void updateSchedule(Schedule schedule) {
//        Schedule existingSchedule = scheduleRepository.findById(schedule.getId())
//                .orElseThrow(() -> new IllegalStateException("Schedule with ID " + schedule.getId() + " does not exist."));
//        existingSchedule.setName(room.getName());
        scheduleRepository.save(schedule);
    }

    public void deleteScheduleById(Long id) {
        scheduleRepository.deleteById(id);
    }

    public void deleteByFilmId(Long filmId) {
        scheduleRepository.deleteByFilmId(filmId);
    }

    @Override
    public List<Schedule> getSchedulesByFilmId(Long filmId) {
        return scheduleRepository.findByFilmId(filmId);
    }

    public Object getSchedulesByCinemaIdAPI(Long cinemaId) {
        List<Schedule> schedulesByRoom = scheduleRepository.findSchedulesByCinemaId(cinemaId);
       return schedulesByRoom.stream().map(ScheduleVM::from).toList();
    }
    public Object getSchedulesByCinemaIdAndMovieIdAPI(Long cinemaId, Long movieId) {
        List<Schedule> schedulesByRoom = scheduleRepository.findSchedulesByCinemaIdAndMovieId(cinemaId, movieId);
        return schedulesByRoom.stream().map(ScheduleVM::from).toList();
    }
}
