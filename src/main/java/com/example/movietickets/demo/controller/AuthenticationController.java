package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "/authentication/sign-in";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User()); // Thêm một đối tượng User mới vào model
        return "/authentication/sign-up";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, // Validate đối tượng User
                           @NotNull BindingResult bindingResult, // Kết quả của quá trình validate
                           Model model) {

        if (userService.existsByUsername(user.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Tên tài khoản đã tồn tại");
        }

        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email đã tồn tại");
        }

        if (userService.existsByPhone(user.getPhone())) {
            bindingResult.rejectValue("phone", "error.user", "Số điện thoại đã đăng ký");
        }

        if (bindingResult.hasErrors()) { // Kiểm tra nếu có lỗi validate
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "/authentication/sign-up"; // Trả về lại view "register" nếu có lỗi
        }

        userService.save(user); // Lưu người dùng vào cơ sở dữ liệu
        userService.setDefaultRole(user.getUsername()); // Gán vai trò mặc định cho người dùng
        return "redirect:/login"; // Chuyển hướng người dùng tới trang "login"
    }

    @GetMapping("/404")
    public String error() {
        return "/error/404";
    }
}
