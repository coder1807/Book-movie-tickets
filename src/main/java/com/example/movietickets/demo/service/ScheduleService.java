package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Schedule;
import com.example.movietickets.demo.repository.ScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ScheduleService {
    List<Schedule> getSchedulesByFilmId(Long filmId);

    Optional<Schedule> getScheduleById(Long id);
}
