package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.service.UserService;
import com.example.movietickets.demo.unitest.AuthUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
public class LoginTest {

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private AuthUnitTest authService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthUnitTest(userService, encoder);
    }

    @Test
    @DisplayName("TC01 - Kiểm tra khi không nhập tên tài khoản")
    public void testEmptyUsername() {
        String result = authService.login("", "123456");
        System.out.println("Kết quả thực tế: " + result);
        assertEquals("Tên tài khoản trống", result);
    }

    @Test
    @DisplayName("TC02 - Kiểm tra khi không nhập mật khẩu")
    public void testEmptyPassword() {
        String result = authService.login("user123", "");
        System.out.println("Kết quả thực tế: " + result);
        assertEquals("Mật khẩu trống", result);
    }

    @Test
    @DisplayName("TC03 - Kiểm tra khi nhập tài khoản sai")
    public void testInvalidUsername() {
        when(userService.findByUsername("user_sai")).thenReturn(Optional.empty());
        String result = authService.login("user_sai", "password123");
        System.out.println("Kết quả thực tế: " + result);
        assertEquals("Tên tài khoản hoặc mật khẩu sai", result);
    }

    @Test
    @DisplayName("TC04 - Kiểm tra khi nhập mật khẩu sai")
    public void testInvalidPassword() {
        User mockUser = new User();
        mockUser.setUsername("admin");
        mockUser.setPassword("encoded_password");
        mockUser.setAccountVerified(true);

        when(userService.findByUsername("admin")).thenReturn(Optional.of(mockUser));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        String result = authService.login("admin", "pass_sai");
        System.out.println("Kết quả thực tế: " + result);
        assertEquals("Tên tài khoản hoặc mật khẩu sai", result);
    }

    @Test
    @DisplayName("TC05 - Kiểm tra khi nhập sai cả tài khoản và mật khẩu")
    public void testInvalidUsernameAndPassword() {
        when(userService.findByUsername("user_sai")).thenReturn(Optional.empty());

        String result = authService.login("user_sai", "pass_sai");
        assertEquals("Tên tài khoản hoặc mật khẩu sai", result);
    }

    @Test
    @DisplayName("TC06 - Kiểm tra khi tài khoản hợp lệ nhưng chưa xác thực")
    public void testUnverifiedAccount() {
        User mockUser = new User();
        mockUser.setUsername("admin");
        mockUser.setPassword("encoded_password");
        mockUser.setAccountVerified(false);

        when(userService.findByUsername("user123")).thenReturn(Optional.of(mockUser));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);

        String result = authService.login("user123", "password123");
        assertEquals("Vui lòng xác thực email để đăng nhập", result);
    }

    @Test
    @DisplayName("TC07 - Kiểm tra khi nhập đúng tài khoản, mật khẩu và đã xác thực")
    public void testSuccessfulLogin() {
        User mockUser = new User();
        mockUser.setUsername("user123");
        mockUser.setPassword("password123");
        mockUser.setAccountVerified(true);

        when(userService.findByUsername("user123")).thenReturn(Optional.of(mockUser));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);

        String result = authService.login("user123", "password123");
        assertEquals("Đăng nhập thành công, chuyển hướng vào hệ thống", result);
    }
}
