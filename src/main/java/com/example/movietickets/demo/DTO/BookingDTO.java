package com.example.movietickets.demo.DTO;

import com.example.movietickets.demo.model.User;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class BookingDTO {
    private Long userID;
    private Long scheduleID;
    private List<String> seatSymbols;
    private Long foodID;
    private String methodPayment;
    private Long totalPrice;
}
