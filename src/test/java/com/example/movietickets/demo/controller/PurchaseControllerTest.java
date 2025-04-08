package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseController Tests")
public class PurchaseControllerTest {

    @InjectMocks
    private PurchaseController purchaseController;

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should show purchase history with successful payment message")
    void testShowPurchaseHistory_WithSuccessfulPayment() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TxnRef", "123456");

        User currentUser = new User();
        currentUser.setId(1L);
        List<Booking> bookings = createMockBookings();
        List<Category> categories = new ArrayList<>();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(bookingService.getBookingsByUser(currentUser.getId())).thenReturn(bookings);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        String viewName = purchaseController.showPurchaseHistory(params, model);

        // Assert
        assertEquals("/purchase/history", viewName);
        verify(model).addAttribute("paymentMessage", "Thanh toán thành công cho mã giao dịch: 123456");
        verify(model).addAttribute("categories", categories);
        verify(model).addAttribute("bookings", bookings);
    }

    @Test
    @DisplayName("Should show purchase history with failed payment message")
    void testShowPurchaseHistory_WithFailedPayment() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_ResponseCode", "01");
        params.put("vnp_TxnRef", "123456");

        User currentUser = new User();
        currentUser.setId(1L);
        List<Booking> bookings = createMockBookings();
        List<Category> categories = new ArrayList<>();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(bookingService.getBookingsByUser(currentUser.getId())).thenReturn(bookings);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        String viewName = purchaseController.showPurchaseHistory(params, model);

        // Assert
        assertEquals("/purchase/history", viewName);
        verify(model).addAttribute("paymentMessage", "Thanh toán thất bại, mã lỗi: 01");
        verify(model).addAttribute("categories", categories);
        verify(model).addAttribute("bookings", bookings);
    }

    @Test
    @DisplayName("Should show purchase history without payment message")
    void testShowPurchaseHistory_WithoutPaymentMessage() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        User currentUser = new User();
        currentUser.setId(1L);
        List<Booking> bookings = createMockBookings();
        List<Category> categories = new ArrayList<>();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(bookingService.getBookingsByUser(currentUser.getId())).thenReturn(bookings);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        String viewName = purchaseController.showPurchaseHistory(params, model);

        // Assert
        assertEquals("/purchase/history", viewName);
        verify(model, never()).addAttribute(eq("paymentMessage"), any());
        verify(model).addAttribute("categories", categories);
        verify(model).addAttribute("bookings", bookings);
    }

    @Test
    @DisplayName("Should show empty purchase history")
    void testShowPurchaseHistory_WithEmptyBookings() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        User currentUser = new User();
        currentUser.setId(1L);
        List<Booking> emptyBookings = new ArrayList<>();
        List<Category> categories = new ArrayList<>();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(bookingService.getBookingsByUser(currentUser.getId())).thenReturn(emptyBookings);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        String viewName = purchaseController.showPurchaseHistory(params, model);

        // Assert
        assertEquals("/purchase/history", viewName);
        verify(model).addAttribute("categories", categories);
        verify(model).addAttribute("bookings", emptyBookings);
    }

    private List<Booking> createMockBookings() {
        List<Booking> bookings = new ArrayList<>();
        
        // Create mock booking 1
        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setFilmName("Test Film 1");
        booking1.setPoster("test-poster-1.jpg");
        booking1.setCinemaName("Test Cinema 1");
        booking1.setRoomName("Room 1");
        booking1.setCinemaAddress("Test Address 1");
        booking1.setStartTime(LocalDateTime.now());
        booking1.setCreateAt(new Date());
        booking1.setPrice(100000L);
        booking1.setSeatName("A1,A2");
        booking1.setPayment("VNPAY");
        booking1.setStatus(true);
        
        // Create mock booking 2
        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setFilmName("Test Film 2");
        booking2.setPoster("test-poster-2.jpg");
        booking2.setCinemaName("Test Cinema 2");
        booking2.setRoomName("Room 2");
        booking2.setCinemaAddress("Test Address 2");
        booking2.setStartTime(LocalDateTime.now().plusDays(1));
        booking2.setCreateAt(new Date());
        booking2.setPrice(150000L);
        booking2.setSeatName("B1,B2");
        booking2.setPayment("MOMO");
        booking2.setStatus(true);

        bookings.add(booking1);
        bookings.add(booking2);
        return bookings;
    }
} 