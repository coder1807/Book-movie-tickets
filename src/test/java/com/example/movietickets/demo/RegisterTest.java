package com.example.movietickets.demo;

import com.example.movietickets.demo.unitest.AuthUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegisterTest {

    @InjectMocks
    private AuthUnitTest authService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthUnitTest(null, null);
    }

    @Test
    @DisplayName("TC01 - Kiểm tra khi không nhập họ tên")
    public void testEmptyName() {
        String result = authService.register("", "user123", "email@example.com", new Date(), "password", "0123456789", true);
        assertEquals("Lỗi: Họ và tên không được phép để trống", result);
    }

    @Test
    @DisplayName("TC02 - Kiểm tra khi không nhập tên tài khoản")
    public void testEmptyUsername() {
        String result = authService.register("John Doe", "", "email@example.com", new Date(), "password", "0123456789", true);
        assertEquals("Lỗi: Tên tài khoản không được phép để trống", result);
    }

    @Test
    @DisplayName("TC03 - Kiểm tra khi nhập tên tài khoản đã tồn tại")
    public void testDuplicateUsername() {
        authService.register("John Doe", "user123", "email@example.com", new Date(), "password", "0123456789", true);
        String result = authService.register("Jane Doe", "user123", "newemail@example.com", new Date(), "password", "0987654321", true);
        assertEquals("Lỗi: Tên tài khoản đã tồn tại", result);
    }

    @Test
    @DisplayName("TC04 - Kiểm tra khi không nhập email")
    public void testEmptyEmail() {
        String result = authService.register("John Doe", "user123", "", new Date(), "password", "0123456789", true);
        assertEquals("Lỗi: Email không được phép để trống", result);
    }

    @Test
    @DisplayName("TC05 - Kiểm tra khi nhập email đã tồn tại")
    public void testDuplicateEmail() {
        authService.register("John Doe", "user123", "email@example.com", new Date(), "password", "0123456789", true);
        String result = authService.register("Jane Doe", "user456", "email@example.com", new Date(), "password", "0987654321", true);
        assertEquals("Lỗi: Email đã tồn tại", result);
    }

    @Test
    @DisplayName("TC06 - Kiểm tra khi không nhập ngày sinh")
    public void testEmptyBirthDate() {
        String result = authService.register("John Doe", "user123", "email@example.com", null, "password", "0123456789", true);
        assertEquals("Lỗi: Ngày sinh không được để trống", result);
    }

    @Test
    @DisplayName("TC07 - Kiểm tra khi không nhập mật khẩu")
    public void testEmptyPassword() {
        String result = authService.register("John Doe", "user123", "email@example.com", new Date(), "", "0123456789", true);
        assertEquals("Lỗi: Mật khẩu không được để trống", result);
    }

    @Test
    @DisplayName("TC08 - Kiểm tra khi nhập số điện thoại không hợp lệ")
    public void testInvalidPhoneNumber() {
        String result = authService.register("John Doe", "user123", "email@example.com", new Date(), "password", "abc123", true);
        assertEquals("Lỗi: Số điện thoại không hợp lệ", result);
    }

    @Test
    @DisplayName("TC09 - Kiểm tra khi đăng ký thành công")
    public void testSuccessfulRegister() {
        String result = authService.register("John Doe", "user123", "email@example.com", new Date(), "password", "0123456789", true);
        assertEquals("Đăng ký thành công", result);
    }
    @Test
    @DisplayName("TC10 - Kiểm tra số điện thoại không đủ độ dài")
    public void testLengthTel(){
        String result = authService.register("John Doe", "user123", "email@example.com", new Date(), "password", "012", true);
        assertEquals("Lỗi: Số điện thoại không hợp lệ", result);
    }
    @Test
    @DisplayName("TC11 - Kiểm tra khi không đồng ý điều khoản")
    public void testDisagreeToTerms() {
        String result = authService.register("John Doe", "user123", "email@example.com", new Date(), "password", "0123456789", false);
        assertEquals("Lỗi: Phải đồng ý điều khoản", result);
    }
}

