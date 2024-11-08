package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Cinema;
import com.example.movietickets.demo.model.Country;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Autowired
    private CinemaService cinemaService;
    @Autowired
    private DistanceService distanceService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String home(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Country> countries = countryService.getAllCountries();
        User currentUser = userService.getCurrentUser();
        Long userPoint = userService.getPointUser(currentUser.getId());
        String userType = userService.getUserType(currentUser.getId());


        Long totalPrice = bookingService.getTotalPriceByUser(currentUser.getId());
        Long bookingCount = bookingService.getCountBookingByUser(currentUser.getId());

        model.addAttribute("userPoint", userPoint);
        model.addAttribute("userType", userType);
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
                                @RequestParam("address") String address,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        User currentUser = userService.getCurrentUser();
        Long userPoint = userService.getPointUser(currentUser.getId());
        String userType = userService.getUserType(currentUser.getId());

        String oldFullname = currentUser.getFullname();
        String oldPhone = currentUser.getPhone();
        String oldAddress = currentUser.getAddress();

        String message = "Thay đổi thông tin thành công";
        if (!oldFullname.equals(fullName))
        {
            currentUser.setFullname(fullName);
            message= message +  "\nĐổi tên từ "+oldFullname+" thành "+fullName;
            session.setAttribute("fullname", fullName);
        }
        if (!oldPhone.equals(phone)) {
            currentUser.setPhone(phone);
            message= message + "\nĐổi SĐT từ "+oldPhone+" thành "+phone;
        }
        if (!oldAddress.equals(address)){
            currentUser.setAddress(address);
            message= message + "\nĐổi địa chỉ từ "+oldAddress+" thành "+address;
        }
        redirectAttributes.addFlashAttribute("success",message);
        userService.saveWithoutPass(currentUser);

        model.addAttribute("userPoint", userPoint);
        model.addAttribute("userType", userType);
        model.addAttribute("user", currentUser);


        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("password") String oldPassword,
                                 @RequestParam("password2") String newPassword,
                                 @RequestParam("repassword2") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không chính xác!");
            return "redirect:/profile";
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/profile";
        }

        // Cập nhật mật khẩu mới cho người dùng
        currentUser.setPassword(newPassword);
        userService.save(currentUser);

        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("success", "Thay đổi mật khẩu thành công!");
        return "redirect:/profile";

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-points")
    public String showPoints(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Country> countries = countryService.getAllCountries();
        User currentUser = userService.getCurrentUser();
        Long userPoint = userService.getPointUser(currentUser.getId());
        String userType = userService.getUserType(currentUser.getId());

        Long totalPrice = bookingService.getTotalPriceByUser(currentUser.getId());
        Long bookingCount = bookingService.getCountBookingByUser(currentUser.getId());
        int progressWidth = Math.round(userPoint * 100 / 5000f);
        String nextType;
        if (userType.equals("STANDARD")) {
            nextType = "FRIEND";
        } else {
            nextType = "VIP";
        }
        model.addAttribute("categories", categories);
        model.addAttribute("countries", countries);
        model.addAttribute("user", currentUser);
        model.addAttribute("userPoint", userPoint);
        model.addAttribute("userType", userType);
        model.addAttribute("nextType", nextType);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("progressWidth", progressWidth);
        return "/profile/points";
    }

//    @GetMapping("/nearest-cinema")
//    public String getNearestCinema() {
//        User currentUser = userService.getCurrentUser();
//        List<Cinema> cinemas = cinemaService.getAllCinemas();
//        return distanceService.findNearestCinema(currentUser.getAddress(), cinemas);
//
//    }


}

