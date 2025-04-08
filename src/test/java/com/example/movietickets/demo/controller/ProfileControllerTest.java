package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProfileController Tests")
public class ProfileControllerTest {

    @InjectMocks
    private ProfileController profileController;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should successfully change password when old password is correct")
    void testChangePassword_Success() {
        // Arrange
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        User currentUser = new User();
        currentUser.setPassword(new BCryptPasswordEncoder().encode(oldPassword));
        
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenReturn(currentUser);

        // Act
        String result = profileController.changePassword(oldPassword, newPassword, confirmPassword, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute("success", "Thay đổi mật khẩu thành công!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail when old password is incorrect")
    void testChangePassword_WrongOldPassword() {
        // Arrange
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        User currentUser = new User();
        currentUser.setPassword(new BCryptPasswordEncoder().encode("correctPassword"));
        
        when(userService.getCurrentUser()).thenReturn(currentUser);

        // Act
        String result = profileController.changePassword(oldPassword, newPassword, confirmPassword, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute("error", "Mật khẩu cũ không chính xác!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail when new password and confirm password do not match")
    void testChangePassword_PasswordMismatch() {
        // Arrange
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "differentPassword";
        
        User currentUser = new User();
        currentUser.setPassword(new BCryptPasswordEncoder().encode(oldPassword));
        
        when(userService.getCurrentUser()).thenReturn(currentUser);

        // Act
        String result = profileController.changePassword(oldPassword, newPassword, confirmPassword, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail when user is not authenticated")
    void testChangePassword_UserNotAuthenticated() {
        // Arrange
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        // Mock userService to throw an exception when getting current user
        when(userService.getCurrentUser()).thenThrow(new RuntimeException("User not authenticated"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            profileController.changePassword(oldPassword, newPassword, confirmPassword, null, redirectAttributes);
        });
        
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
        verify(userRepository, never()).save(any(User.class));
    }
} 