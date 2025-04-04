package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@Entity
@Table(name = "Booking_Detail")
public class BookingDetail {
    @Column(name = "BOOKING_DETAIL_ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "BOOKING_ID")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "SEAT_ID")
    private Seat seat;

    @Column(name = "PRICE")
    private Long price;

    @ManyToOne
    @JoinColumn(name = "SCHEDULE_ID")

    private Schedule schedule;
    // Constructor không tham số (mặc định)
    public BookingDetail() {}

    // Constructor nhận Booking và Seat
    public BookingDetail(Booking booking, Seat seat) {
        this.booking = booking;
        this.seat = seat;
    }
}
