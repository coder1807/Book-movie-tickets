package com.example.movietickets.demo.controller;


import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/films")
public class RatingController {

    @Autowired
    private final RatingService ratingService;

    @Autowired
    private final FilmService filmService;

    @Autowired
    private final UserService userService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/film-details/{id}/rating")
    public String addRating(@PathVariable Long id, @Valid @ModelAttribute Rating rating,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {


        // Lấy thông tin blog từ id
        Film film = filmService.getFilmByIdFilm(id);

        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("categories", categories);

        // Kiểm tra và lấy thông tin người dùng đăng nhập hiện tại
        User user = userService.getCurrentUser();

        // Thiết lập thông tin cho comment
        Rating newRating = new Rating();

        newRating.setFilm(film);
        newRating.setUser(user);
        newRating.setContent(rating.getContent());
        newRating.setStar(rating.getStar());
        newRating.setDate(LocalDateTime.now());

        // Lưu comment vào cơ sở dữ liệu
        ratingService.saveRating(newRating);

        // Điều hướng lại đến trang chi tiết blog
        return "redirect:/films/film-details/" + id;
    }

    @PostMapping("/film-details/{filmId}/delete-rating/{ratingId}")
    public String deleteRating(@PathVariable("filmId") Long filmId, @PathVariable("ratingId") Long ratingId, RedirectAttributes redirectAttributes) {
        ratingService.deleteRating(ratingId);
        redirectAttributes.addFlashAttribute("message", "Đánh giá đã được xóa thành công.");
        return "redirect:/films/film-details/" + filmId;
    }

}
