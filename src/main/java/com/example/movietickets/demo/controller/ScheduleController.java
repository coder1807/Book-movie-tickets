package com.example.movietickets.demo.controller;


import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Cinema;
import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.Schedule;
import com.example.movietickets.demo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller("userScheduleController")
public class ScheduleController {
    @Autowired
    private final ScheduleServiceImpl scheduleService;

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/schedules/{id}")
    public String getSchedulesByFilmId(@PathVariable("id") Long id, Model model) {
        List<Schedule> schedules = scheduleService.getSchedulesByFilmId(id);
        if (!schedules.isEmpty()) {
            // Nhóm các schedules theo cinema
            Map<Cinema, List<Schedule>> schedulesByCinema = schedules.stream()
                    .collect(Collectors.groupingBy(schedule -> schedule.getRoom().getCinema()));
            // lay ra phim de truyen thong tin phim
            Film film = schedules.isEmpty() ? null : schedules.get(0).getFilm();
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("schedulesByCinema", schedulesByCinema);
            model.addAttribute("film", film);
            model.addAttribute("title", "Danh sách lịch chiếu");
            return "/schedule/schedule-detail";
        } else {
            return "redirect:/404";
        }
    }


}
