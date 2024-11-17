package com.example.movietickets.demo.controller.admin;

import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Schedule;
import com.example.movietickets.demo.service.CinemaService;
import com.example.movietickets.demo.service.FilmService;
import com.example.movietickets.demo.service.RoomService;
import com.example.movietickets.demo.service.ScheduleServiceImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller("adminScheduleController")
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminScheduleController {
    @Autowired
    private final ScheduleServiceImpl scheduleService;
    @Autowired
    private final FilmService filmService;
    @Autowired
    private final RoomService roomService;
    @Autowired
    private final CinemaService cinemaService;

    @GetMapping("/schedules")
    public String listSchedules(Model model) {
        List<Schedule> schedules = scheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);
        model.addAttribute("title", "Danh sách lịch chiếu");
        return "/admin/schedule/schedule-list";
    }

    @GetMapping("/schedules/add/{id}")
    public String showAddForm(@PathVariable("id") Long id, Model model) {
        Schedule schedule = new Schedule();
        schedule.setFilm(filmService.getFilmById(id).orElseThrow(() -> new IllegalArgumentException("Invalid film Id: " + id)));

        model.addAttribute("title", "Thêm mới Lịch chiếu phim #" + schedule.getFilm().getName());
        model.addAttribute("schedule", schedule);
        model.addAttribute("id", id);
        model.addAttribute("films", filmService.getAllFilms());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "/admin/schedule/schedule-add";
    }

    @PostMapping("/schedules/add")
    public String addSchedule(@Valid Schedule schedule, BindingResult result, @RequestParam("film_id") Long id, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("films", filmService.getAllFilms());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "/schedule/schedule-add";
        }
        schedule.setFilm(filmService.getFilmById(id).orElseThrow(() -> new IllegalArgumentException("Invalid film Id: " + id)));
        scheduleService.addSchedule(schedule);
        return "redirect:/admin/schedules";
    }

    @GetMapping("/schedules/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Schedule schedule = scheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid film Id: " + id));
        model.addAttribute("title", "Chỉnh sửa Lịch chiếu #" + schedule.getId());
        model.addAttribute("schedule", schedule);
        model.addAttribute("films", filmService.getAllFilms());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        return "/admin/schedule/schedule-edit";
    }

    @PostMapping("/schedules/edit/{id}")
    public String updateSchedule(@PathVariable("id") Long id, @Valid Schedule schedule,
                                 @RequestParam("film_id") Long filmId, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("films", filmService.getAllFilms());
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("cinemas", cinemaService.getAllCinemas());
            return "/admin/schedule/schedule-edit";
        }
        Film film = filmService.getFilmById(filmId).orElseThrow(() -> new IllegalArgumentException("Invalid film Id: " + filmId));
        schedule.setFilm(film);
        Schedule existingSchedule = scheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id:" + id));

        existingSchedule.setCinema(schedule.getCinema());
        existingSchedule.setRoom(schedule.getRoom());
        scheduleService.updateSchedule(existingSchedule);
        return "redirect:/admin/schedules";
    }

    @GetMapping("/schedules/delete/{id}")
    public String deleteSchedule(@PathVariable("id") Long id) {
        scheduleService.deleteScheduleById(id);
        return "redirect:/admin/schedules";
    }

}