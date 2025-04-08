package com.example.movietickets.demo.controller;


import com.example.movietickets.demo.controller.SeatController;
import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Seat;
import com.example.movietickets.demo.service.RoomService;
import com.example.movietickets.demo.service.SeatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatByRoomIdTest {

    @Mock
    private SeatService seatService;

    @Mock
    private RoomService roomService;

    @Mock
    private Model model;

    @InjectMocks
    private SeatController controller;

    @Test
    public void testGetSeatsByRoomId_WithRoomId() {
        // Arrange
        Long roomId = 1L;
        List<Seat> expectedSeats = Arrays.asList(new Seat(), new Seat());
        List<Room> rooms = Arrays.asList(new Room(), new Room());

        when(seatService.getSeatsByRoomIdDistinct(roomId)).thenReturn(expectedSeats);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // Act
        String viewName = controller.getSeatsByRoomId(roomId, model);

        // Assert
        assertEquals("/seat/seat-list", viewName);
        verify(seatService, times(1)).getSeatsByRoomIdDistinct(roomId);
        verify(seatService, never()).getAllSeats();
        verify(model).addAttribute("seats", expectedSeats);
        verify(model).addAttribute("rooms", rooms);
        verify(model).addAttribute("selectedRoomId", roomId);
    }

    @Test
    public void testGetSeatsByRoomId_WithoutRoomId() {
        // Arrange
        List<Seat> expectedSeats = Arrays.asList(new Seat(), new Seat());
        List<Room> rooms = Arrays.asList(new Room(), new Room());

        when(seatService.getAllSeats()).thenReturn(expectedSeats);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // Act
        String viewName = controller.getSeatsByRoomId(null, model);

        // Assert
        assertEquals("/seat/seat-list", viewName);
        verify(seatService, never()).getSeatsByRoomIdDistinct(anyLong());
        verify(seatService, times(1)).getAllSeats();
        verify(model).addAttribute("seats", expectedSeats);
        verify(model).addAttribute("rooms", rooms);
        verify(model).addAttribute("selectedRoomId", null);
    }

    @Test
    public void testGetSeatsByRoomId_EmptyResults() {
        // Arrange
        Long roomId = 1L;
        List<Seat> emptySeats = Arrays.asList();
        List<Room> rooms = Arrays.asList(new Room(), new Room());

        when(seatService.getSeatsByRoomIdDistinct(roomId)).thenReturn(emptySeats);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // Act
        String viewName = controller.getSeatsByRoomId(roomId, model);

        // Assert
        assertEquals("/seat/seat-list", viewName);
        verify(seatService, times(1)).getSeatsByRoomIdDistinct(roomId);
        verify(model).addAttribute("seats", emptySeats);
        verify(model).addAttribute("rooms", rooms);
        verify(model).addAttribute("selectedRoomId", roomId);
    }
}
