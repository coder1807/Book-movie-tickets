package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Booking;
import com.example.movietickets.demo.model.BookingDetail;
import com.example.movietickets.demo.model.Purchase;
import com.example.movietickets.demo.model.Seat;
import com.example.movietickets.demo.repository.BookingDetailRepository;
import com.example.movietickets.demo.repository.BookingRepository;
import javassist.compiler.Parser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

@Setter
@Getter
@Service
@SessionScope
public class PurchaseService {

    private Purchase purchase;

    @Autowired
    private BookingRepository bookingRepository;


    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    public void addToBuy(String seats, String filmTitle, String poster, String category,
                         Long totalPrice, String cinemaName, String cinemaAddress, String startTime, String roomName) {
        purchase = new Purchase(seats, filmTitle, poster, category, totalPrice, cinemaName, cinemaAddress, startTime, roomName);
    }

    public boolean IsExist() {
        return purchase != null;
    }

    public Purchase Get() {
        return purchase;
    }

    public void clearPurchase() {
        purchase = null;
    }
}
