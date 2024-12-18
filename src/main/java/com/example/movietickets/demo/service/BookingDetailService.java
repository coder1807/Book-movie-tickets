package com.example.movietickets.demo.service;

import com.example.movietickets.demo.DTO.SeatDTO;
import com.example.movietickets.demo.model.BookingDetail;
import com.example.movietickets.demo.model.Seat;
import com.example.movietickets.demo.repository.BookingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingDetailService {
    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    public List<BookingDetail> getBookingDetailsByScheduleId(Long scheduleId) {
        return bookingDetailRepository.findByScheduleId(scheduleId);
    }

    //List seats booked theo schedule tránh trùng lắp
    public List<SeatDTO> getSeatBookedIdsByScheduleId(Long scheduleId) {
        return bookingDetailRepository.findByScheduleId(scheduleId)
                .stream()
                .map(BookingDetail::getSeat) // lấy seat từ booking detail
                .map(seat -> new SeatDTO(seat.getId(), seat.getSymbol())) // lấy seat id
                .distinct()
                .collect(Collectors.toList());
    }
}
