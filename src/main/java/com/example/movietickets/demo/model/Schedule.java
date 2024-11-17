package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "Schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "FILM_ID")
    private Film film;

    @ManyToOne
    @JoinColumn(name = "ROOM_ID")
    private Room room;
    
    @ManyToOne
    @JoinColumn(name = "CINEMA_ID")
    private Cinema cinema;

    @Column(name = "START_TIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date startTime;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<SeatReservation> seatReservations;

    // Constructor without parameters
    public Schedule() {
    }

    // Constructor with schedule ID
    public Schedule(Long id) {
        this.id = id;
    }

}
