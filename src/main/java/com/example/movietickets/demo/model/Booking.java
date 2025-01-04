package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "Booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKING_ID")
    private Long id;

    @Column(name = "FILM_NAME")
    private String filmName;

    @Column(name = "POSTER")
    private String poster;

    @Column(name = "CINEMA_NAME")
    private String cinemaName;

    @Column(name = "ROOM_NAME")
    private String roomName;

    @Column(name = "CINEMA_ADDRESS")
    private String cinemaAddress;

    @Column(name = "LICH_CHIEU")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @Column(name = "CREAT_AT")
    private Date createAt;

    @Column(name = "PRICE")
    private Long price;

    @Column(name = "SEAT_NAME")
    private String seatName;

    @Column(name = "PAYMENT")
    private String payment;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "STATUS")
    private boolean status;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "COMBO_ID")
    private ComboFood comboFood;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingDetail> bookingDetails;
}
