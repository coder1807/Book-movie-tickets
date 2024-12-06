package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Seat;
import com.example.movietickets.demo.model.SeatType;

import java.util.List;

import static java.util.Arrays.stream;

public record SeatVM(Long id, String image, String status, String seatNumber, Long price) {
    public static SeatVM from (Seat s){
        return new SeatVM(
                s.getId(),
                s.getImage(),
                s.getStatus(),
                s.getSymbol(),
                s.getSeattype().getPrice()
        );
    }
}
