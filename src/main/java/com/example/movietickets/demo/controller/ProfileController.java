package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Country;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.service.BookingService;
import com.example.movietickets.demo.service.CategoryService;
import com.example.movietickets.demo.service.CountryService;
import com.example.movietickets.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class ProfileController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final CategoryService categoryService;
    private final BookingService bookingService;
    @Autowired
    private final CountryService countryService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String home(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Country> countries = countryService.getAllCountries();
        User currentUser = userService.getCurrentUser();

        Long totalPrice = bookingService.getTotalPriceByUser(currentUser.getId());
        Long bookingCount = bookingService.getCountBookingByUser(currentUser.getId());
        model.addAttribute("categories", categories);
        model.addAttribute("countries", countries);
        model.addAttribute("user", currentUser);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("totalPrice", totalPrice);

        return "/profile/profile";
    }
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("full_name") String fullName,
                                @RequestParam("phone") String phone,
                                Model model,
                                HttpSession session){
        List<Category> categories = categoryService.getAllCategories();
        List<Country> countries = countryService.getAllCountries();

        User currentUser = userService.getCurrentUser();
        currentUser.setFullname(fullName);
        currentUser.setPhone(phone);
        userService.save(currentUser);

        Long totalPrice = bookingService.getTotalPriceByUser(currentUser.getId());
        Long bookingCount = bookingService.getCountBookingByUser(currentUser.getId());

        session.setAttribute("fullname", fullName);

        model.addAttribute("categories", categories);
        model.addAttribute("countries", countries);
        model.addAttribute("user", currentUser);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("totalPrice", totalPrice);

        return "/profile/profile";
    }
}

