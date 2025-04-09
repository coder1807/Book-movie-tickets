package com.example.movietickets.demo.unitest;

import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.repository.IUserRepository;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AuthUnitTest {
    @Autowired
    private final UserService userService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserRepository user_Repository;

    private final BCryptPasswordEncoder encoder;
    private Set<String> existingUsernames = new HashSet<>();
    private Set<String> existingEmails = new HashSet<>();


    @Autowired
    public AuthUnitTest(UserService userService, BCryptPasswordEncoder encoder) {
        this.userService = userService;
        this.encoder = encoder;
    }

    public String login(String username, String password) {
        if (username.isEmpty()) {
            return "Tên tài khoản trống";
        }

        if (password.isEmpty()) {
            return "Mật khẩu trống";
        }

        // Kiểm tra người dùng tồn tại không
        var userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            return "Tên tài khoản hoặc mật khẩu sai";
        }
        // Kiểm tra mật khẩu
        User user = userOptional.get();
        if (!this.encoder.matches(password, user.getPassword())) {
            return "Tên tài khoản hoặc mật khẩu sai";
        }

        // Kiểm tra tài khoản đã xác thực chưa
        if (!user.isAccountVerified()) {
            return "Vui lòng xác thực email để đăng nhập";
        }

        return "Đăng nhập thành công, chuyển hướng vào hệ thống";
    }

    public String register(String name, String username, String email, Date birthOfDate, String password, String tel, Boolean agreeToTerm){
        if (name == null || name.trim().isEmpty()) {
            return "Lỗi: Họ và tên không được phép để trống";
        }
        if (username == null || username.trim().isEmpty()) {
            return "Lỗi: Tên tài khoản không được phép để trống";
        }
        if (existingUsernames.contains(username)) {
            return "Lỗi: Tên tài khoản đã tồn tại";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Lỗi: Email không được phép để trống";
        }
        if (existingEmails.contains(email)) {
            return "Lỗi: Email đã tồn tại";
        }
        if (birthOfDate == null) {
            return "Lỗi: Ngày sinh không được để trống";
        }
        if (password == null || password.trim().isEmpty()) {
            return "Lỗi: Mật khẩu không được để trống";
        }
        if (tel == null || !tel.matches("\\d{10,11}")) {
            return "Lỗi: Số điện thoại không hợp lệ";
        }
        if(!agreeToTerm){
            return "Lỗi: Phải đồng ý điều khoản";
        }

        existingUsernames.add(username);
        existingEmails.add(email);
        return "Đăng ký thành công";
    }

}