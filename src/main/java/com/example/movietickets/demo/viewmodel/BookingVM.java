package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public record BookingVM(Long id, String filmName, String poster, String cinemaName, String roomName, LocalDateTime startTime, Date createAt, Long price, String seatName, String payment) {
    public  static BookingVM from(Booking c){
        return new BookingVM(
                c.getId(),
                c.getFilmName(),
                c.getPoster(),
                c.getCinemaName(),
                c.getRoomName(),
                c.getStartTime(),
                c.getCreateAt(),
                c.getPrice(),
                c.getSeatName(),
                c.getPayment()
        );
    }
}
