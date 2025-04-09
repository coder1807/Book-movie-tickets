package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.CardStudentRepository;
import com.example.movietickets.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeatController Tests")
public class SeatControllerTest {

    @InjectMocks
    private SeatController seatController;

    @Mock
    private SeatService seatService;

    @Mock
    private ScheduleServiceImpl scheduleService;

    @Mock
    private RoomService roomService;

    @Mock
    private SeatTypeService seatTypeService;

    @Mock
    private BookingDetailService bookingDetailService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @Mock
    private CardStudentRepository cardStudentRepository;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return seat list when roomId is provided")
    void testGetSeatsByRoomId_WithValidRoomId() {
        // Arrange
        Long roomId = 1L;
        List<Seat> expectedSeats = createMockSeats();
        List<Room> expectedRooms = createMockRooms();

        when(seatService.getSeatsByRoomIdDistinct(roomId)).thenReturn(expectedSeats);
        when(roomService.getAllRooms()).thenReturn(expectedRooms);

        // Act
        String viewName = seatController.getSeatsByRoomId(roomId, model);

        // Assert
        assertEquals("/seat/seat-list", viewName);
        verify(model).addAttribute("seats", expectedSeats);
        verify(model).addAttribute("rooms", expectedRooms);
        verify(model).addAttribute("selectedRoomId", roomId);
    }

    @Test
    @DisplayName("Should return all seats when roomId is null")
    void testGetSeatsByRoomId_WithNullRoomId() {
        // Arrange
        List<Seat> expectedSeats = createMockSeats();
        List<Room> expectedRooms = createMockRooms();

        when(seatService.getAllSeats()).thenReturn(expectedSeats);
        when(roomService.getAllRooms()).thenReturn(expectedRooms);

        // Act
        String viewName = seatController.getSeatsByRoomId(null, model);

        // Assert
        assertEquals("/seat/seat-list", viewName);
        verify(model).addAttribute("seats", expectedSeats);
        verify(model).addAttribute("rooms", expectedRooms);
        verify(model).addAttribute("selectedRoomId", null);
    }

    @Test
    @DisplayName("Should return empty seat list when room has no seats")
    void testGetSeatsByRoomId_WithEmptySeats() {
        // Arrange
        Long roomId = 1L;
        List<Seat> emptySeats = new ArrayList<>();
        List<Room> expectedRooms = createMockRooms();

        when(seatService.getSeatsByRoomIdDistinct(roomId)).thenReturn(emptySeats);
        when(roomService.getAllRooms()).thenReturn(expectedRooms);

        // Act
        String viewName = seatController.getSeatsByRoomId(roomId, model);

        // Assert
        assertEquals("/seat/seat-list", viewName);
        verify(model).addAttribute("seats", emptySeats);
        verify(model).addAttribute("rooms", expectedRooms);
        verify(model).addAttribute("selectedRoomId", roomId);
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void testGetSeatsByRoomId_WithServiceException() {
        // Arrange
        Long roomId = 1L;
        List<Room> expectedRooms = createMockRooms();

        when(seatService.getSeatsByRoomIdDistinct(roomId)).thenThrow(new RuntimeException("Service error"));
        when(roomService.getAllRooms()).thenReturn(expectedRooms);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            seatController.getSeatsByRoomId(roomId, model);
        });
    }

    @Test
    @DisplayName("Should return correct view with empty room list")
    void testGetSeatsByRoomId_WithEmptyRooms() {
        // Arrange
        Long roomId = 1L;
        List<Seat> expectedSeats = createMockSeats();
        List<Room> emptyRooms = new ArrayList<>();

        when(seatService.getSeatsByRoomIdDistinct(roomId)).thenReturn(expectedSeats);
        when(roomService.getAllRooms()).thenReturn(emptyRooms);

        // Act
        String viewName = seatController.getSeatsByRoomId(roomId, model);

        // Assert
        assertEquals("/seat/seat-list", viewName);
        verify(model).addAttribute("seats", expectedSeats);
        verify(model).addAttribute("rooms", emptyRooms);
        verify(model).addAttribute("selectedRoomId", roomId);
    }

    @Test
    @DisplayName("Should handle adult ticket selection without seats")
    void testGetSeatsBySchedule_NoSeatSelection_AdultTicket() {
        // Arrange
        Long scheduleId = 1L;
        Boolean isStudent = false;

        // Mock schedule data
        Schedule schedule = new Schedule();
        Room room = new Room();
        room.setId(1L);
        Cinema cinema = new Cinema();
        cinema.setName("Test Cinema");
        cinema.setAddress("Test Address");
        room.setCinema(cinema);
        room.setName("Room 1");
        Film film = new Film();
        schedule.setRoom(room);
        schedule.setFilm(film);

        // Mock user
        User currentUser = new User();
        currentUser.setId(1L);

        // Mock seats
        List<Seat> seats = new ArrayList<>();
        List<BookingDetail> bookingDetails = new ArrayList<>();

        // Set up mock responses
        when(scheduleService.getScheduleById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(seatService.getSeatsByRoomIdDistinct(room.getId())).thenReturn(seats);
        when(bookingDetailService.getBookingDetailsByScheduleId(scheduleId)).thenReturn(bookingDetails);
        when(categoryService.getAllCategories()).thenReturn(new ArrayList<>());
        when(cardStudentRepository.isVerified(anyLong())).thenReturn(true);

        // Act
        String viewName = seatController.getSeatsBySchedule(scheduleId, model, isStudent);

        // Assert
        assertEquals("/seat/seat-choose", viewName);
        verify(model).addAttribute("seats", seats);
        verify(model).addAttribute("film", film);
        verify(model).addAttribute("schedule", schedule);
        verify(model).addAttribute("selectedScheduleId", scheduleId);
        verify(model).addAttribute("selectedRoomId", room.getId());
        verify(model).addAttribute("cinemaName", cinema.getName());
        verify(model).addAttribute("cinemaAddress", cinema.getAddress());
        verify(model).addAttribute("roomName", room.getName());
        verify(model).addAttribute("is_student", isStudent);
    }

    @Test
    @DisplayName("Should handle student ticket with unverified card")
    void testGetSeatsBySchedule_StudentTicket_UnverifiedCard() {
        // Arrange
        Long scheduleId = 1L;
        Boolean isStudent = true;

        // Mock user
        User currentUser = new User();
        currentUser.setId(1L);

        // Set up mock responses
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(cardStudentRepository.isVerified(anyLong())).thenReturn(null);

        // Act
        String viewName = seatController.getSeatsBySchedule(scheduleId, model, isStudent);

        // Assert
        assertEquals("redirect:/404", viewName);
    }

    // Helper methods to create test data
    private List<Seat> createMockSeats() {
        List<Seat> seats = new ArrayList<>();
        SeatType seatType = new SeatType("Standard");
        
        for (int i = 1; i <= 5; i++) {
            Seat seat = new Seat();
            seat.setId((long) i);
            seat.setSeattype(seatType);
            seats.add(seat);
        }
        return seats;
    }

    private List<Room> createMockRooms() {
        List<Room> rooms = new ArrayList<>();
        Cinema cinema = new Cinema();
        cinema.setName("Test Cinema");
        cinema.setAddress("Test Address");

        for (int i = 1; i <= 3; i++) {
            Room room = new Room();
            room.setId((long) i);
            room.setName("Room " + i);
            room.setCinema(cinema);
            rooms.add(room);
        }
        return rooms;
    }
} 