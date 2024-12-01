package com.example.movietickets.demo.controller;


import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.CardStudentRepository;
import com.example.movietickets.demo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
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
    @Autowired
    private UserService userService;
    @Autowired
    private CardStudentRepository cardStudentRepository;

    @GetMapping("/schedules/{id}")
    public String getSchedulesByFilmId(@PathVariable("id") Long id, Model model) {
        List<Schedule> schedules = scheduleService.getSchedulesByFilmId(id);
        User currentUser = userService.getCurrentUser();
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

    // API Enpoint check age
    @GetMapping("/check-age")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAge() {
        User currentUser = userService.getCurrentUser();
        Boolean isValidAge = userService.getAge(currentUser); // Trả về true nếu độ tuổi hợp lệ
        Boolean isVerified = cardStudentRepository.isVerified(currentUser.getId()); // Trả về true nếu đã xác thực ảnh thẻ

        Map<String, Object> results = new HashMap<>();
        results.put("isValidAge", isValidAge);
        results.put("isVerified", isVerified);
        results.put("userId", currentUser.getId());
        return ResponseEntity.ok(results);
    }
}
