package com.example.movietickets.demo;

import com.example.movietickets.demo.controller.SeatController;
import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.CardStudentRepository;
import com.example.movietickets.demo.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatsByScheduleTest {

    @Mock
    private ScheduleServiceImpl scheduleService;

    @Mock
    private SeatService seatService;

    @Mock
    private BookingDetailService bookingDetailService;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CardStudentRepository cardStudentRepository;

    @Mock
    private Model model;

    @InjectMocks
    private SeatController controller;

    @Test
    public void testGetSeatsBySchedule_ScheduleExists_NotStudent() {
        // Arrange
        Long scheduleId = 1L;
        Boolean isStudent = null;
        User currentUser = new User();
        currentUser.setId(1L);

        // Tạo các đối tượng giả lập
        Schedule schedule = new Schedule();
        Film film = new Film();
        Room room = new Room();
        room.setId(2L);
        Cinema cinema = new Cinema();
        cinema.setName("Cinema ABC");
        cinema.setAddress("123 Main Street");
        room.setCinema(cinema);
        room.setName("Room 1");
        schedule.setFilm(film);
        schedule.setRoom(room);

        // Tạo danh sách ghế và booking details
        List<Seat> seats = createSeatsList();
        List<BookingDetail> bookingDetails = createBookingDetailsList(seats.get(0));
        List<Category> categories = Arrays.asList(new Category(), new Category());

        // Mock các service calls
        when(scheduleService.getScheduleById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(seatService.getSeatsByRoomIdDistinct(room.getId())).thenReturn(seats);
        when(bookingDetailService.getBookingDetailsByScheduleId(scheduleId)).thenReturn(bookingDetails);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        String viewName = controller.getSeatsBySchedule(scheduleId, model, isStudent);

        // Assert
        assertEquals("/seat/seat-choose", viewName);

        // Verify service calls
        verify(scheduleService).getScheduleById(scheduleId);
        verify(userService).getCurrentUser();
        verify(seatService).getSeatsByRoomIdDistinct(room.getId());
        verify(bookingDetailService).getBookingDetailsByScheduleId(scheduleId);
        verify(categoryService).getAllCategories();
        verify(cardStudentRepository, never()).isVerified(anyLong());

        // Verify model attributes
        verify(model).addAttribute("categories", categories);
        verify(model).addAttribute(eq("currentTime"), anyString());
        verify(model).addAttribute("seats", seats);
        verify(model).addAttribute("film", film);
        verify(model).addAttribute("schedule", schedule);
        verify(model).addAttribute("selectedScheduleId", scheduleId);
        verify(model).addAttribute("selectedRoomId", room.getId());
        verify(model).addAttribute(eq("seatsByType"), any(Map.class));
        verify(model).addAttribute("cinemaName", cinema.getName());
        verify(model).addAttribute("cinemaAddress", cinema.getAddress());
        verify(model).addAttribute("roomName", room.getName());
        verify(model).addAttribute("is_student", false);
    }

    @Test
    public void testGetSeatsBySchedule_ScheduleExists_IsStudentAndVerified() {
        // Arrange
        Long scheduleId = 1L;
        Boolean isStudent = true;
        User currentUser = new User();
        currentUser.setId(1L);

        Schedule schedule = new Schedule();
        Film film = new Film();
        Room room = new Room();
        room.setId(2L);
        Cinema cinema = new Cinema();
        cinema.setName("Cinema ABC");
        cinema.setAddress("123 Main Street");
        room.setCinema(cinema);
        room.setName("Room 1");
        schedule.setFilm(film);
        schedule.setRoom(room);

        List<Seat> seats = createSeatsList();
        List<BookingDetail> bookingDetails = createBookingDetailsList(seats.get(0));
        List<Category> categories = Arrays.asList(new Category(), new Category());

        when(scheduleService.getScheduleById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(seatService.getSeatsByRoomIdDistinct(room.getId())).thenReturn(seats);
        when(bookingDetailService.getBookingDetailsByScheduleId(scheduleId)).thenReturn(bookingDetails);
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(cardStudentRepository.isVerified(currentUser.getId())).thenReturn(true);

        // Act
        String viewName = controller.getSeatsBySchedule(scheduleId, model, isStudent);

        // Assert
        assertEquals("/seat/seat-choose", viewName);

        // Các verify khác tương tự như test case trước
        verify(cardStudentRepository).isVerified(currentUser.getId());
        verify(model).addAttribute("is_student", true);
    }

    @Test
    public void testGetSeatsBySchedule_ScheduleExists_IsStudentButNotVerified() {
        // Arrange
        Long scheduleId = 1L;
        Boolean isStudent = true;
        User currentUser = new User();
        currentUser.setId(1L);

        Schedule schedule = new Schedule();
        Film film = new Film();
        Room room = new Room();
        Cinema cinema = new Cinema();
        cinema.setName("Test Cinema");
        cinema.setAddress("Test Address");
        schedule.setFilm(film);
        schedule.setRoom(room);

        when(scheduleService.getScheduleById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userService.getCurrentUser()).thenReturn(currentUser);
        lenient().when(cardStudentRepository.isVerified(currentUser.getId())).thenReturn(null); // ✅ Sử dụng lenient()

        // Act
        String viewName = controller.getSeatsBySchedule(scheduleId, model, isStudent);

        // Assert
        assertEquals("redirect:/error/404", viewName);

        // Verify gọi services cần thiết và không gọi các services không cần thiết
        verify(scheduleService).getScheduleById(scheduleId);
        verify(userService).getCurrentUser();
        verify(cardStudentRepository, times(1)).isVerified(currentUser.getId());

        verify(seatService, never()).getSeatsByRoomIdDistinct(anyLong());
        verify(bookingDetailService, never()).getBookingDetailsByScheduleId(anyLong()); // never() = times(0)
    }


    @Test
    public void testGetSeatsBySchedule_ScheduleNotExists() {
        // Arrange
        Long scheduleId = 1L;
        Boolean isStudent = null;

        when(scheduleService.getScheduleById(scheduleId)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.getSeatsBySchedule(scheduleId, model, isStudent);

        // Assert: kết quả cả về
        assertEquals("redirect:/404", viewName);

        // Verify: nó là chỉ gọi service cần thiết
        verify(scheduleService).getScheduleById(scheduleId);
        verify(userService, never()).getCurrentUser();
        verify(seatService, never()).getSeatsByRoomIdDistinct(anyLong());
        verify(bookingDetailService, never()).getBookingDetailsByScheduleId(anyLong());
    }

    // Helper methods to create test data
    private List<Seat> createSeatsList() {
        List<Seat> seats = new ArrayList<>();

        Seat seat1 = new Seat();
        seat1.setId(1L);
        SeatType seatType1 = new SeatType();
        seatType1.setType("VIP");
        seat1.setSeattype(seatType1);

        Seat seat2 = new Seat();
        seat2.setId(2L);
        SeatType seatType2 = new SeatType();
        seatType2.setType("Standard");
        seat2.setSeattype(seatType2);

        seats.add(seat1);
        seats.add(seat2);

        return seats;
    }

    private List<BookingDetail> createBookingDetailsList(Seat bookedSeat) {
        List<BookingDetail> bookingDetails = new ArrayList<>();

        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setSeat(bookedSeat);

        bookingDetails.add(bookingDetail);

        return bookingDetails;
    }
}